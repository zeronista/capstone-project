package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.SurveyDTO;
import com.g4.capstoneproject.entity.Survey;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.SurveyRepository;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;

    /**
     * Get all surveys
     */
    public List<SurveyDTO> getAllSurveys() {
        return surveyRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active surveys
     */
    public List<SurveyDTO> getActiveSurveys() {
        return surveyRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get surveys for landing page
     */
    public List<SurveyDTO> getSurveysForLandingPage() {
        return surveyRepository.findByShowOnLandingTrueAndIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .showOnLanding(dto.getShowOnLanding() != null ? dto.getShowOnLanding() : true)
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
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSurveys", surveyRepository.count());
        stats.put("activeSurveys", surveyRepository.countActiveSurveys());
        stats.put("totalResponses", surveyRepository.sumTotalResponses());
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
