package com.g4.capstoneproject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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

/**
 * Service xu ly upload/download file tu AWS S3
 * Cac chuc nang chinh:
 * - Upload file: avatar, recording, document
 * - Download file tu URL va upload len S3
 * - Tao presigned URL de truy cap file tam thoi
 * - List file theo folder/prefix
 * 
 * Cac toi uu:
 * - Retry mechanism: Tu dong thu lai khi gap loi tam thoi
 * - Timeout: Gioi han thoi gian cho request
 * - Logging: Ghi log chi tiet de debug
 * - Validation: Kiem tra file truoc khi upload
 */
@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    // Gioi han kich thuoc file (100MB)
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    
    // Timeout cho HTTP connection (30 giay)
    private static final int HTTP_CONNECT_TIMEOUT = 30000;
    private static final int HTTP_READ_TIMEOUT = 60000;
    
    // So lan thu lai khi download tu URL
    private static final int MAX_DOWNLOAD_RETRIES = 3;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.presignedUrlDuration:3600}")
    private long presignedUrlDuration; // Mac dinh 1 gio (3600 giay)

    /**
     * Upload file len S3 va tra ve key cua file
     * Bao gom validation kich thuoc file truoc khi upload
     * @param file File can upload
     * @return Key cua file trong S3 (ten file)
     * @throws IOException Neu file khong hop le hoac loi upload
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);
        
        // Tao ten file unique de tranh trung lap
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        logger.info("Uploading file: {} (size: {} bytes)", fileName, file.getSize());

        try {
            // Tao request upload
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            // Thuc hien upload
            s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            logger.info("Upload thanh cong: {}", fileName);
            // Tra ve key cua file (khong phai URL)
            return fileName;
        } catch (S3Exception e) {
            logger.error("Loi S3 khi upload file {}: {}", fileName, e.getMessage());
            throw new IOException("Loi upload file len S3: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate file truoc khi upload
     * Kiem tra: file khong null, khong rong, kich thuoc hop le
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File khong duoc de trong");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File vuot qua gioi han " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
    }

    /**
     * Upload avatar len S3 vao folder image/avatars/{userId}/
     * @param file File avatar can upload
     * @param userId ID cua user
     * @return Key cua file trong S3
     * @throws IOException Neu file khong hop le hoac loi upload
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        // Validate file
        validateFile(file);
        
        // Tao ten file voi userId va timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        
        // Key: image/avatars/{userId}/avatar_{timestamp}.jpg
        String fileName = "image/avatars/" + userId + "/avatar_" + timestamp + extension;

        logger.info("Uploading avatar cho user {}: {}", userId, fileName);

        try {
            // Tao request upload
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            // Thuc hien upload
            s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            logger.info("Upload avatar thanh cong: {}", fileName);
            // Tra ve key cua file
            return fileName;
        } catch (S3Exception e) {
            logger.error("Loi S3 khi upload avatar {}: {}", fileName, e.getMessage());
            throw new IOException("Loi upload avatar len S3: " + e.getMessage(), e);
        }
    }

    /**
     * Upload file recording len S3 vao folder voice/calls/{callId}/
     * Cau truc folder: voice/calls/{callId}/{type}_{timestamp}.webm
     * 
     * Validation:
     * - Kiem tra file khong rong
     * - Kiem tra kich thuoc file <= 100MB
     * 
     * @param file File ghi am can upload
     * @param callId ID cua cuoc goi
     * @param userId ID cua user (dung de log)
     * @return Key cua file trong S3
     * @throws IOException Neu file khong hop le hoac loi upload
     */
    public String uploadRecordingFile(MultipartFile file, String callId, String userId) throws IOException {
        return uploadRecordingFile(file, callId, userId, "combined");
    }
    
    /**
     * Upload file recording len S3 vao folder voice/calls/{callId}/
     * Cau truc folder: voice/calls/{callId}/{type}_{timestamp}.webm
     * 
     * Types: caller, receiver, combined
     * 
     * @param file File ghi am can upload
     * @param callId ID cua cuoc goi
     * @param userId ID cua user (dung de log)
     * @param recordingType Loai recording: "caller", "receiver", "combined"
     * @return Key cua file trong S3
     * @throws IOException Neu file khong hop le hoac loi upload
     */
    public String uploadRecordingFile(MultipartFile file, String callId, String userId, String recordingType) throws IOException {
        // Validate file
        validateFile(file);
        
        // Tao cau truc folder theo callId
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".webm";
        
        // Dat ten file theo type
        String fileName;
        switch (recordingType.toLowerCase()) {
            case "caller":
                fileName = "caller_" + timestamp + extension;
                break;
            case "receiver":
                fileName = "receiver_" + timestamp + extension;
                break;
            case "combined":
            default:
                fileName = "combined_" + timestamp + extension;
                break;
        }
        
        // Cau truc: voice/calls/{callId}/{type}_{timestamp}.webm
        String s3Key = "voice/calls/" + callId + "/" + fileName;

        logger.info("Uploading recording: {} (size: {} bytes, user: {}, callId: {}, type: {})", 
                s3Key, file.getSize(), userId, callId, recordingType);

        try {
            // Tao request upload
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType() != null ? file.getContentType() : "audio/webm")
                    .build();

            // Thuc hien upload
            s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            logger.info("Upload recording thanh cong: {}", s3Key);
            // Tra ve key cua file
            return s3Key;
        } catch (S3Exception e) {
            logger.error("Loi S3 khi upload recording {}: {}", s3Key, e.getMessage());
            throw new IOException("Loi upload recording len S3: " + e.getMessage(), e);
        }
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
     * Download file tu URL (Stringee recording) va upload len S3 vao folder voice/stringee/
     * 
     * Cac toi uu:
     * - Retry mechanism: Tu dong thu lai MAX_DOWNLOAD_RETRIES lan khi gap loi mang
     * - Timeout: Connect timeout 30s, Read timeout 60s
     * - Validation: Kiem tra file size truoc khi upload
     * - Streaming: Su dung buffer 8KB de doc file
     * 
     * Cau truc: voice/stringee/{yyyyMMdd}/{timestamp}_{callId}.mp3
     * 
     * @param fileUrl URL cua file can download (tu Stringee)
     * @param callId ID cua cuoc goi (de dat ten file)
     * @param contentType Content type cua file (vi du: audio/mpeg, audio/wav)
     * @return Key cua file trong S3
     * @throws IOException Neu download that bai sau MAX_DOWNLOAD_RETRIES lan hoac loi upload
     */
    public String uploadFileFromUrl(String fileUrl, String callId, String contentType) throws IOException {
        logger.info("Bat dau download file tu URL: {} (callId: {})", fileUrl, callId);
        
        // Tao cau truc folder theo ngay
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String fileExtension = getFileExtensionFromUrl(fileUrl, contentType);
        
        // Cau truc: voice/stringee/{yyyyMMdd}/{HHmmss}_{callId}.mp3
        String fileName = "voice/stringee/" + date + "/" + timestamp + "_" + callId + fileExtension;

        // Download file voi retry mechanism
        byte[] fileBytes = downloadFileWithRetry(fileUrl, MAX_DOWNLOAD_RETRIES);

        // Kiem tra file co du lieu khong
        if (fileBytes.length == 0) {
            throw new IOException("File tai xuong tu URL trong rong: " + fileUrl);
        }
        
        // Kiem tra kich thuoc file
        if (fileBytes.length > MAX_FILE_SIZE) {
            throw new IOException("File vuot qua gioi han " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
        
        logger.info("Download thanh cong: {} bytes", fileBytes.length);

        try {
            // Tao request upload len S3
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType != null ? contentType : "audio/mpeg")
                    .contentLength((long) fileBytes.length)
                    .build();

            // Upload len S3 voi kich thuoc chinh xac
            s3Client.putObject(putOb, RequestBody.fromBytes(fileBytes));
            
            logger.info("Upload thanh cong len S3: {}", fileName);
            return fileName;
        } catch (S3Exception e) {
            logger.error("Loi S3 khi upload {}: {}", fileName, e.getMessage());
            throw new IOException("Loi upload file len S3: " + e.getMessage(), e);
        }
    }
    
    /**
     * Download file tu URL voi retry mechanism
     * Tu dong thu lai khi gap loi mang tam thoi
     * 
     * @param fileUrl URL can download
     * @param maxRetries So lan thu lai toi da
     * @return Noi dung file dang byte array
     * @throws IOException Neu that bai sau tat ca cac lan thu
     */
    private byte[] downloadFileWithRetry(String fileUrl, int maxRetries) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.debug("Download attempt {}/{}: {}", attempt, maxRetries, fileUrl);
                return downloadFile(fileUrl);
            } catch (IOException e) {
                lastException = e;
                logger.warn("Download attempt {} failed: {}", attempt, e.getMessage());
                
                // Chi retry neu khong phai loi 4xx (client error)
                if (e.getMessage().contains("HTTP 4")) {
                    throw e; // Khong retry cho loi 4xx
                }
                
                // Cho 1 giay truoc khi retry
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Download bi gian doan", ie);
                    }
                }
            }
        }
        
        String errorMsg = lastException != null ? lastException.getMessage() : "Unknown error";
        throw new IOException("Download that bai sau " + maxRetries + " lan thu: " + errorMsg, lastException);
    }
    
    /**
     * Download file tu URL voi timeout
     * 
     * @param fileUrl URL can download
     * @return Noi dung file dang byte array
     * @throws IOException Neu download that bai
     */
    private byte[] downloadFile(String fileUrl) throws IOException {
        URL url = URI.create(fileUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Cau hinh timeout
            connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            connection.setReadTimeout(HTTP_READ_TIMEOUT);
            connection.setRequestMethod("GET");
            
            // Kiem tra response code
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
            }
            
            // Doc file vao byte array
            try (InputStream inputStream = connection.getInputStream();
                 java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    
                    // Kiem tra kich thuoc trong khi download
                    if (totalBytes > MAX_FILE_SIZE) {
                        throw new IOException("File vuot qua gioi han " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
                    }
                }
                
                return outputStream.toByteArray();
            }
        } finally {
            connection.disconnect();
        }
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
        // Tao ten file voi timestamp va thong tin benh nhan
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        
        // Tao key voi cau truc: patients/{userId}/documents/{documentType}/{timestamp}_{safeFilename}
        // safeFilename giu lai extension goc cua file
        String safeFilename = originalFilename != null 
            ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") 
            : "file.bin";
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
     * Lay danh sach tat ca cac file recordings trong folder voice/
     * Ho tro pagination de tranh load qua nhieu file mot luc
     * @return Danh sach cac recordings voi thong tin chi tiet
     */
    public List<Map<String, Object>> listRecordings() {
        List<Map<String, Object>> recordings = new ArrayList<>();
        
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("voice/")
                    .maxKeys(1000) // Gioi han so luong file tra ve
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            
            for (S3Object s3Object : listResponse.contents()) {
                // Bo qua folder
                if (s3Object.key().endsWith("/")) {
                    continue;
                }
                
                Map<String, Object> recordingInfo = new HashMap<>();
                recordingInfo.put("key", s3Object.key());
                recordingInfo.put("filename", s3Object.key().substring(s3Object.key().lastIndexOf("/") + 1));
                recordingInfo.put("size", s3Object.size());
                recordingInfo.put("lastModified", s3Object.lastModified().toString());
                recordingInfo.put("url", generatePresignedUrl(s3Object.key(), 7 * 24 * 3600));
                
                // Parse userId tu path neu co (voice/{userId}/...)
                String[] parts = s3Object.key().split("/");
                if (parts.length >= 2 && !parts[1].equals("stringee")) {
                    recordingInfo.put("userId", parts[1]);
                }
                
                recordings.add(recordingInfo);
            }
            
            // Sap xep theo thoi gian moi nhat
            recordings.sort((a, b) -> {
                String dateA = (String) a.get("lastModified");
                String dateB = (String) b.get("lastModified");
                return dateB.compareTo(dateA);
            });
            
        } catch (Exception e) {
            throw new RuntimeException("Loi khi lay danh sach recordings: " + e.getMessage(), e);
        }
        
        return recordings;
    }

    /**
     * Lay danh sach recordings cua mot user cu the
     * Cau truc folder: voice/{userId}/...
     * @param userId ID cua user
     * @return Danh sach recordings cua user
     */
    public List<Map<String, Object>> listUserRecordings(String userId) {
        List<Map<String, Object>> recordings = new ArrayList<>();
        
        try {
            String prefix = "voice/" + userId + "/";
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .maxKeys(500)
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            
            for (S3Object s3Object : listResponse.contents()) {
                if (s3Object.key().endsWith("/")) {
                    continue;
                }
                
                Map<String, Object> recordingInfo = new HashMap<>();
                recordingInfo.put("key", s3Object.key());
                recordingInfo.put("filename", s3Object.key().substring(s3Object.key().lastIndexOf("/") + 1));
                recordingInfo.put("size", s3Object.size());
                recordingInfo.put("lastModified", s3Object.lastModified().toString());
                recordingInfo.put("url", generatePresignedUrl(s3Object.key(), 7 * 24 * 3600));
                recordingInfo.put("userId", userId);
                
                // Parse ngay tu path (voice/{userId}/{yyyyMMdd}/...)
                String[] parts = s3Object.key().split("/");
                if (parts.length >= 3) {
                    recordingInfo.put("date", parts[2]);
                }
                
                recordings.add(recordingInfo);
            }
            
            // Sap xep theo thoi gian moi nhat
            recordings.sort((a, b) -> {
                String dateA = (String) a.get("lastModified");
                String dateB = (String) b.get("lastModified");
                return dateB.compareTo(dateA);
            });
            
        } catch (Exception e) {
            throw new RuntimeException("Loi khi lay danh sach recordings cua user: " + e.getMessage(), e);
        }
        
        return recordings;
    }

    /**
     * Kiem tra file co ton tai trong S3 khong
     * @param fileKey Key cua file
     * @return true neu file ton tai
     */
    public boolean doesFileExist(String fileKey) {
        try {
            software.amazon.awssdk.services.s3.model.HeadObjectRequest headRequest = 
                software.amazon.awssdk.services.s3.model.HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Loi khi kiem tra file ton tai: " + e.getMessage(), e);
        }
    }

    /**
     * Download file bytes từ S3
     * @param fileKey Key của file trong S3
     * @return byte array chứa nội dung file
     * @throws IOException Nếu có lỗi khi download
     */
    public byte[] downloadFileBytes(String fileKey) throws IOException {
        try {
            logger.info("Downloading file from S3: {}", fileKey);
            
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            
            try (InputStream inputStream = s3Client.getObject(getRequest)) {
                byte[] bytes = inputStream.readAllBytes();
                logger.info("Downloaded {} bytes from S3", bytes.length);
                return bytes;
            }
        } catch (S3Exception e) {
            logger.error("S3 error downloading file {}: {}", fileKey, e.getMessage());
            throw new IOException("Lỗi download file từ S3: " + e.getMessage(), e);
        }
    }

    /**
     * Upload text content lên S3 dưới dạng file .txt
     * @param content Nội dung text cần upload
     * @param fileKey Key của file (đường dẫn trong S3)
     * @return Key của file đã upload
     * @throws IOException Nếu có lỗi khi upload
     */
    public String uploadTextContent(String content, String fileKey) throws IOException {
        try {
            logger.info("Uploading text content to S3: {} ({} chars)", fileKey, content.length());
            
            byte[] contentBytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType("text/plain; charset=utf-8")
                    .build();
            
            s3Client.putObject(putRequest, RequestBody.fromBytes(contentBytes));
            
            logger.info("Text content uploaded successfully to: {}", fileKey);
            return fileKey;
            
        } catch (S3Exception e) {
            logger.error("S3 error uploading text content: {}", e.getMessage());
            throw new IOException("Lỗi upload text lên S3: " + e.getMessage(), e);
        }
    }

    /**
     * Download text content từ S3
     * @param fileKey Key của file .txt trong S3
     * @return Nội dung text
     * @throws IOException Nếu có lỗi khi download
     */
    public String downloadTextContent(String fileKey) throws IOException {
        byte[] bytes = downloadFileBytes(fileKey);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Lấy transcript key từ recording key
     * Ví dụ: recordings/call_user_3_to_user_1_xxx.webm -> transcripts/call_user_3_to_user_1_xxx.txt
     * @param recordingKey Key của file recording
     * @return Key của file transcript tương ứng
     */
    public String getTranscriptKeyFromRecordingKey(String recordingKey) {
        // Thay đổi folder từ recordings -> transcripts và extension từ .webm -> .txt
        String transcriptKey = recordingKey
                .replace("recordings/", "transcripts/")
                .replace("voice/", "transcripts/")
                .replaceAll("\\.(webm|mp3|wav|ogg|m4a)$", ".txt");
        
        // Nếu không có folder prefix, thêm transcripts/
        if (!transcriptKey.startsWith("transcripts/")) {
            // Extract filename
            String filename = transcriptKey;
            if (transcriptKey.contains("/")) {
                filename = transcriptKey.substring(transcriptKey.lastIndexOf("/") + 1);
            }
            transcriptKey = "transcripts/" + filename;
        }
        
        return transcriptKey;
    }

    /**
     * Tìm file recording trên S3 theo filename pattern
     * Do file được lưu với format: voice/{userId}/{date}/{filename}
     * Cần tìm đúng key từ filename
     * 
     * @param filename Tên file cần tìm (vd: call_user_3_to_user_1_xxx.webm)
     * @return Full S3 key nếu tìm thấy, null nếu không
     */
    public String findRecordingKeyByFilename(String filename) {
        try {
            logger.info("Searching for recording file: {}", filename);
            
            // List all objects trong folder voice/
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("voice/")
                    .build();
            
            ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);
            
            for (S3Object object : response.contents()) {
                String key = object.key();
                // Check nếu key kết thúc bằng filename
                if (key.endsWith(filename) || key.endsWith("/" + filename)) {
                    logger.info("Found recording key: {}", key);
                    return key;
                }
            }
            
            // Nếu có nhiều trang, tiếp tục tìm
            while (response.isTruncated()) {
                listRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix("voice/")
                        .continuationToken(response.nextContinuationToken())
                        .build();
                response = s3Client.listObjectsV2(listRequest);
                
                for (S3Object object : response.contents()) {
                    String key = object.key();
                    if (key.endsWith(filename) || key.endsWith("/" + filename)) {
                        logger.info("Found recording key: {}", key);
                        return key;
                    }
                }
            }
            
            logger.warn("Recording file not found: {}", filename);
            return null;
            
        } catch (S3Exception e) {
            logger.error("S3 error searching for file {}: {}", filename, e.getMessage());
            return null;
        }
    }

    /**
     * Xóa file trên S3
     * @param fileKey Key của file cần xóa
     * @throws IOException Nếu có lỗi khi xóa
     */
    public void deleteFile(String fileKey) throws IOException {
        try {
            logger.info("Deleting file from S3: {}", fileKey);
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
            
            logger.info("File deleted successfully: {}", fileKey);
            
        } catch (S3Exception e) {
            logger.error("S3 error deleting file {}: {}", fileKey, e.getMessage());
            throw new IOException("Lỗi xóa file từ S3: " + e.getMessage(), e);
        }
    }
}