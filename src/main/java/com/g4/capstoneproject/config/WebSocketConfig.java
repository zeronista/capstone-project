package com.g4.capstoneproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for Real-time Queue Updates
 * Configures STOMP protocol over WebSocket for bidirectional communication
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker options
     * - Enable simple broker for /topic (pub/sub pattern)
     * - Set application destination prefix to /app (for client messages)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker
        // Messages to destinations starting with "/topic" will be routed to all
        // subscribers
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages bound for @MessageMapping annotated methods
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints
     * - /ws endpoint for WebSocket connection
     * - Enable SockJS fallback for browsers that don't support WebSocket
     * - Allow cross-origin requests (configure properly in production)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register /ws endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins (configure properly in production)
                .withSockJS(); // Enable SockJS fallback for older browsers
    }
}
