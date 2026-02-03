package com.g4.capstoneproject.service;

import com.g4.capstoneproject.entity.HealthForecast;
import com.g4.capstoneproject.repository.HealthForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service for Health Forecast Management
 * Handles business logic for health risk assessment and forecasting
 * 
 * NOTE: VitalSigns entity has been removed in schema v4.0
 * Risk calculation methods that depend on VitalSigns have been removed.
 * These methods need to be reimplemented using vitalSignsSnapshot (JSONB) from HealthForecast entity.
 */
@Service
@RequiredArgsConstructor
public class HealthForecastService {

    private final HealthForecastRepository healthForecastRepository;

    // ========== CRUD Operations ==========

    /**
     * Get all forecasts for a patient
     */
    public List<HealthForecast> getForecastsByPatientId(Long patientId) {
        return healthForecastRepository.findByPatientIdOrderByForecastDateDesc(patientId);
    }

    /**
     * Get forecast by ID
     */
    public HealthForecast getForecastById(Long id) {
        return healthForecastRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forecast not found with ID: " + id));
    }

    /**
     * Get latest active forecast for a patient
     */
    public Optional<HealthForecast> getLatestActiveForecast(Long patientId) {
        return healthForecastRepository.findLatestActiveByPatientId(patientId);
    }

    /**
     * Get recent forecasts (last N records)
     */
    public List<HealthForecast> getRecentForecasts(Long patientId, int limit) {
        return healthForecastRepository.findRecentByPatientId(patientId, limit);
    }

    /**
     * Get forecasts within date range
     */
    public List<HealthForecast> getForecastsByDateRange(Long patientId, LocalDate startDate, LocalDate endDate) {
        return healthForecastRepository.findByPatientIdAndDateRange(patientId, startDate, endDate);
    }

    /**
     * Create or update health forecast
     */
    @Transactional
    public HealthForecast saveForecast(HealthForecast forecast) {
        return healthForecastRepository.save(forecast);
    }

    /**
     * Delete forecast by ID
     */
    @Transactional
    public void deleteForecast(Long id) {
        healthForecastRepository.deleteById(id);
    }

    /**
     * Mark old forecasts as outdated when creating a new one
     */
    @Transactional
    public void markOldForecastsAsOutdated(Long patientId) {
        List<HealthForecast> activeForecasts = healthForecastRepository
                .findActiveByPatientId(patientId);
        
        for (HealthForecast forecast : activeForecasts) {
            forecast.markAsOutdated();
            healthForecastRepository.save(forecast);
        }
    }
}
