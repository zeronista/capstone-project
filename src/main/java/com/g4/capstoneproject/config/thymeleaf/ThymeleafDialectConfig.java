package com.g4.capstoneproject.config.thymeleaf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to register the custom UI Dialect with Thymeleaf
 * 
 * This configuration automatically registers the UiDialect bean,
 * making it available for use in all Thymeleaf templates.
 * 
 * Usage in templates (add namespace):
 * 
 * <pre>
 * {@code
 * <html xmlns:th="http://www.thymeleaf.org"
 *       xmlns:ui="http://www.example.com/ui">
 * 
 * <!-- Then use custom tags -->
 * <ui:card title="Dashboard" />
 * <ui:button type="primary" text="Save" />
 * <ui:badge variant="success" text="Active" />
 * <ui:alert type="info" message="Welcome!" />
 * }
 * </pre>
 * 
 * @author Development Team
 * @version 1.0.0
 * @since January 28, 2026
 */
@Configuration
public class ThymeleafDialectConfig {

    /**
     * Register the custom UI Dialect
     * 
     * @return UiDialect instance
     */
    @Bean
    public UiDialect uiDialect() {
        return new UiDialect();
    }
}
