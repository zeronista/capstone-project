package com.g4.capstoneproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight client for interacting with Mailtrap's transactional email API.
 */
@Service
@Slf4j
public class MailtrapClient {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiToken;
    private final String defaultCategory;

    public MailtrapClient(@Value("${mailtrap.api-url:https://send.api.mailtrap.io/api/send}") String apiUrl,
                          @Value("${mailtrap.api-token:}") String apiToken,
                          @Value("${mailtrap.timeout.connect-ms:10000}") long connectTimeoutMs,
                          @Value("${mailtrap.timeout.read-ms:10000}") long readTimeoutMs,
                          @Value("${mailtrap.default-category:Transactional}") String defaultCategory) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.restTemplate = new RestTemplate(requestFactory);
        this.apiUrl = apiUrl;
        this.apiToken = apiToken;
        this.defaultCategory = defaultCategory;
    }

    public void sendEmail(String fromEmail,
                          String fromName,
                          String toEmail,
                          String toName,
                          String subject,
                          String htmlBody,
                          String textBody,
                          String categoryOverride) {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalArgumentException("From email must be configured");
        }
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email must be provided");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Email subject must be provided");
        }
        if ((htmlBody == null || htmlBody.isBlank()) && (textBody == null || textBody.isBlank())) {
            throw new IllegalArgumentException("Either htmlBody or textBody must be provided");
        }
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalStateException("Mailtrap API token is not configured");
        }

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> from = new HashMap<>();
        from.put("email", fromEmail);
        if (fromName != null && !fromName.isBlank()) {
            from.put("name", fromName);
        }
        payload.put("from", from);

        Map<String, Object> to = new HashMap<>();
        to.put("email", toEmail);
        if (toName != null && !toName.isBlank()) {
            to.put("name", toName);
        }
        payload.put("to", List.of(to));

        payload.put("subject", subject);

        if (htmlBody != null && !htmlBody.isBlank()) {
            payload.put("html", htmlBody);
        }
        if (textBody != null && !textBody.isBlank()) {
            payload.put("text", textBody);
        }

        String category = categoryOverride != null && !categoryOverride.isBlank()
                ? categoryOverride
                : defaultCategory;
        payload.put("category", category);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(apiToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        log.debug("Sending email via Mailtrap to {}", toEmail);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody() != null ? response.getBody() : "no response body";
                throw new IllegalStateException("Mailtrap API responded with status "
                        + response.getStatusCode().value() + ": " + responseBody);
            }
            log.debug("Email sent via Mailtrap. Status: {}", response.getStatusCode());
        } catch (RestClientException ex) {
            throw new IllegalStateException("Failed to invoke Mailtrap API", ex);
        }
    }

    public boolean isConfigured() {
        return apiToken != null && !apiToken.isBlank();
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }
}
