package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.GoogleFormSyncRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoogleFormSyncRecordRepository extends JpaRepository<GoogleFormSyncRecord, Long> {

    Optional<GoogleFormSyncRecord> findByFormIdAndResponseId(String formId, String responseId);

    List<GoogleFormSyncRecord> findBySyncStatusAndPatientIsNotNullOrderBySyncedAtDesc(
            GoogleFormSyncRecord.SyncStatus syncStatus,
            Pageable pageable);

    boolean existsByPatientId(Long patientId);
}
