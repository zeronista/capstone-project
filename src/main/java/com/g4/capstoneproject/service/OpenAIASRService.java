package com.g4.capstoneproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service để thực hiện Speech-to-Text sử dụng OpenAI Whisper API
 * 
 * Luồng xử lý:
 * 1. Nhận audio bytes (webm format)
 * 2. Gửi multipart request đến OpenAI Whisper API
 * 3. Parse response và trả về transcript text
 */
@Service
public class OpenAIASRService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIASRService.class);
    
    private static final String WHISPER_API_URL = "https://api.openai.com/v1/audio/transcriptions";
    private static final int TIMEOUT_MS = 120000; // 2 phút
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    private final ObjectMapper objectMapper;
    
    public OpenAIASRService() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Transcribe audio bytes thành text sử dụng OpenAI Whisper API
     * 
     * @param audioBytes Dữ liệu audio (webm format)
     * @param filename Tên file gốc
     * @return Transcript text
     * @throws IOException Nếu có lỗi khi gọi API
     */
    public String transcribe(byte[] audioBytes, String filename) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key chưa được cấu hình. Vui lòng thêm openai.api.key vào application.properties");
        }
        
        logger.info("Bắt đầu transcribe audio với OpenAI Whisper: {} bytes, filename: {}", audioBytes.length, filename);
        
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        
        URL url = new URL(WHISPER_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            try (OutputStream os = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {
                
                // Add file part
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filename).append("\"\r\n");
                writer.append("Content-Type: audio/webm\r\n\r\n");
                writer.flush();
                os.write(audioBytes);
                os.flush();
                writer.append("\r\n");
                
                // Add model part
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"model\"\r\n\r\n");
                writer.append("whisper-1\r\n");
                
                // Add language part (Vietnamese)
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
                writer.append("vi\r\n");
                
                // Add response format
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"response_format\"\r\n\r\n");
                writer.append("json\r\n");
                
                // End boundary
                writer.append("--").append(boundary).append("--\r\n");
                writer.flush();
            }
            
            int responseCode = conn.getResponseCode();
            logger.info("OpenAI Whisper API response status: {}", responseCode);
            
            String responseBody;
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    responseBody = sb.toString();
                }
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    responseBody = sb.toString();
                }
                logger.error("OpenAI Whisper API error: {}", responseBody);
                throw new IOException("OpenAI API trả về lỗi: " + responseCode + " - " + responseBody);
            }
            
            // Parse response
            String transcript = parseResponse(responseBody);
            logger.info("Transcribe thành công: {} ký tự", transcript.length());
            
            return transcript;
            
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * Parse response từ OpenAI Whisper API
     */
    private String parseResponse(String responseBody) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("text").asText("");
            return text.trim();
        } catch (Exception e) {
            logger.error("Lỗi parse OpenAI response: {}", e.getMessage());
            throw new IOException("Lỗi parse response từ OpenAI API", e);
        }
    }
    
    /**
     * Kiểm tra API key đã được cấu hình chưa
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
