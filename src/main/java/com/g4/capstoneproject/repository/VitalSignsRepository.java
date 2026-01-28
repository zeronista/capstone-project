package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for VitalSigns entity
 * Provides data access methods for patient vital signs
 */
@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {

    /**
     * Find all vital signs for a specific patient
     */
    List<VitalSigns> findByPatientId(Long patientId);

    /**
     * Find all vital signs for a specific patient ordered by record date descending
     */
    List<VitalSigns> findByPatientIdOrderByRecordDateDesc(Long patientId);

    /**
     * Find the latest vital signs for a patient
     */
    @Query("SELECT v FROM VitalSigns v WHERE v.patient.id = :patientId ORDER BY v.recordDate DESC LIMIT 1")
    Optional<VitalSigns> findLatestByPatientId(@Param("patientId") Long patientId);

    /**
     * Find vital signs within a date range for a patient
     */
    @Query("SELECT v FROM VitalSigns v WHERE v.patient.id = :patientId " +
            "AND v.recordDate BETWEEN :startDate AND :endDate " +
            "ORDER BY v.recordDate DESC")
    List<VitalSigns> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find all vital signs recorded by a specific user (doctor/nurse)
     */
    List<VitalSigns> findByRecordedById(Long recordedById);

    /**
     * Count vital signs records for a patient
     */
    long countByPatientId(Long patientId);

    /**
     * Find recent vital signs (last N records) for a patient
     */
    @Query("SELECT v FROM VitalSigns v WHERE v.patient.id = :patientId ORDER BY v.recordDate DESC LIMIT :limit")
    List<VitalSigns> findRecentByPatientId(@Param("patientId") Long patientId, @Param("limit") int limit);

    /**
     * Delete all vital signs for a patient
     */
    void deleteByPatientId(Long patientId);
}
