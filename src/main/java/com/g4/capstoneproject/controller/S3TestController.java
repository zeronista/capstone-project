package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/s3-test")
public class S3TestController {

    @Autowired
    private S3Service s3Service;

    /**
     * Hiển thị trang test upload S3
     */
    @GetMapping
    public String showTestPage() {
        return "test/s3-upload";
    }

    /**
     * API endpoint để upload file lên S3
     * 
     * @param file File được upload từ form
     * @return JSON response với URL của file đã upload
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File không được để trống"));
            }

            // Validate file size (giới hạn 10MB)
            long maxFileSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File không được vượt quá 10MB"));
            }

            // Upload file lên S3 và nhận về file key
            String fileKey = s3Service.uploadFile(file);
            
            // Tạo Pre-signed URL có thời hạn 1 giờ
            String presignedUrl = s3Service.generatePresignedUrl(fileKey, 3600);

            // Trả về response thành công
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upload file thành công!");
            response.put("fileUrl", presignedUrl);
            response.put("fileKey", fileKey);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("contentType", file.getContentType());
            response.put("urlExpiresIn", "1 giờ");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi upload file: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API endpoint để kiểm tra kết nối S3
     */
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<?> checkS3Connection() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "S3 Service đã sẵn sàng");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * API endpoint để tạo Pre-signed URL cho file đã tồn tại
     * 
     * @param fileKey Key của file trong S3
     * @param duration Thời gian URL có hiệu lực (giây), mặc định 3600 (1 giờ)
     * @return JSON response với Pre-signed URL
     */
    @GetMapping("/generate-url")
    @ResponseBody
    public ResponseEntity<?> generatePresignedUrl(
            @RequestParam("fileKey") String fileKey,
            @RequestParam(value = "duration", defaultValue = "3600") long duration) {
        try {
            // Validate duration (tối đa 7 ngày = 604800 giây)
            if (duration > 604800) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thời gian tối đa là 7 ngày (604800 giây)"));
            }

            // Tạo Pre-signed URL
            String presignedUrl = s3Service.generatePresignedUrl(fileKey, duration);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo Pre-signed URL thành công!");
            response.put("fileKey", fileKey);
            response.put("presignedUrl", presignedUrl);
            response.put("expiresIn", duration + " giây");
            response.put("expiresAt", System.currentTimeMillis() + (duration * 1000));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
}
