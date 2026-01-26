package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.TreatmentPlan;
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
 * Repository cho TreatmentPlan entity
 */
@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Long> {
    
    /**
     * Tìm kế hoạch theo bệnh nhân
     */
    List<TreatmentPlan> findByPatient(User patient);
    
    /**
     * Tìm kế hoạch theo ID bệnh nhân
     */
    List<TreatmentPlan> findByPatientId(Long patientId);
    
    /**
     * Tìm kế hoạch theo bác sĩ
     */
    List<TreatmentPlan> findByDoctor(User doctor);
    
    /**
     * Tìm kế hoạch theo ID bác sĩ
     */
    List<TreatmentPlan> findByDoctorId(Long doctorId);
    
    /**
     * Tìm kế hoạch theo trạng thái
     */
    List<TreatmentPlan> findByStatus(TreatmentPlan.PlanStatus status);
    
    /**
     * Tìm kế hoạch đang hoạt động của bệnh nhân
     */
    @Query("SELECT tp FROM TreatmentPlan tp WHERE tp.patient.id = :patientId AND tp.status = 'ACTIVE'")
    List<TreatmentPlan> findActiveByPatientId(@Param("patientId") Long patientId);
    
    /**
     * Tìm kế hoạch được AI gợi ý
     */
    List<TreatmentPlan> findByAiSuggestedTrue();
    
    /**
     * Tìm kế hoạch sắp kết thúc
     */
    @Query("SELECT tp FROM TreatmentPlan tp WHERE tp.status = 'ACTIVE' AND tp.expectedEndDate <= :date")
    List<TreatmentPlan> findPlanEndingSoon(@Param("date") LocalDate date);
    
    /**
     * Phân trang kế hoạch
     */
    Page<TreatmentPlan> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Đếm kế hoạch đang hoạt động của bác sĩ
     */
    long countByDoctorIdAndStatus(Long doctorId, TreatmentPlan.PlanStatus status);
    
    /**
     * Đếm kế hoạch của bệnh nhân
     */
    long countByPatientId(Long patientId);
}
