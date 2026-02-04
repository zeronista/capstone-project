package com.g4.capstoneproject.controller.api;

import com.g4.capstoneproject.service.WhisperASRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * API Controller cho Whisper ASR Service
 * 
 * Cung cấp endpoints để transcribe audio sử dụng self-hosted Whisper model
 * thay vì sử dụng API của Gemini/OpenAI
 */
@RestController
@RequestMapping("/api/asr")
@Tag(name = "ASR - Speech to Text", description = "Automatic Speech Recognition APIs using self-hosted Whisper")
public class ASRApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(ASRApiController.class);
    
    private final WhisperASRService whisperASRService;
    
    public ASRApiController(WhisperASRService whisperASRService) {
        this.whisperASRService = whisperASRService;
    }
    
    /**
     * Transcribe audio file thành text (simple mode)
     */
    @PostMapping("/transcribe")
    @Operation(
        summary = "Transcribe audio to text",
        description = "Upload audio file và nhận về transcript text. Hỗ trợ các format: wav, mp3, webm, ogg, flac, m4a"
    )
    public ResponseEntity<Map<String, Object>> transcribe(
            @Parameter(description = "Audio file to transcribe", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Language code (vi=Vietnamese, en=English, etc.). Để trống để auto-detect")
            @RequestParam(value = "language", required = false) String language
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            long startTime = System.currentTimeMillis();
            
            String transcript = whisperASRService.transcribe(file, language);
            
            long elapsed = System.currentTimeMillis() - startTime;
            
            response.put("success", true);
            response.put("text", transcript);
            response.put("processing_time_ms", elapsed);
            response.put("filename", file.getOriginalFilename());
            response.put("file_size_kb", file.getSize() / 1024);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Transcription failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Transcribe audio file và trả về kết quả chi tiết với timestamps
     */
    @PostMapping("/transcribe/detailed")
    @Operation(
        summary = "Transcribe audio with detailed segments",
        description = "Upload audio file và nhận về transcript chi tiết bao gồm timestamps cho từng segment"
    )
    public ResponseEntity<Map<String, Object>> transcribeDetailed(
            @Parameter(description = "Audio file to transcribe", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Language code (vi=Vietnamese, en=English, etc.)")
            @RequestParam(value = "language", required = false) String language
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "File không được để trống");
                return ResponseEntity.badRequest().body(response);
            }
            
            WhisperASRService.TranscriptionResult result = 
                whisperASRService.transcribeDetailed(file, language);
            
            response.put("success", result.isSuccess());
            response.put("text", result.getText());
            response.put("language", result.getLanguage());
            response.put("language_probability", result.getLanguageProbability());
            response.put("duration", result.getDuration());
            response.put("processing_time", result.getProcessingTime());
            response.put("segments", result.getSegments());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Detailed transcription failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Kiểm tra trạng thái ASR service
     */
    @GetMapping("/health")
    @Operation(
        summary = "Check ASR service health",
        description = "Kiểm tra xem Whisper ASR service có đang hoạt động không"
    )
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        
        boolean available = whisperASRService.isServiceAvailable();
        
        response.put("available", available);
        response.put("status", available ? "healthy" : "unavailable");
        
        if (available) {
            Map<String, Object> serviceInfo = whisperASRService.getServiceInfo();
            response.put("service_info", serviceInfo);
        } else {
            response.put("message", "ASR service không khả dụng. Vui lòng kiểm tra Docker container.");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy danh sách các ngôn ngữ được hỗ trợ
     */
    @GetMapping("/languages")
    @Operation(
        summary = "Get supported languages",
        description = "Lấy danh sách các ngôn ngữ được Whisper hỗ trợ"
    )
    public ResponseEntity<Map<String, Object>> getSupportedLanguages() {
        Map<String, Object> response = new HashMap<>();
        
        // Whisper hỗ trợ hơn 100 ngôn ngữ, đây là các ngôn ngữ phổ biến
        Map<String, String> commonLanguages = Map.ofEntries(
            Map.entry("vi", "Vietnamese - Tiếng Việt"),
            Map.entry("en", "English"),
            Map.entry("ja", "Japanese - 日本語"),
            Map.entry("ko", "Korean - 한국어"),
            Map.entry("zh", "Chinese - 中文"),
            Map.entry("th", "Thai - ไทย"),
            Map.entry("fr", "French - Français"),
            Map.entry("de", "German - Deutsch"),
            Map.entry("es", "Spanish - Español"),
            Map.entry("pt", "Portuguese - Português"),
            Map.entry("ru", "Russian - Русский"),
            Map.entry("ar", "Arabic - العربية"),
            Map.entry("hi", "Hindi - हिन्दी"),
            Map.entry("id", "Indonesian - Bahasa Indonesia"),
            Map.entry("ms", "Malay - Bahasa Melayu")
        );
        
        response.put("common_languages", commonLanguages);
        response.put("note", "Whisper hỗ trợ hơn 100 ngôn ngữ. Để trống language để auto-detect.");
        response.put("auto_detect", true);
        
        return ResponseEntity.ok(response);
    }
}
