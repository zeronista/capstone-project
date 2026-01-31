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
import java.net.URI;
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
     * Upload avatar lên S3 vào folder image/avatars/{userId}/
     * @param file File avatar cần upload
     * @param userId ID của user
     * @return Key của file trong S3
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        // Tạo tên file với userId và timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        
        // Key: image/avatars/{userId}/avatar_{timestamp}.jpg
        String fileName = "image/avatars/" + userId + "/avatar_" + timestamp + extension;

        // Tạo request upload
        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        // Thực hiện upload
        s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // Trả về key của file
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
        URL url = URI.create(fileUrl).toURL();
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
     * Upload file tài liệu của bệnh nhân lên S3 vào folder patients/{userId}/documents/
     * @param file File tài liệu cần upload
     * @param userId ID của bệnh nhân
     * @param documentType Loại tài liệu (MEDICAL_HISTORY, PRESCRIPTION, TEST_RESULT, OTHER)
     * @return Key của file trong S3
     */
    public String uploadPatientDocument(MultipartFile file, Long userId, String documentType) throws IOException {
        // Tạo tên file với timestamp và thông tin bệnh nhân
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        
        // Tạo key với cấu trúc: patients/{userId}/documents/{documentType}/{timestamp}_{originalFilename}
        String safeFilename = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "file";
        String fileName = "patients/" + userId + "/documents/" + documentType.toLowerCase() + "/" + timestamp + "_" + safeFilename;

        // Tạo request upload
        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        // Thực hiện upload
        s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // Trả về key của file
        return fileName;
    }

    /**
     * Lấy danh sách tất cả các file tài liệu của bệnh nhân
     * @param userId ID của bệnh nhân
     * @return Danh sách các tài liệu với thông tin chi tiết
     */
    public List<Map<String, Object>> listPatientDocuments(Long userId) {
        List<Map<String, Object>> documents = new ArrayList<>();
        
        try {
            String prefix = "patients/" + userId + "/documents/";
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            
            for (S3Object s3Object : listResponse.contents()) {
                // Bỏ qua folder
                if (s3Object.key().endsWith("/")) {
                    continue;
                }
                
                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("key", s3Object.key());
                docInfo.put("filename", s3Object.key().substring(s3Object.key().lastIndexOf("/") + 1));
                docInfo.put("size", s3Object.size());
                docInfo.put("lastModified", s3Object.lastModified().toString());
                docInfo.put("url", generatePresignedUrl(s3Object.key(), 7 * 24 * 3600)); // URL có hiệu lực 7 ngày
                
                // Parse document type from path
                String[] parts = s3Object.key().split("/");
                if (parts.length >= 4) {
                    docInfo.put("documentType", parts[3].toUpperCase());
                } else {
                    docInfo.put("documentType", "OTHER");
                }
                
                documents.add(docInfo);
            }
            
            // Sắp xếp theo thời gian mới nhất
            documents.sort((a, b) -> {
                String dateA = (String) a.get("lastModified");
                String dateB = (String) b.get("lastModified");
                return dateB.compareTo(dateA);
            });
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách tài liệu bệnh nhân: " + e.getMessage(), e);
        }
        
        return documents;
    }

    /**
     * Xóa file tài liệu của bệnh nhân trên S3
     * @param fileKey Key của file cần xóa
     */
    public void deletePatientDocument(String fileKey) {
        try {
            software.amazon.awssdk.services.s3.model.DeleteObjectRequest deleteRequest = 
                software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa file: " + e.getMessage(), e);
        }
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