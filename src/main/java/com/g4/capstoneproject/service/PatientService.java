package com.g4.capstoneproject.service;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.VitalSigns;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.repository.VitalSignsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Patient Management
 * Handles business logic for patient-related operations
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final UserRepository userRepository;
    private final VitalSignsRepository vitalSignsRepository;

    // ========== Patient CRUD Operations ==========

    /**
     * Get all patients
     * Cached for 30 minutes to reduce database load
     */
    @Cacheable(value = "patients", key = "'all'")
    public List<User> getAllPatients() {
        return userRepository.findByRole(User.UserRole.PATIENT);
    }

    /**
     * Get all active patients
     * Cached for 30 minutes to reduce database load
     */
    @Cacheable(value = "patients", key = "'active'")
    public List<User> getAllActivePatients() {
        return userRepository.findByRoleAndIsActiveTrue(User.UserRole.PATIENT);
    }

    /**
     * Get patient by ID
     * Cached for 30 minutes
     */
    @Cacheable(value = "patients", key = "#patientId")
    public Optional<User> getPatientById(Long patientId) {
        return userRepository.findById(patientId)
                .filter(user -> user.getRole() == User.UserRole.PATIENT);
    }

    /**
     * Get patient by ID with user info (eager fetch)
     */
    public Optional<User> getPatientByIdWithInfo(Long patientId) {
        return userRepository.findByIdWithUserInfo(patientId)
                .filter(user -> user.getRole() == User.UserRole.PATIENT);
    }

    /**
     * Get patients by doctor ID (from treatment plans)
     * Cached for 30 minutes
     */
    @Cacheable(value = "patients", key = "'doctor-' + #doctorId")
    public List<User> getPatientsByDoctorId(Long doctorId) {
        return userRepository.findPatientsByDoctorId(doctorId);
    }

    /**
     * Get active patients of a doctor
     */
    public List<User> getActivePatientsOfDoctor(Long doctorId) {
        return userRepository.findActivePatientsOfDoctor(doctorId);
    }

    /**
     * Search patients by keyword
     */
    public List<User> searchPatients(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActivePatients();
        }
        return userRepository.searchPatients(keyword.trim());
    }

    /**
     * Count patients by doctor ID
     */
    public long countPatientsByDoctorId(Long doctorId) {
        return userRepository.countPatientsByDoctorId(doctorId);
    }

    // ========== Vital Signs Operations ==========

    /**
     * Get all vital signs for a patient
     */
    public List<VitalSigns> getPatientVitalSigns(Long patientId) {
        return vitalSignsRepository.findByPatientIdOrderByRecordDateDesc(patientId);
    }

    /**
     * Get latest vital signs for a patient
     */
    public Optional<VitalSigns> getLatestVitalSigns(Long patientId) {
        return vitalSignsRepository.findLatestByPatientId(patientId);
    }

    /**
     * Get recent vital signs (last N records)
     */
    public List<VitalSigns> getRecentVitalSigns(Long patientId, int limit) {
        return vitalSignsRepository.findRecentByPatientId(patientId, limit);
    }

    /**
     * Get vital signs within date range
     */
    public List<VitalSigns> getVitalSignsByDateRange(Long patientId, LocalDateTime startDate, LocalDateTime endDate) {
        return vitalSignsRepository.findByPatientIdAndDateRange(patientId, startDate, endDate);
    }

    /**
     * Create or update vital signs
     */
    @Transactional
    public VitalSigns saveVitalSigns(VitalSigns vitalSigns) {
        // Calculate BMI if weight and height are provided
        vitalSigns.calculateBMI();
        return vitalSignsRepository.save(vitalSigns);
    }

    /**
     * Create vital signs for a patient
     */
    @Transactional
    public VitalSigns createVitalSigns(Long patientId, VitalSigns vitalSigns, Long recordedById) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

        if (patient.getRole() != User.UserRole.PATIENT) {
            throw new RuntimeException("User is not a patient");
        }

        vitalSigns.setPatient(patient);

        if (recordedById != null) {
            User recordedBy = userRepository.findById(recordedById)
                    .orElseThrow(() -> new RuntimeException("Recorded by user not found"));
            vitalSigns.setRecordedBy(recordedBy);
        }

        if (vitalSigns.getRecordDate() == null) {
            vitalSigns.setRecordDate(LocalDateTime.now());
        }

        // Calculate BMI
        vitalSigns.calculateBMI();

        return vitalSignsRepository.save(vitalSigns);
    }

    /**
     * Delete vital signs by ID
     */
    @Transactional
    public void deleteVitalSigns(Long vitalSignsId) {
        vitalSignsRepository.deleteById(vitalSignsId);
    }

    /**
     * Count vital signs records for a patient
     */
    public long countVitalSignsRecords(Long patientId) {
        return vitalSignsRepository.countByPatientId(patientId);
    }

    // ========== Patient Statistics ==========

    /**
     * Check if patient has any vital signs records
     */
    public boolean hasVitalSigns(Long patientId) {
        return vitalSignsRepository.countByPatientId(patientId) > 0;
    }

    /**
     * Get patient health summary
     */
    public PatientHealthSummary getPatientHealthSummary(Long patientId) {
        Optional<VitalSigns> latestVitalSigns = getLatestVitalSigns(patientId);
        long recordsCount = countVitalSignsRecords(patientId);

        return PatientHealthSummary.builder()
                .patientId(patientId)
                .latestVitalSigns(latestVitalSigns.orElse(null))
                .totalRecords(recordsCount)
                .hasVitalSigns(recordsCount > 0)
                .build();
    }

    // ========== Helper DTO ==========

    @lombok.Data
    @lombok.Builder
    public static class PatientHealthSummary {
        private Long patientId;
        private VitalSigns latestVitalSigns;
        private long totalRecords;
        private boolean hasVitalSigns;
    }
}
