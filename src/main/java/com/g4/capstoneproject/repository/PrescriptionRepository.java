package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.Prescription;
import com.g4.capstoneproject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository cho Prescription entity
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    /**
     * Tìm đơn thuốc theo bệnh nhân
     */
    List<Prescription> findByPatient(User patient);

    /**
     * Tìm đơn thuốc theo ID bệnh nhân
     */
    List<Prescription> findByPatientId(Long patientId);

    /**
     * Tìm đơn thuốc theo bác sĩ
     */
    List<Prescription> findByDoctor(User doctor);

    /**
     * Tìm đơn thuốc theo ID bác sĩ
     */
    List<Prescription> findByDoctorId(Long doctorId);

    /**
     * Tìm đơn thuốc theo trạng thái
     */
    List<Prescription> findByStatus(Prescription.PrescriptionStatus status);

    /**
     * Đếm đơn thuốc theo trạng thái
     */
    long countByStatus(Prescription.PrescriptionStatus status);

    /**
     * Tìm đơn thuốc theo khoảng thời gian
     */
    List<Prescription> findByPrescriptionDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Tìm đơn thuốc đang hoạt động của bệnh nhân
     */
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId AND p.status = 'ACTIVE' ORDER BY p.prescriptionDate DESC")
    List<Prescription> findActiveByPatientId(@Param("patientId") Long patientId);

    /**
     * Tìm đơn thuốc trong khoảng thời gian
     */
    @Query("SELECT p FROM Prescription p WHERE p.prescriptionDate BETWEEN :startDate AND :endDate ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Tìm đơn thuốc mới nhất của bệnh nhân
     */
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId ORDER BY p.prescriptionDate DESC")
    List<Prescription> findLatestByPatientId(@Param("patientId") Long patientId, Pageable pageable);

    /**
     * Phân trang đơn thuốc
     */
    Page<Prescription> findAllByOrderByPrescriptionDateDesc(Pageable pageable);

    /**
     * Đếm đơn thuốc của bệnh nhân
     */
    long countByPatientId(Long patientId);

    /**
     * Đếm đơn thuốc của bệnh nhân (dùng entity)
     */
    long countByPatient(User patient);

    /**
     * Tìm đơn thuốc theo bệnh nhân sắp xếp theo ngày
     */
    List<Prescription> findByPatientOrderByPrescriptionDateDesc(User patient);

    /**
     * Đếm đơn thuốc của bác sĩ
     */
    long countByDoctorId(Long doctorId);

    /**
     * Tìm đơn thuốc theo patient ID sắp xếp theo ngày mới nhất
     */
    List<Prescription> findByPatientIdOrderByPrescriptionDateDesc(Long patientId);
}
