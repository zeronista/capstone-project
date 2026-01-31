package com.g4.capstoneproject.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Web Configuration for Static Resources
 * 
 * This configuration class optimizes the serving of static resources
 * (CSS, JS, images, fonts) with proper cache control headers and
 * resource versioning support.
 * 
 * @author Development Team
 * @version 1.0.0
 * @since January 28, 2026
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

        private final RateLimitInterceptor rateLimitInterceptor;

        /**
         * Configure resource handlers for static content.
         * 
         * This method sets up optimal caching strategies for different types of static
         * resources:
         * - Versioned resources (with hash): Long-term caching (1 year)
         * - Non-versioned resources: Short-term caching (1 day) with revalidation
         * 
         * @param registry the ResourceHandlerRegistry to configure
         */
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Static resources (CSS, JS, images, fonts)
                // These are served from /static/** URLs and map to classpath:/static/
                registry.addResourceHandler("/static/**")
                                .addResourceLocations("classpath:/static/")
                                .setCacheControl(CacheControl
                                                .maxAge(365, TimeUnit.DAYS)
                                                .cachePublic()
                                                .immutable())
                                .resourceChain(true);

                // Webjars (if used - for third-party frontend libraries)
                registry.addResourceHandler("/webjars/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                                .setCacheControl(CacheControl
                                                .maxAge(365, TimeUnit.DAYS)
                                                .cachePublic()
                                                .immutable());

                // Frontend assets from stitch_frontend_n (design references)
                // These should have shorter cache for development/reference purposes
                registry.addResourceHandler("/stitch_frontend_n/**")
                                .addResourceLocations("classpath:/stitch_frontend_n/")
                                .setCacheControl(CacheControl
                                                .maxAge(1, TimeUnit.DAYS)
                                                .cachePublic()
                                                .mustRevalidate());
        }

        /**
         * Register interceptors for rate limiting on API endpoints
         * Applies rate limiting to all /api/** paths except public endpoints
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(rateLimitInterceptor)
                                .addPathPatterns("/api/**")
                                .excludePathPatterns(
                                                "/api/auth/login",
                                                "/api/auth/register",
                                                "/api/stringee/webhook/**");
        }

        /**
         * Notes on Cache Control Strategy:
         * 
         * 1. Immutable Resources (1 year cache):
         * - Used for versioned resources that never change
         * - Browser won't revalidate these files
         * - Ideal for: app.css, tailwind-config.js, api-client.js
         * - Add version query param or content hash to bust cache when updated
         * - Example: /static/css/app.css?v=1.0.0
         * 
         * 2. Revalidation Strategy (1 day cache with must-revalidate):
         * - Used for design references and development resources
         * - Browser checks with server (ETag/Last-Modified) before serving
         * - More bandwidth but ensures freshness
         * 
         * 3. Future Enhancement - Content Hash Versioning:
         * - Consider adding ResourceResolver for automatic versioning
         * - Example: app-a3b2c1d.css (hash from file content)
         * - Automatic cache busting without manual version management
         * 
         * Example configuration for content-based versioning (future):
         * 
         * registry.addResourceHandler("/static/**")
         * .addResourceLocations("classpath:/static/")
         * .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
         * .resourceChain(true)
         * .addResolver(new VersionResourceResolver()
         * .addContentVersionStrategy("/**"));
         */
}
