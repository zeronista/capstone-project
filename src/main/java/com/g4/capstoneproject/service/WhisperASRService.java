package com.g4.capstoneproject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Service tích hợp với self-hosted Whisper ASR Service
 * Thay thế việc sử dụng API của Gemini/OpenAI
 * 
 * ASR Service chạy trên Docker với model openai/whisper-large-v3
 */
@Service
public class WhisperASRService {
    
    private static final Logger logger = LoggerFactory.getLogger(WhisperASRService.class);
    
    @Value("${asr.service.url:http://localhost:8001}")
    private String asrServiceUrl;
    
    @Value("${asr.service.enabled:true}")
    private boolean asrEnabled;
    
    @Value("${asr.service.default-language:vi}")
    private String defaultLanguage;
    
    @Value("${asr.service.timeout:60000}")
    private int timeout;
    
    private final RestTemplate restTemplate;
    
    public WhisperASRService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Transcribe audio file thành text
     * 
     * @param audioFile File audio cần transcribe (wav, mp3, webm, etc.)
     * @return Transcript text
     */
    public String transcribe(MultipartFile audioFile) {
        return transcribe(audioFile, defaultLanguage);
    }
    
    /**
     * Transcribe audio file thành text với ngôn ngữ chỉ định
     * 
     * @param audioFile File audio cần transcribe
     * @param language Mã ngôn ngữ (vi, en, ja, etc.) hoặc null để auto-detect
     * @return Transcript text
     */
    public String transcribe(MultipartFile audioFile, String language) {
        if (!asrEnabled) {
            logger.warn("ASR Service is disabled");
            return "";
        }
        
        try {
            logger.info("Transcribing audio: {} ({}KB), language: {}", 
                audioFile.getOriginalFilename(), 
                audioFile.getSize() / 1024,
                language);
            
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }
            });
            
            if (language != null && !language.isEmpty()) {
                body.add("language", language);
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
            
            // Call ASR service
            long startTime = System.currentTimeMillis();
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                asrServiceUrl + "/transcribe/simple",
                requestEntity,
                Map.class
            );
            
            long elapsed = System.currentTimeMillis() - startTime;
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String text = (String) response.getBody().get("text");
                String detectedLanguage = (String) response.getBody().get("language");
                
                logger.info("✅ Transcription successful: {} chars, detected language: {}, took {}ms", 
                    text.length(), detectedLanguage, elapsed);
                
                return text;
            } else {
                logger.error("❌ ASR service returned error: {}", response.getStatusCode());
                return "";
            }
            
        } catch (Exception e) {
            logger.error("❌ Transcription failed: {}", e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Transcribe audio file và trả về kết quả chi tiết bao gồm timestamps
     * 
     * @param audioFile File audio cần transcribe
     * @param language Mã ngôn ngữ hoặc null
     * @return TranscriptionResult với segments và metadata
     */
    public TranscriptionResult transcribeDetailed(MultipartFile audioFile, String language) {
        if (!asrEnabled) {
            logger.warn("ASR Service is disabled");
            return TranscriptionResult.empty();
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }
            });
            
            if (language != null) {
                body.add("language", language);
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                asrServiceUrl + "/transcribe",
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                return TranscriptionResult.fromMap(data);
            }
            
            return TranscriptionResult.empty();
            
        } catch (Exception e) {
            logger.error("❌ Detailed transcription failed: {}", e.getMessage(), e);
            return TranscriptionResult.empty();
        }
    }
    
    /**
     * Transcribe audio từ byte array
     * 
     * @param audioBytes Audio data
     * @param filename Tên file (để xác định format)
     * @param language Mã ngôn ngữ
     * @return Transcript text
     */
    public String transcribeBytes(byte[] audioBytes, String filename, String language) {
        if (!asrEnabled) {
            return "";
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return filename != null ? filename : "audio.webm";
                }
            });
            
            if (language != null) {
                body.add("language", language);
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                asrServiceUrl + "/transcribe/simple",
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("text");
            }
            
            return "";
            
        } catch (Exception e) {
            logger.error("❌ Transcription failed: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Kiểm tra ASR service có available không
     */
    public boolean isServiceAvailable() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                asrServiceUrl + "/health",
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean modelLoaded = (Boolean) response.getBody().get("model_loaded");
                return modelLoaded != null && modelLoaded;
            }
            
            return false;
        } catch (Exception e) {
            logger.debug("ASR service not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy thông tin về ASR service
     */
    public Map<String, Object> getServiceInfo() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                asrServiceUrl + "/health",
                Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            return Map.of("error", e.getMessage(), "available", false);
        }
    }
    
    // ============================================
    // Inner class cho kết quả chi tiết
    // ============================================
    
    public static class TranscriptionResult {
        private boolean success;
        private String text;
        private String language;
        private double languageProbability;
        private double duration;
        private double processingTime;
        private List<Segment> segments;
        
        public static TranscriptionResult empty() {
            TranscriptionResult result = new TranscriptionResult();
            result.success = false;
            result.text = "";
            return result;
        }
        
        @SuppressWarnings("unchecked")
        public static TranscriptionResult fromMap(Map<String, Object> data) {
            TranscriptionResult result = new TranscriptionResult();
            result.success = Boolean.TRUE.equals(data.get("success"));
            result.text = (String) data.getOrDefault("text", "");
            result.language = (String) data.getOrDefault("language", "");
            result.languageProbability = ((Number) data.getOrDefault("language_probability", 0.0)).doubleValue();
            result.duration = ((Number) data.getOrDefault("duration", 0.0)).doubleValue();
            result.processingTime = ((Number) data.getOrDefault("processing_time", 0.0)).doubleValue();
            
            // Parse segments
            List<Map<String, Object>> segmentMaps = (List<Map<String, Object>>) data.get("segments");
            if (segmentMaps != null) {
                result.segments = segmentMaps.stream()
                    .map(Segment::fromMap)
                    .toList();
            }
            
            return result;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getText() { return text; }
        public String getLanguage() { return language; }
        public double getLanguageProbability() { return languageProbability; }
        public double getDuration() { return duration; }
        public double getProcessingTime() { return processingTime; }
        public List<Segment> getSegments() { return segments; }
    }
    
    public static class Segment {
        private int id;
        private double start;
        private double end;
        private String text;
        
        public static Segment fromMap(Map<String, Object> data) {
            Segment segment = new Segment();
            segment.id = ((Number) data.getOrDefault("id", 0)).intValue();
            segment.start = ((Number) data.getOrDefault("start", 0.0)).doubleValue();
            segment.end = ((Number) data.getOrDefault("end", 0.0)).doubleValue();
            segment.text = (String) data.getOrDefault("text", "");
            return segment;
        }
        
        // Getters
        public int getId() { return id; }
        public double getStart() { return start; }
        public double getEnd() { return end; }
        public String getText() { return text; }
    }
}
