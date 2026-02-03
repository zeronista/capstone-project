package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Patient Management
 * Provides endpoints for patient data and vital signs
 * Separate from PatientController which handles patient portal session-based
 * endpoints
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientManagementController {

    private final PatientService patientService;

    // ========== Patient Endpoints ==========

    /**
     * Get all patients (Admin/Receptionist/Doctor)
     * GET /api/patients
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<User>> getAllPatients() {
        List<User> patients = patientService.getAllActivePatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Get all patients as simplified list for call center
     * GET /api/patients/list
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getPatientsForCallCenter() {
        List<User> patients = patientService.getAllActivePatientsWithInfo();
        List<Map<String, Object>> result = patients.stream().map(p -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", p.getId());
            map.put("email", p.getEmail());
            map.put("phoneNumber", p.getPhoneNumber());
            map.put("fullName", p.getFullName());
            map.put("dateOfBirth", p.getDateOfBirth());
            map.put("gender", p.getGender() != null ? p.getGender().name() : null);
            map.put("address", p.getAddress());
            map.put("stringeeUserId", "user_" + p.getId());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Get patient by ID
     * GET /api/patients/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<Map<String, Object>> getPatientById(@PathVariable Long id) {
        return patientService.getPatientByIdWithInfo(id)
                .map(p -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", p.getId());
                    map.put("email", p.getEmail());
                    map.put("phoneNumber", p.getPhoneNumber());
                    map.put("fullName", p.getFullName());
                    map.put("dateOfBirth", p.getDateOfBirth());
                    map.put("gender", p.getGender() != null ? p.getGender().name() : null);
                    map.put("address", p.getAddress());
                    map.put("stringeeUserId", "user_" + p.getId());
                    map.put("createdAt", p.getCreatedAt());
                    return ResponseEntity.ok(map);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search patients by keyword
     * GET /api/patients/search?q=keyword
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<List<User>> searchPatients(@RequestParam("q") String keyword) {
        List<User> patients = patientService.searchPatients(keyword);
        return ResponseEntity.ok(patients);
    }

    /**
     * Get patients by doctor ID
     * GET /api/patients/by-doctor/{doctorId}
     */
    @GetMapping("/by-doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<User>> getPatientsByDoctor(@PathVariable Long doctorId) {
        List<User> patients = patientService.getPatientsByDoctorId(doctorId);
        return ResponseEntity.ok(patients);
    }

    /**
     * Get active patients of a doctor
     * GET /api/patients/by-doctor/{doctorId}/active
     */
    @GetMapping("/by-doctor/{doctorId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<User>> getActivePatientsOfDoctor(@PathVariable Long doctorId) {
        List<User> patients = patientService.getActivePatientsOfDoctor(doctorId);
        return ResponseEntity.ok(patients);
    }

    /**
     * Count patients by doctor
     * GET /api/patients/by-doctor/{doctorId}/count
     */
    @GetMapping("/by-doctor/{doctorId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Map<String, Long>> countPatientsByDoctor(@PathVariable Long doctorId) {
        long count = patientService.countPatientsByDoctorId(doctorId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // ========== Vital Signs Endpoints - DEPRECATED ==========
    /*
     * NOTE: All VitalSigns endpoints removed - VitalSigns entity deprecated in schema v4.0
     * 
     * Vital signs data is now stored as JSONB in health_forecasts.vital_signs_snapshot
     * These endpoints have been removed:
     * - GET /api/patients/{patientId}/vitals
     * - GET /api/patients/{patientId}/vitals/latest
     * - GET /api/patients/{patientId}/vitals/recent
     * - GET /api/patients/{patientId}/vitals/range
     * - POST /api/patients/{patientId}/vitals
     * - PUT /api/patients/{patientId}/vitals/{vitalSignsId}
     * - DELETE /api/patients/{patientId}/vitals/{vitalSignsId}
     * - GET /api/patients/{patientId}/vitals/count
     * 
     * Vital signs functionality should be reimplemented using HealthForecast entity.
     */

    /*
     * NOTE: Method removed - depends on VitalSigns entity which was deprecated
     * GET /api/patients/{patientId}/health-summary
     * 
     * This endpoint should be reimplemented using HealthForecast entity.
     */
}
