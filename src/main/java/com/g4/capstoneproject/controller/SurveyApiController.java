package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.SurveyDTO;
import com.g4.capstoneproject.service.SurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Slf4j
public class SurveyApiController {

    private final SurveyService surveyService;

    /**
     * Get all surveys
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSurveys() {
        try {
            List<SurveyDTO> surveys = surveyService.getAllSurveys();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", surveys);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting surveys", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Get active surveys
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveSurveys() {
        try {
            List<SurveyDTO> surveys = surveyService.getActiveSurveys();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", surveys);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting active surveys", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Get surveys for landing page (public endpoint)
     */
    @GetMapping("/landing")
    public ResponseEntity<Map<String, Object>> getSurveysForLanding() {
        try {
            List<SurveyDTO> surveys = surveyService.getSurveysForLandingPage();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", surveys);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting landing surveys", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Get survey statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSurveyStats() {
        try {
            Map<String, Object> stats = surveyService.getSurveyStats();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting survey stats", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Sync survey response counts from Google Forms.
     */
    @PostMapping("/sync-responses")
    public ResponseEntity<Map<String, Object>> syncSurveyResponses() {
        try {
            Map<String, Object> data = surveyService.syncResponseCountsFromGoogleForms();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error syncing survey responses from Google Forms", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Get survey by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSurveyById(@PathVariable Long id) {
        try {
            SurveyDTO survey = surveyService.getSurveyById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", survey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting survey: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Create new survey
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSurvey(@RequestBody SurveyDTO dto) {
        try {
            SurveyDTO created = surveyService.createSurvey(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo survey thành công");
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating survey", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi tạo survey: " + e.getMessage()));
        }
    }

    /**
     * Update survey
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSurvey(@PathVariable Long id, @RequestBody SurveyDTO dto) {
        try {
            SurveyDTO updated = surveyService.updateSurvey(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật survey thành công");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating survey: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi cập nhật survey: " + e.getMessage()));
        }
    }

    /**
     * Delete survey
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSurvey(@PathVariable Long id) {
        try {
            surveyService.deleteSurvey(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa survey thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting survey: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi xóa survey: " + e.getMessage()));
        }
    }

    /**
     * Toggle survey active status
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleSurveyStatus(@PathVariable Long id) {
        try {
            SurveyDTO updated = surveyService.toggleSurveyStatus(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", updated.getIsActive() ? "Đã hiển thị survey" : "Đã ẩn survey");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling survey status: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Track survey response (can be called from landing page)
     */
    @PostMapping("/{id}/track")
    public ResponseEntity<Map<String, Object>> trackResponse(@PathVariable Long id) {
        try {
            surveyService.incrementResponseCount(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error tracking survey response: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}
