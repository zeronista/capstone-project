package com.g4.capstoneproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleFormsSyncScheduler {

    private final GoogleFormsSyncService googleFormsSyncService;

    @Value("${google.forms.sync.enabled:true}")
    private boolean syncEnabled;

    @Value("${google.forms.sync.scheduled-enabled:true}")
    private boolean scheduledEnabled;

    @Scheduled(cron = "${google.forms.sync.cron:0 0/15 * * * *}")
    public void syncGoogleFormsBySchedule() {
        if (!syncEnabled || !scheduledEnabled) {
            return;
        }

        try {
            Map<String, Object> result = googleFormsSyncService.syncPatientsFromConfiguredForms("scheduled");
            log.info("Scheduled Google Forms sync completed: {}", result);
        } catch (Exception ex) {
            log.error("Scheduled Google Forms sync failed: {}", ex.getMessage(), ex);
        }
    }
}
