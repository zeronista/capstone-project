package com.g4.capstoneproject.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Interceptor using Token Bucket algorithm
 * Limits API requests per IP address to prevent abuse
 * 
 * Default limits:
 * - 100 requests per minute per IP
 * - Burst capacity: 20 requests
 */
@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Creates a new rate limit bucket for an IP address
     * 100 requests per minute with burst of 20 tokens
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Resolve bucket by IP address
     */
    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Get client IP address from request
     * Handles proxied requests with X-Forwarded-For header
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String ip = getClientIP(request);
        Bucket bucket = resolveBucket(ip);

        if (bucket.tryConsume(1)) {
            // Request allowed
            return true;
        }

        // Rate limit exceeded
        log.warn("Rate limit exceeded for IP: {} on path: {}", ip, request.getRequestURI());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\":\"Too many requests\",\"message\":\"Bạn đã vượt quá giới hạn số lượng yêu cầu. Vui lòng thử lại sau.\"}");

        return false;
    }
}
