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
 * Service để thực hiện Speech-to-Text sử dụng Gemini API
 * 
 * Luồng xử lý:
 * 1. Nhận audio bytes (webm format)
 * 2. Encode thành base64
 * 3. Gửi request đến Gemini API
 * 4. Parse response và trả về transcript text
 */
@Service
public class GeminiASRService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiASRService.class);
    
    // Sử dụng Gemini 3 Flash Preview
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent";
    
    private static final int HTTP_TIMEOUT_SECONDS = 120; // 2 phút timeout cho audio dài
    
    @Value("${gemini.api.key:}")
    private String apiKey;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public GeminiASRService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Transcribe audio bytes thành text sử dụng Gemini API
     * 
     * @param audioBytes Dữ liệu audio (webm format)
     * @param mimeType MIME type của audio (vd: audio/webm)
     * @return Transcript text
     * @throws IOException Nếu có lỗi khi gọi API
     */
    public String transcribe(byte[] audioBytes, String mimeType) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key chưa được cấu hình. Vui lòng thêm gemini.api.key vào application.properties");
        }
        
        logger.info("Bắt đầu transcribe audio: {} bytes, mimeType: {}", audioBytes.length, mimeType);
        
        // Encode audio thành base64
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
        
        // Tạo request body
        String requestBody = buildRequestBody(base64Audio, mimeType);
        
        // Gọi API
        String url = GEMINI_API_URL + "?key=" + apiKey;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            logger.info("Gemini API response status: {}", response.statusCode());
            
            if (response.statusCode() != 200) {
                logger.error("Gemini API error: {}", response.body());
                throw new IOException("Gemini API trả về lỗi: " + response.statusCode() + " - " + response.body());
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
     * Build request body cho Gemini API
     */
    private String buildRequestBody(String base64Audio, String mimeType) {
        // Gemini API format for audio transcription
        return """
            {
              "contents": [
                {
                  "role": "user",
                  "parts": [
                    {
                      "inline_data": {
                        "mime_type": "%s",
                        "data": "%s"
                      }
                    },
                    {
                      "text": "Hãy chuyển đổi đoạn audio này thành văn bản tiếng Việt. Chỉ trả về nội dung transcript, không thêm giải thích hay chú thích nào khác. Nếu audio không có tiếng nói hoặc không nghe rõ, trả về '[Không có nội dung]'."
                    }
                  ]
                }
              ],
              "generationConfig": {
                "temperature": 0.1,
                "maxOutputTokens": 8192
              }
            }
            """.formatted(mimeType, base64Audio);
    }
    
    /**
     * Parse response từ Gemini API để lấy transcript text
     */
    private String parseResponse(String responseBody) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // Gemini response structure:
            // { "candidates": [ { "content": { "parts": [ { "text": "..." } ] } } ] }
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText("");
                    return text.trim();
                }
            }
            
            logger.warn("Không tìm thấy transcript trong response: {}", responseBody);
            return "[Không thể transcribe]";
            
        } catch (Exception e) {
            logger.error("Lỗi parse Gemini response: {}", e.getMessage());
            throw new IOException("Lỗi parse response từ Gemini API", e);
        }
    }
    
    /**
     * Kiểm tra API key đã được cấu hình chưa
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
