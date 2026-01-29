package com.g4.capstoneproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.presignedUrlDuration:3600}")
    private long presignedUrlDuration; // Mặc định 1 giờ (3600 giây)

    /**
     * Upload file lên S3 và trả về key của file
     * @param file File cần upload
     * @return Key của file trong S3 (tên file)
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // Tạo tên file unique để tránh trùng lặp
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Tạo request upload
        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        // Thực hiện upload
        s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // Trả về key của file (không phải URL)
        return fileName;
    }

    /**
     * Upload file recording lên S3 vào folder voice/
     * @param file File ghi âm cần upload
     * @param callId ID của cuộc gọi
     * @param userId ID của user
     * @return Key của file trong S3 (voice/xxx.webm)
     */
    public String uploadRecordingFile(MultipartFile file, String callId, String userId) throws IOException {
        // Tạo tên file với timestamp, userId và callId
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".webm";
        
        String fileName = "voice/" + timestamp + "_" + userId + "_" + callId + extension;

        // Tạo request upload
        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType() != null ? file.getContentType() : "audio/webm")
                .build();

        // Thực hiện upload
        s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // Trả về key của file
        return fileName;
    }

    /**
     * Tạo Pre-signed URL để truy cập file trong thời gian giới hạn
     * @param fileKey Key của file trong S3
     * @param durationInSeconds Thời gian URL có hiệu lực (giây)
     * @return Pre-signed URL có thời hạn
     */
    public String generatePresignedUrl(String fileKey, long durationInSeconds) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(durationInSeconds))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * Tạo Pre-signed URL với thời gian mặc định
     * @param fileKey Key của file trong S3
     * @return Pre-signed URL có thời hạn
     */
    public String generatePresignedUrl(String fileKey) {
        return generatePresignedUrl(fileKey, presignedUrlDuration);
    }

    /**
     * Download file từ URL (Stringee recording) và upload lên S3 vào folder voice/
     * @param fileUrl URL của file cần download (từ Stringee)
     * @param callId ID của cuộc gọi (để đặt tên file)
     * @param contentType Content type của file (ví dụ: audio/mpeg, audio/wav)
     * @return Key của file trong S3 (voice/xxx.mp3)
     */
    public String uploadFileFromUrl(String fileUrl, String callId, String contentType) throws IOException {
        // Tạo tên file với timestamp và callId
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileExtension = getFileExtensionFromUrl(fileUrl, contentType);
        String fileName = "voice/" + timestamp + "_" + callId + fileExtension;

        // Download file từ URL
        URL url = new URL(fileUrl);
        try (InputStream inputStream = url.openStream()) {
            // Tạo request upload lên S3
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType != null ? contentType : "audio/mpeg")
                    .build();

            // Upload lên S3
            s3Client.putObject(putOb, RequestBody.fromInputStream(inputStream, inputStream.available()));
        }

        return fileName;
    }

    /**
     * Lấy extension của file từ URL hoặc content type
     */
    private String getFileExtensionFromUrl(String fileUrl, String contentType) {
        // Thử lấy extension từ URL trước
        if (fileUrl.contains(".")) {
            String urlExtension = fileUrl.substring(fileUrl.lastIndexOf("."));
            // Loại bỏ query string nếu có
            if (urlExtension.contains("?")) {
                urlExtension = urlExtension.substring(0, urlExtension.indexOf("?"));
            }
            if (urlExtension.matches("\\.(mp3|wav|m4a|ogg)$")) {
                return urlExtension;
            }
        }

        // Nếu không có extension hợp lệ, dùng content type
        if (contentType != null) {
            if (contentType.contains("mpeg") || contentType.contains("mp3")) {
                return ".mp3";
            } else if (contentType.contains("wav")) {
                return ".wav";
            } else if (contentType.contains("m4a")) {
                return ".m4a";
            } else if (contentType.contains("ogg")) {
                return ".ogg";
            }
        }

        // Mặc định là .mp3
        return ".mp3";
    }

    /**
     * Lấy danh sách tất cả các file recordings trong folder voice/
     * @return Danh sách các recordings với thông tin chi tiết
     */
    public List<Map<String, Object>> listRecordings() {
        List<Map<String, Object>> recordings = new ArrayList<>();
        
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("voice/")
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            
            for (S3Object s3Object : listResponse.contents()) {
                // Bỏ qua folder
                if (s3Object.key().endsWith("/")) {
                    continue;
                }
                
                Map<String, Object> recordingInfo = new HashMap<>();
                recordingInfo.put("key", s3Object.key());
                recordingInfo.put("filename", s3Object.key().substring(s3Object.key().lastIndexOf("/") + 1));
                recordingInfo.put("size", s3Object.size());
                recordingInfo.put("lastModified", s3Object.lastModified().toString());
                recordingInfo.put("url", generatePresignedUrl(s3Object.key(), 7 * 24 * 3600)); // URL có hiệu lực 7 ngày
                
                recordings.add(recordingInfo);
            }
            
            // Sắp xếp theo thời gian mới nhất
            recordings.sort((a, b) -> {
                String dateA = (String) a.get("lastModified");
                String dateB = (String) b.get("lastModified");
                return dateB.compareTo(dateA);
            });
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách recordings: " + e.getMessage(), e);
        }
        
        return recordings;
    }
}