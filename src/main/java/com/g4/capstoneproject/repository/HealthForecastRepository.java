package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.HealthForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for HealthForecast entity
 * Provides data access methods for patient health forecasts
 */
@Repository
public interface HealthForecastRepository extends JpaRepository<HealthForecast, Long> {

    /**
     * Find all forecasts for a specific patient
     */
    List<HealthForecast> findByPatientId(Long patientId);

    /**
     * Find all forecasts for a specific patient ordered by date descending
     */
    List<HealthForecast> findByPatientIdOrderByForecastDateDesc(Long patientId);

    /**
     * Find the latest active forecast for a patient
     */
    @Query("SELECT f FROM HealthForecast f WHERE f.patient.id = :patientId " +
            "AND f.status = 'ACTIVE' ORDER BY f.forecastDate DESC LIMIT 1")
    Optional<HealthForecast> findLatestActiveByPatientId(@Param("patientId") Long patientId);

    /**
     * Find all forecasts for a patient within a date range
     */
    @Query("SELECT f FROM HealthForecast f WHERE f.patient.id = :patientId " +
            "AND f.forecastDate BETWEEN :startDate AND :endDate " +
            "ORDER BY f.forecastDate DESC")
    List<HealthForecast> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find all forecasts by status
     */
    List<HealthForecast> findByStatus(HealthForecast.ForecastStatus status);

    /**
     * Find all high-risk forecasts using JSON query
     * Note: This requires MySQL 5.7+ or MariaDB 10.2+ with JSON support
     */
    @Query(value = "SELECT * FROM health_forecasts " +
            "WHERE status = 'ACTIVE' " +
            "AND (JSON_EXTRACT(risk_scores, '$.overallRisk') = 'HIGH' " +
            "OR JSON_EXTRACT(risk_scores, '$.overallRisk') = 'VERY_HIGH' " +
            "OR CAST(JSON_EXTRACT(risk_scores, '$.cardiovascularRisk') AS DECIMAL(5,2)) > 20.0)", nativeQuery = true)
    List<HealthForecast> findHighRiskForecasts();

    /**
     * Find all active forecasts for a patient
     */
    @Query("SELECT f FROM HealthForecast f WHERE f.patient.id = :patientId " +
            "AND f.status = 'ACTIVE' ORDER BY f.forecastDate DESC")
    List<HealthForecast> findActiveByPatientId(@Param("patientId") Long patientId);

    /**
     * Find recent forecasts (last N records) for a patient
     */
    @Query("SELECT f FROM HealthForecast f WHERE f.patient.id = :patientId " +
            "ORDER BY f.forecastDate DESC LIMIT :limit")
    List<HealthForecast> findRecentByPatientId(@Param("patientId") Long patientId, @Param("limit") int limit);

    /**
     * Count forecasts for a patient
     */
    long countByPatientId(Long patientId);

    /**
     * Count active forecasts for a patient
     */
    @Query("SELECT COUNT(f) FROM HealthForecast f WHERE f.patient.id = :patientId AND f.status = 'ACTIVE'")
    long countActiveByPatientId(@Param("patientId") Long patientId);

    /**
     * Find all forecasts created by a specific doctor
     */
    List<HealthForecast> findByCreatedById(Long doctorId);

    /**
     * Find forecasts that need to be updated (older than X days)
     */
    @Query("SELECT f FROM HealthForecast f WHERE f.status = 'ACTIVE' " +
            "AND f.forecastDate < :thresholdDate")
    List<HealthForecast> findOutdatedForecasts(@Param("thresholdDate") LocalDate thresholdDate);

    /**
     * Mark all active forecasts for a patient as outdated except the latest one
     */
    @Query("UPDATE HealthForecast f SET f.status = 'OUTDATED', f.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE f.patient.id = :patientId AND f.status = 'ACTIVE' AND f.id != :exceptId")
    void markOthersAsOutdated(@Param("patientId") Long patientId, @Param("exceptId") Long exceptId);

    /**
     * Delete all forecasts for a patient
     */
    void deleteByPatientId(Long patientId);

    /**
     * Find forecasts with cardiovascular risk above threshold
     */
    @Query(value = "SELECT * FROM health_forecasts " +
            "WHERE status = 'ACTIVE' " +
            "AND CAST(JSON_EXTRACT(risk_scores, '$.cardiovascularRisk') AS DECIMAL(5,2)) >= :threshold " +
            "ORDER BY CAST(JSON_EXTRACT(risk_scores, '$.cardiovascularRisk') AS DECIMAL(5,2)) DESC", nativeQuery = true)
    List<HealthForecast> findByCardiovascularRiskAbove(@Param("threshold") double threshold);
}
