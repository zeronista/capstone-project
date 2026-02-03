package com.g4.capstoneproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * Service để thực hiện Speech-to-Text sử dụng Google Cloud Speech-to-Text API
 * 
 * Docs: https://cloud.google.com/speech-to-text/docs/reference/rest
 */
@Service
public class GoogleSpeechService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSpeechService.class);
    
    // Google Cloud Speech-to-Text API endpoint
    private static final String SPEECH_API_URL = "https://speech.googleapis.com/v1/speech:recognize";
    
    private static final int HTTP_TIMEOUT_SECONDS = 120;
    
    @Value("${google.speech.api.key:}")
    private String apiKey;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public GoogleSpeechService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Transcribe audio bytes thành text sử dụng Google Cloud Speech-to-Text API
     * 
     * @param audioBytes Dữ liệu audio
     * @param mimeType MIME type của audio (vd: audio/webm)
     * @return Transcript text
     * @throws IOException Nếu có lỗi khi gọi API
     */
    public String transcribe(byte[] audioBytes, String mimeType) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Google Speech API key chưa được cấu hình");
        }
        
        logger.info("Bắt đầu transcribe audio với Google Speech: {} bytes, mimeType: {}", audioBytes.length, mimeType);
        
        // Encode audio thành base64
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
        
        // Xác định encoding dựa trên MIME type
        String encoding = getEncoding(mimeType);
        
        // Tạo request body
        String requestBody = buildRequestBody(base64Audio, encoding);
        
        // Gọi API
        String url = SPEECH_API_URL + "?key=" + apiKey;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            logger.info("Google Speech API response status: {}", response.statusCode());
            
            if (response.statusCode() != 200) {
                logger.error("Google Speech API error: {}", response.body());
                throw new IOException("Google Speech API trả về lỗi: " + response.statusCode() + " - " + response.body());
            }
            
            // Parse response
            String transcript = parseResponse(response.body());
            logger.info("Transcribe thành công: {} ký tự", transcript.length());
            
            return transcript;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request bị interrupted", e);
        }
    }
    
    /**
     * Xác định encoding từ MIME type
     */
    private String getEncoding(String mimeType) {
        if (mimeType == null) return "WEBM_OPUS";
        
        return switch (mimeType.toLowerCase()) {
            case "audio/webm" -> "WEBM_OPUS";
            case "audio/ogg" -> "OGG_OPUS";
            case "audio/mp3", "audio/mpeg" -> "MP3";
            case "audio/wav", "audio/wave" -> "LINEAR16";
            case "audio/flac" -> "FLAC";
            default -> "WEBM_OPUS";
        };
    }
    
    /**
     * Build request body cho Google Speech API
     */
    private String buildRequestBody(String base64Audio, String encoding) {
        // Sử dụng model chirp cho chất lượng tốt nhất
        return """
            {
              "config": {
                "encoding": "%s",
                "sampleRateHertz": 48000,
                "languageCode": "vi-VN",
                "model": "default",
                "enableAutomaticPunctuation": true,
                "enableWordTimeOffsets": false
              },
              "audio": {
                "content": "%s"
              }
            }
            """.formatted(encoding, base64Audio);
    }
    
    /**
     * Parse response từ Google Speech API
     */
    private String parseResponse(String responseBody) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // Google Speech response structure:
            // { "results": [ { "alternatives": [ { "transcript": "...", "confidence": 0.9 } ] } ] }
            JsonNode results = root.path("results");
            
            if (results.isArray() && results.size() > 0) {
                StringBuilder fullTranscript = new StringBuilder();
                
                for (JsonNode result : results) {
                    JsonNode alternatives = result.path("alternatives");
                    if (alternatives.isArray() && alternatives.size() > 0) {
                        String transcript = alternatives.get(0).path("transcript").asText("");
                        fullTranscript.append(transcript).append(" ");
                    }
                }
                
                String result = fullTranscript.toString().trim();
                if (!result.isEmpty()) {
                    return result;
                }
            }
            
            logger.warn("Không tìm thấy transcript trong response: {}", responseBody);
            return "[Không có nội dung hoặc không nghe rõ]";
            
        } catch (Exception e) {
            logger.error("Lỗi parse Google Speech response: {}", e.getMessage());
            throw new IOException("Lỗi parse response từ Google Speech API", e);
        }
    }
    
    /**
     * Kiểm tra API key đã được cấu hình chưa
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
