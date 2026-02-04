package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.CheckupSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository cho CheckupSchedule entity
 */
@Repository
public interface CheckupScheduleRepository extends JpaRepository<CheckupSchedule, Long> {

    /**
     * Tìm lịch tái khám theo treatment plan
     */
    List<CheckupSchedule> findByTreatmentPlanId(Long treatmentPlanId);

    /**
     * Tìm lịch tái khám theo bệnh nhân
     */
    List<CheckupSchedule> findByPatientId(Long patientId);

    /**
     * Tìm lịch tái khám theo bác sĩ
     */
    List<CheckupSchedule> findByDoctorId(Long doctorId);

    /**
     * Tìm lịch tái khám theo trạng thái
     */
    List<CheckupSchedule> findByStatus(CheckupSchedule.CheckupStatus status);

    /**
     * Tìm lịch tái khám theo khoảng thời gian
     */
    @Query("SELECT cs FROM CheckupSchedule cs WHERE cs.scheduledDate BETWEEN :startDate AND :endDate ORDER BY cs.scheduledDate ASC")
    List<CheckupSchedule> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Tìm lịch tái khám sắp tới của bệnh nhân
     */
    @Query("SELECT cs FROM CheckupSchedule cs WHERE cs.patient.id = :patientId AND cs.scheduledDate >= :today AND cs.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY cs.scheduledDate ASC")
    List<CheckupSchedule> findUpcomingByPatientId(@Param("patientId") Long patientId, @Param("today") LocalDate today);

    /**
     * Tìm lịch tái khám sắp tới của bác sĩ
     */
    @Query("SELECT cs FROM CheckupSchedule cs WHERE cs.doctor.id = :doctorId AND cs.scheduledDate >= :today AND cs.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY cs.scheduledDate ASC")
    List<CheckupSchedule> findUpcomingByDoctorId(@Param("doctorId") Long doctorId, @Param("today") LocalDate today);

    /**
     * Đếm số lịch tái khám theo treatment plan
     */
    long countByTreatmentPlanId(Long treatmentPlanId);

    /**
     * Đếm số lịch tái khám theo trạng thái
     */
    long countByStatus(CheckupSchedule.CheckupStatus status);

    /**
     * Tìm lịch tái khám quá hạn (đã qua ngày nhưng chưa hoàn thành)
     */
    @Query("SELECT cs FROM CheckupSchedule cs WHERE cs.scheduledDate < :today AND cs.status NOT IN ('COMPLETED', 'CANCELLED', 'NO_SHOW') ORDER BY cs.scheduledDate ASC")
    List<CheckupSchedule> findOverdue(@Param("today") LocalDate today);
}
