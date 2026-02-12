package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.PatientHealthProfileRequest;
import com.g4.capstoneproject.entity.PatientHealthProfile;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.PatientHealthProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST API cho tab "Sức khỏe" của bệnh nhân trong trang profile.
 */
@RestController
@RequestMapping("/api/patient/health-profile")
@RequiredArgsConstructor
@Slf4j
public class PatientHealthProfileController {

    private final PatientHealthProfileService healthProfileService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealthProfile(
            jakarta.servlet.http.HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            User patient = userRepository.findById(userId).orElse(null);

            if (patient == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }

            Optional<PatientHealthProfile> profileOpt = healthProfileService.getByUserId(patient.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("bloodType", profileOpt.map(PatientHealthProfile::getBloodType).orElse(null));
            data.put("heightCm", profileOpt.map(PatientHealthProfile::getHeightCm).orElse(null));
            data.put("weightKg", profileOpt.map(PatientHealthProfile::getWeightKg).orElse(null));
            data.put("allergies", profileOpt.map(PatientHealthProfile::getAllergies).orElse(null));
            data.put("chronicDiseases", profileOpt.map(PatientHealthProfile::getChronicDiseases).orElse(null));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting patient health profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi hệ thống. Vui lòng thử lại sau"));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> updateHealthProfile(
            @RequestBody PatientHealthProfileRequest request,
            jakarta.servlet.http.HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Chưa đăng nhập"));
            }
            User patient = userRepository.findById(userId).orElse(null);

            if (patient == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Không tìm thấy người dùng"));
            }

            PatientHealthProfile profile = healthProfileService.upsertHealthProfile(patient.getId(), request);

            Map<String, Object> data = new HashMap<>();
            data.put("bloodType", profile.getBloodType());
            data.put("heightCm", profile.getHeightCm());
            data.put("weightKg", profile.getWeightKg());
            data.put("allergies", profile.getAllergies());
            data.put("chronicDiseases", profile.getChronicDiseases());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật thông tin sức khỏe thành công");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.warn("Validation error when updating health profile: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        } catch (Exception e) {
            log.error("Error updating patient health profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi hệ thống. Vui lòng thử lại sau"));
        }
    }
}

