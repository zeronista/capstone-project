package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.VitalSigns;
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

    // ========== Vital Signs Endpoints ==========

    /**
     * Get all vital signs for a patient
     * GET /api/patients/{patientId}/vitals
     */
    @GetMapping("/{patientId}/vitals")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<VitalSigns>> getPatientVitalSigns(@PathVariable Long patientId) {
        List<VitalSigns> vitalSigns = patientService.getPatientVitalSigns(patientId);
        return ResponseEntity.ok(vitalSigns);
    }

    /**
     * Get latest vital signs for a patient
     * GET /api/patients/{patientId}/vitals/latest
     */
    @GetMapping("/{patientId}/vitals/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<VitalSigns> getLatestVitalSigns(@PathVariable Long patientId) {
        return patientService.getLatestVitalSigns(patientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Get recent vital signs (last N records)
     * GET /api/patients/{patientId}/vitals/recent?limit=5
     */
    @GetMapping("/{patientId}/vitals/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<VitalSigns>> getRecentVitalSigns(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "5") int limit) {
        List<VitalSigns> vitalSigns = patientService.getRecentVitalSigns(patientId, limit);
        return ResponseEntity.ok(vitalSigns);
    }

    /**
     * Get vital signs within date range
     * GET
     * /api/patients/{patientId}/vitals/range?start=2026-01-01T00:00:00&end=2026-01-31T23:59:59
     */
    @GetMapping("/{patientId}/vitals/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<VitalSigns>> getVitalSignsByDateRange(
            @PathVariable Long patientId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<VitalSigns> vitalSigns = patientService.getVitalSignsByDateRange(patientId, start, end);
        return ResponseEntity.ok(vitalSigns);
    }

    /**
     * Create vital signs for a patient
     * POST /api/patients/{patientId}/vitals
     */
    @PostMapping("/{patientId}/vitals")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<VitalSigns> createVitalSigns(
            @PathVariable Long patientId,
            @RequestBody VitalSigns vitalSigns,
            @RequestParam(required = false) Long recordedById) {
        try {
            VitalSigns created = patientService.createVitalSigns(patientId, vitalSigns, recordedById);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update vital signs
     * PUT /api/patients/{patientId}/vitals/{vitalSignsId}
     */
    @PutMapping("/{patientId}/vitals/{vitalSignsId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<VitalSigns> updateVitalSigns(
            @PathVariable Long patientId,
            @PathVariable Long vitalSignsId,
            @RequestBody VitalSigns vitalSigns) {
        vitalSigns.setId(vitalSignsId);
        VitalSigns updated = patientService.saveVitalSigns(vitalSigns);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete vital signs
     * DELETE /api/patients/{patientId}/vitals/{vitalSignsId}
     */
    @DeleteMapping("/{patientId}/vitals/{vitalSignsId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Void> deleteVitalSigns(
            @PathVariable Long patientId,
            @PathVariable Long vitalSignsId) {
        patientService.deleteVitalSigns(vitalSignsId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Count vital signs records for a patient
     * GET /api/patients/{patientId}/vitals/count
     */
    @GetMapping("/{patientId}/vitals/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<Map<String, Long>> countVitalSigns(@PathVariable Long patientId) {
        long count = patientService.countVitalSignsRecords(patientId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Get patient health summary
     * GET /api/patients/{patientId}/health-summary
     */
    @GetMapping("/{patientId}/health-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<PatientService.PatientHealthSummary> getHealthSummary(@PathVariable Long patientId) {
        PatientService.PatientHealthSummary summary = patientService.getPatientHealthSummary(patientId);
        return ResponseEntity.ok(summary);
    }
}
