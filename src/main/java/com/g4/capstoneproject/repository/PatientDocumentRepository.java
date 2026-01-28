package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.PatientDocument;
import com.g4.capstoneproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho PatientDocument entity
 */
@Repository
public interface PatientDocumentRepository extends JpaRepository<PatientDocument, Long> {
    
    /**
     * Tìm tài liệu theo bệnh nhân
     */
    List<PatientDocument> findByPatient(User patient);
    
    /**
     * Tìm tài liệu theo ID bệnh nhân
     */
    List<PatientDocument> findByPatientId(Long patientId);
    
    /**
     * Tìm theo bệnh nhân và loại tài liệu
     */
    List<PatientDocument> findByPatientAndDocumentType(User patient, PatientDocument.DocumentType documentType);
    
    /**
     * Tìm theo bệnh nhân ID và loại tài liệu
     */
    List<PatientDocument> findByPatientIdAndDocumentType(Long patientId, PatientDocument.DocumentType documentType);
    
    /**
     * Đếm số tài liệu của bệnh nhân
     */
    long countByPatientId(Long patientId);
    
    /**
     * Tìm tài liệu mới nhất của bệnh nhân theo loại
     */
    @Query("SELECT pd FROM PatientDocument pd WHERE pd.patient.id = :patientId AND pd.documentType = :type ORDER BY pd.uploadDate DESC")
    List<PatientDocument> findLatestByPatientAndType(@Param("patientId") Long patientId, @Param("type") PatientDocument.DocumentType type);
}
