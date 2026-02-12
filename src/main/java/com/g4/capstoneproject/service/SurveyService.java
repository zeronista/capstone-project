package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.SurveyDTO;
import com.g4.capstoneproject.entity.Survey;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.SurveyRepository;
import com.g4.capstoneproject.repository.UserRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.FormsScopes;
import com.google.api.services.forms.v1.model.FormResponse;
import com.google.api.services.forms.v1.model.ListFormResponsesResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;

    @Value("${google.forms.credentials.path:classpath:google-credentials.json}")
    private Resource credentialsResource;

    @Value("${google.forms.application-name:Capstone Project}")
    private String applicationName;

    @Value("${google.forms.sync.max-responses-per-page:200}")
    private Integer maxResponsesPerPage;

    /**
     * Get all surveys
     */
    public List<SurveyDTO> getAllSurveys() {
        List<Survey> surveys = surveyRepository.findAllByOrderByDisplayOrderAsc();
        List<SurveyDTO> result = surveys.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        enrichResponseCountsFromGoogleForms(surveys, result);
        return result;
    }

    /**
     * Get active surveys
     */
    public List<SurveyDTO> getActiveSurveys() {
        List<Survey> surveys = surveyRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        List<SurveyDTO> result = surveys.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        enrichResponseCountsFromGoogleForms(surveys, result);
        return result;
    }

    /**
     * Get surveys for landing page
     */
    public List<SurveyDTO> getSurveysForLandingPage() {
        List<Survey> surveys = surveyRepository.findByShowOnLandingTrueAndIsActiveTrueOrderByDisplayOrderAsc();
        List<SurveyDTO> result = surveys.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        enrichResponseCountsFromGoogleForms(surveys, result);
        return result;
    }

    /**
     * Get survey by ID
     */
    public SurveyDTO getSurveyById(Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found with id: " + id));
        return toDTO(survey);
    }

    /**
     * Create new survey
     */
    @Transactional
    public SurveyDTO createSurvey(SurveyDTO dto) {
        // Validation
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống");
        }
        if (dto.getFormUrl() == null || dto.getFormUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Đường dẫn form không được để trống");
        }

        User currentUser = getCurrentUser();

        Survey survey = Survey.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .formUrl(dto.getFormUrl())
                .iconType(dto.getIconType() != null ? dto.getIconType() : "survey")
                .iconColor(dto.getIconColor() != null ? dto.getIconColor() : "#3B82F6")
                .tag(dto.getTag() != null ? dto.getTag() : "Khảo sát")
                .tagColor(dto.getTagColor() != null ? dto.getTagColor() : "#3B82F6")
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .responseCount(0)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE)
                .showOnLanding(dto.getShowOnLanding() != null ? dto.getShowOnLanding() : Boolean.TRUE)
                .createdBy(currentUser)
                .build();

        Survey saved = surveyRepository.save(survey);
        log.info("Created new survey: {} by user: {}", saved.getTitle(), currentUser.getEmail());

        return toDTO(saved);
    }

    /**
     * Update survey
     */
    @Transactional
    public SurveyDTO updateSurvey(Long id, SurveyDTO dto) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found with id: " + id));

        // Validation
        if (dto.getTitle() != null && dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống");
        }
        if (dto.getFormUrl() != null && dto.getFormUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Đường dẫn form không được để trống");
        }

        if (dto.getTitle() != null)
            survey.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            survey.setDescription(dto.getDescription());
        if (dto.getFormUrl() != null)
            survey.setFormUrl(dto.getFormUrl());
        if (dto.getIconType() != null)
            survey.setIconType(dto.getIconType());
        if (dto.getIconColor() != null)
            survey.setIconColor(dto.getIconColor());
        if (dto.getTag() != null)
            survey.setTag(dto.getTag());
        if (dto.getTagColor() != null)
            survey.setTagColor(dto.getTagColor());
        if (dto.getDisplayOrder() != null)
            survey.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getIsActive() != null)
            survey.setIsActive(dto.getIsActive());
        if (dto.getShowOnLanding() != null)
            survey.setShowOnLanding(dto.getShowOnLanding());
        if (dto.getResponseCount() != null)
            survey.setResponseCount(dto.getResponseCount());

        Survey saved = surveyRepository.save(survey);
        log.info("Updated survey: {}", saved.getId());

        return toDTO(saved);
    }

    /**
     * Delete survey
     */
    @Transactional
    public void deleteSurvey(Long id) {
        if (!surveyRepository.existsById(id)) {
            throw new RuntimeException("Survey not found with id: " + id);
        }
        surveyRepository.deleteById(id);
        log.info("Deleted survey: {}", id);
    }

    /**
     * Toggle survey active status
     */
    @Transactional
    public SurveyDTO toggleSurveyStatus(Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found with id: " + id));

        survey.setIsActive(!survey.getIsActive());
        Survey saved = surveyRepository.save(survey);
        log.info("Toggled survey status: {} -> {}", id, saved.getIsActive());

        return toDTO(saved);
    }

    /**
     * Get survey statistics
     */
    public Map<String, Object> getSurveyStats() {
        // Use live response counts so stats khớp với homepage & màn quản lý
        List<SurveyDTO> surveys = getAllSurveys();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSurveys", surveys.size());
        stats.put("activeSurveys", surveys.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .count());
        stats.put("totalResponses", surveys.stream()
                .mapToLong(s -> s.getResponseCount() != null ? s.getResponseCount() : 0)
                .sum());
        return stats;
    }

    /**
     * Increment response count
     */
    @Transactional
    public void incrementResponseCount(Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found with id: " + id));
        survey.setResponseCount(survey.getResponseCount() + 1);
        surveyRepository.save(survey);
    }

    /**
     * Helper: enrich DTO list with live response count from Google Forms.
     * Không ghi xuống DB, chỉ dùng cho view (landing, quản lý survey).
     */
    private void enrichResponseCountsFromGoogleForms(List<Survey> surveys, List<SurveyDTO> dtos) {
        if (surveys == null || surveys.isEmpty()) {
            return;
        }

        try {
            Forms formsService = createFormsService();
            for (int i = 0; i < surveys.size(); i++) {
                Survey survey = surveys.get(i);
                SurveyDTO dto = dtos.get(i);
                String formId = extractFormIdFromUrl(survey.getFormUrl());
                if (formId == null) {
                    continue;
                }
                try {
                    int responseCount = countResponses(formsService, formId);
                    dto.setResponseCount(responseCount);
                } catch (Exception ex) {
                    log.warn("Failed to load live response count for survey id={} formId={} message={}",
                            survey.getId(), formId, ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Cannot create Google Forms client for survey response enrichment: {}", e.getMessage());
        }
    }

    /**
     * Sync responseCount of surveys with actual response count from Google Forms.
     * This uses the formUrl field to extract Google Form ID and count responses via Google Forms API.
     */
    @Transactional
    public Map<String, Object> syncResponseCountsFromGoogleForms() {
        Map<String, Object> summary = new HashMap<>();

        Forms formsService;
        try {
            formsService = createFormsService();
        } catch (Exception e) {
            throw new RuntimeException("Không khởi tạo được Google Forms client: " + e.getMessage(), e);
        }

        int updated = 0;
        int skipped = 0;
        int errors = 0;

        List<Survey> surveys = surveyRepository.findAll();
        for (Survey survey : surveys) {
            String formUrl = survey.getFormUrl();
            String formId = extractFormIdFromUrl(formUrl);
            if (formId == null) {
                skipped++;
                continue;
            }

            try {
                int responseCount = countResponses(formsService, formId);
                if (!Objects.equals(survey.getResponseCount(), responseCount)) {
                    survey.setResponseCount(responseCount);
                    surveyRepository.save(survey);
                    updated++;
                }
            } catch (Exception e) {
                errors++;
                log.warn("Failed to sync response count for survey id={} formId={} message={}", survey.getId(), formId, e.getMessage());
            }
        }

        summary.put("updatedSurveys", updated);
        summary.put("skippedSurveys", skipped);
        summary.put("errorSurveys", errors);
        return summary;
    }

    private Forms createFormsService() throws IOException, GeneralSecurityException {
        try (InputStream inputStream = openCredentialsStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(List.of(
                            FormsScopes.FORMS_RESPONSES_READONLY,
                            FormsScopes.FORMS_BODY_READONLY));
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
            return new Forms.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer)
                    .setApplicationName(applicationName)
                    .build();
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        try {
            return credentialsResource.getInputStream();
        } catch (IOException primaryException) {
            Resource fallbackPrimary = new ClassPathResource("google-credentials.json");
            if (fallbackPrimary.exists()) {
                log.warn("Cannot open configured credentials at '{}'. Fallback to classpath:google-credentials.json",
                        credentialsResource);
                return fallbackPrimary.getInputStream();
            }

            Resource fallbackSecondary = new ClassPathResource("credentials.json");
            if (fallbackSecondary.exists()) {
                log.warn("Cannot open configured credentials at '{}'. Fallback to classpath:credentials.json",
                        credentialsResource);
                return fallbackSecondary.getInputStream();
            }

            throw new IOException("Khong tim thay Google credentials. "
                    + "Da thu: " + credentialsResource
                    + ", classpath:google-credentials.json, classpath:credentials.json", primaryException);
        }
    }

    private int countResponses(Forms formsService, String formId) throws IOException {
        int total = 0;
        String pageToken = null;
        do {
            var listRequest = formsService.forms().responses().list(formId)
                    .setPageSize(maxResponsesPerPage);
            if (pageToken != null) {
                listRequest.setPageToken(pageToken);
            }

            ListFormResponsesResponse response = listRequest.execute();
            List<FormResponse> responses = response.getResponses();
            if (responses != null) {
                total += responses.size();
            }
            pageToken = response.getNextPageToken();
        } while (pageToken != null && !pageToken.isBlank());
        return total;
    }

    private String extractFormIdFromUrl(String formUrl) {
        if (formUrl == null || formUrl.isBlank()) {
            return null;
        }

        try {
            // Handle both classic and new Forms URLs:
            // - https://docs.google.com/forms/d/FORM_ID/edit
            // - https://docs.google.com/forms/d/e/FORM_ID/viewform
            Pattern pattern = Pattern.compile("/forms/d/(?:e/)?([^/]+)");
            Matcher matcher = pattern.matcher(formUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("Could not extract formId from url '{}': {}", formUrl, e.getMessage());
        }
        return null;
    }

    // Helper methods
    private SurveyDTO toDTO(Survey survey) {
        String createdByName = "System";
        Long createdById = null;

        try {
            if (survey.getCreatedBy() != null) {
                createdById = survey.getCreatedBy().getId();
                if (survey.getCreatedBy().getUserInfo() != null
                        && survey.getCreatedBy().getUserInfo().getFullName() != null) {
                    createdByName = survey.getCreatedBy().getUserInfo().getFullName();
                } else if (survey.getCreatedBy().getEmail() != null) {
                    createdByName = survey.getCreatedBy().getEmail();
                }
            }
        } catch (Exception e) {
            log.warn("Error getting createdBy info for survey {}: {}", survey.getId(), e.getMessage());
        }

        return SurveyDTO.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .formUrl(survey.getFormUrl())
                .iconType(survey.getIconType())
                .iconColor(survey.getIconColor())
                .tag(survey.getTag())
                .tagColor(survey.getTagColor())
                .displayOrder(survey.getDisplayOrder())
                .responseCount(survey.getResponseCount())
                .isActive(survey.getIsActive())
                .showOnLanding(survey.getShowOnLanding())
                .createdByName(createdByName)
                .createdById(createdById)
                .createdAt(survey.getCreatedAt())
                .updatedAt(survey.getUpdatedAt())
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
