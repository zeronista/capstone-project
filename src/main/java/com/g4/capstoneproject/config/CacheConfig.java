package com.g4.capstoneproject.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration using Caffeine
 * Configures caching strategy for frequently accessed data
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Caffeine Cache Manager with multiple caches
     * Each cache has different TTL and size limits based on usage patterns
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "users", // User lookups (findByEmail, findById)
                "dashboardStats", // Dashboard statistics
                "knowledgeArticles", // Knowledge base articles
                "prescriptions", // Patient prescriptions
                "treatmentPlans", // Treatment plans
                "patients", // Patient lists
                "doctors" // Doctor lists
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Default Caffeine cache configuration
     * - 30 minute expiration after write
     * - Maximum 1000 entries
     * - Record cache statistics for monitoring
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats();
    }

    /**
     * Specialized cache for dashboard statistics
     * Shorter TTL since stats need to be more current
     */
    @Bean
    public Caffeine<Object, Object> dashboardStatsCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(50)
                .recordStats();
    }

    /**
     * Knowledge articles cache
     * Longer TTL since articles change infrequently
     */
    @Bean
    public Caffeine<Object, Object> knowledgeArticlesCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats();
    }

    /**
     * User cache configuration
     * Medium TTL for user data
     */
    @Bean
    public Caffeine<Object, Object> usersCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats();
    }

    /**
     * Prescription cache
     * Medium TTL, larger size for high-traffic feature
     */
    @Bean
    public Caffeine<Object, Object> prescriptionsCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats();
    }
}
