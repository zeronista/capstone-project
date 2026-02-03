package com.g4.capstoneproject.service;

import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Get all active patients with UserInfo (for call center)
     * Returns patients with their personal information loaded
     */
    @Transactional(readOnly = true)
    public List<User> getAllActivePatientsWithInfo() {
        return userRepository.findAllActivePatientsWithUserInfo();
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
}
