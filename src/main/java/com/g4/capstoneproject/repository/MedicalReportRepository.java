package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {

    List<MedicalReport> findByPatientIdOrderByReportDateDesc(Long patientId);

    List<MedicalReport> findByPatientIdAndTypeOrderByReportDateDesc(Long patientId, MedicalReport.ReportType type);

    List<MedicalReport> findByCreatedByIdOrderByCreatedAtDesc(Long doctorId);
}
