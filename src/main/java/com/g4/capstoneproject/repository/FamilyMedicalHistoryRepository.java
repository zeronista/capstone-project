package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.FamilyMedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyMedicalHistoryRepository extends JpaRepository<FamilyMedicalHistory, Long> {

    List<FamilyMedicalHistory> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<FamilyMedicalHistory> findByPatientIdAndRelationship(Long patientId,
            FamilyMedicalHistory.Relationship relationship);

    List<FamilyMedicalHistory> findByCreatedByIdOrderByCreatedAtDesc(Long doctorId);
}
