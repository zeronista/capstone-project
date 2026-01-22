package com.g4.capstoneproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
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
}