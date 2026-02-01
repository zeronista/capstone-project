package com.g4.capstoneproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

/**
 * Cau hinh AWS S3 Client voi cac toi uu:
 * - Retry policy: Tu dong thu lai 3 lan khi gap loi tam thoi
 * - Timeout: Gioi han thoi gian cho request/response
 * - Backoff strategy: Tang dan thoi gian cho giua cac lan thu lai
 */
@Configuration
public class S3Config {

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretAccessKey}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    // Cau hinh timeout (co the override qua application.properties)
    @Value("${aws.s3.apiCallTimeout:60}")
    private int apiCallTimeout; // Tong thoi gian cho 1 API call (giay)

    @Value("${aws.s3.apiCallAttemptTimeout:30}")
    private int apiCallAttemptTimeout; // Thoi gian cho moi lan thu (giay)

    @Value("${aws.s3.maxRetries:3}")
    private int maxRetries; // So lan thu lai toi da

    /**
     * Tao S3Client voi cau hinh retry va timeout
     * - Retry 3 lan khi gap loi mang hoac loi 5xx tu S3
     * - Timeout 30s cho moi request, tong 60s cho toan bo operation
     * - Exponential backoff: 100ms -> 200ms -> 400ms...
     */
    @Bean
    public S3Client s3Client() {
        // Cau hinh retry policy voi exponential backoff
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .numRetries(maxRetries)
                .retryCondition(RetryCondition.defaultRetryCondition())
                .backoffStrategy(BackoffStrategy.defaultStrategy())
                .build();

        // Cau hinh timeout va retry
        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(apiCallTimeout))
                .apiCallAttemptTimeout(Duration.ofSeconds(apiCallAttemptTimeout))
                .retryPolicy(retryPolicy)
                .build();

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .overrideConfiguration(overrideConfig)
                .build();
    }

    /**
     * Tao S3Presigner de tao presigned URL
     * Presigner khong can retry vi chi tao URL, khong goi network
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}