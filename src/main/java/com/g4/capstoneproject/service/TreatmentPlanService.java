package com.g4.capstoneproject.service;

import com.g4.capstoneproject.entity.CheckupSchedule;
import com.g4.capstoneproject.entity.TreatmentPlan;
import com.g4.capstoneproject.entity.TreatmentPlanItem;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.CheckupScheduleRepository;
import com.g4.capstoneproject.repository.TreatmentPlanRepository;
import com.g4.capstoneproject.repository.TreatmentPlanItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý lộ trình điều trị
 * Refactored to use database in Phase 2
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TreatmentPlanService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentPlanItemRepository treatmentPlanItemRepository;
    private final CheckupScheduleRepository checkupScheduleRepository;

    /**
     * Lấy tất cả treatment plan
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlan> getAllTreatmentPlans() {
        return treatmentPlanRepository.findAll();
    }

    /**
     * Lấy treatment plan theo ID
     */
    @Transactional(readOnly = true)
    public Optional<TreatmentPlan> getTreatmentPlanById(Long id) {
        return treatmentPlanRepository.findById(id);
    }

    /**
     * Lấy treatment plan theo bệnh nhân
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlan> getTreatmentPlansByPatientId(Long patientId) {
        return treatmentPlanRepository.findByPatientId(patientId);
    }

    /**
     * Lấy treatment plan theo bác sĩ
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlan> getTreatmentPlansByDoctorId(Long doctorId) {
        return treatmentPlanRepository.findByDoctorId(doctorId);
    }

    /**
     * Lọc treatment plan theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlan> getTreatmentPlansByStatus(TreatmentPlan.PlanStatus status) {
        return treatmentPlanRepository.findByStatus(status);
    }

    /**
     * Lấy treatment plan active
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlan> getActiveTreatmentPlans() {
        return treatmentPlanRepository.findByStatus(TreatmentPlan.PlanStatus.ACTIVE);
    }

    /**
     * Tạo treatment plan mới
     */
    public TreatmentPlan createTreatmentPlan(TreatmentPlan plan) {
        return treatmentPlanRepository.save(plan);
    }

    /**
     * Cập nhật treatment plan
     */
    public TreatmentPlan updateTreatmentPlan(Long id, TreatmentPlan updatedPlan) {
        return treatmentPlanRepository.findById(id)
                .map(existing -> {
                    existing.setDiagnosis(updatedPlan.getDiagnosis());
                    existing.setStatus(updatedPlan.getStatus());
                    return treatmentPlanRepository.save(existing);
                })
                .orElse(null);
    }

    /**
     * Xóa treatment plan
     */
    public boolean deleteTreatmentPlan(Long id) {
        if (treatmentPlanRepository.existsById(id)) {
            treatmentPlanRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Thống kê treatment plan
     */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return treatmentPlanRepository.count();
    }

    @Transactional(readOnly = true)
    public long getActiveCount() {
        return treatmentPlanRepository.findByStatus(TreatmentPlan.PlanStatus.ACTIVE).size();
    }

    @Transactional(readOnly = true)
    public long getCompletedCount() {
        return treatmentPlanRepository.findByStatus(TreatmentPlan.PlanStatus.COMPLETED).size();
    }

    /**
     * Thêm item vào treatment plan
     */
    public TreatmentPlanItem addItem(Long planId, TreatmentPlanItem item) {
        return treatmentPlanRepository.findById(planId)
                .map(plan -> {
                    item.setTreatmentPlan(plan);
                    return treatmentPlanItemRepository.save(item);
                })
                .orElse(null);
    }

    /**
     * Lấy items của treatment plan
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlanItem> getTreatmentPlanItems(Long planId) {
        return treatmentPlanItemRepository.findByTreatmentPlanId(planId);
    }

    /**
     * Cập nhật trạng thái item
     */
    public TreatmentPlanItem updateItemStatus(Long itemId, TreatmentPlanItem.ItemStatus status) {
        return treatmentPlanItemRepository.findById(itemId)
                .map(item -> {
                    item.setStatus(status);
                    return treatmentPlanItemRepository.save(item);
                })
                .orElse(null);
    }

    // ==================== CHECKUP SCHEDULE MANAGEMENT ====================

    /**
     * Tạo lịch tái khám cho treatment plan
     */
    public CheckupSchedule createCheckupSchedule(Long treatmentPlanId, CheckupSchedule checkupSchedule, User doctor) {
        return treatmentPlanRepository.findById(treatmentPlanId)
                .map(plan -> {
                    checkupSchedule.setTreatmentPlan(plan);
                    checkupSchedule.setPatient(plan.getPatient());
                    checkupSchedule.setDoctor(doctor);
                    return checkupScheduleRepository.save(checkupSchedule);
                })
                .orElse(null);
    }

    /**
     * Lấy danh sách lịch tái khám của treatment plan
     */
    @Transactional(readOnly = true)
    public List<CheckupSchedule> getCheckupSchedulesByPlanId(Long treatmentPlanId) {
        return checkupScheduleRepository.findByTreatmentPlanId(treatmentPlanId);
    }

    /**
     * Lấy lịch tái khám theo ID
     */
    @Transactional(readOnly = true)
    public CheckupSchedule getCheckupScheduleById(Long checkupId) {
        return checkupScheduleRepository.findById(checkupId).orElse(null);
    }

    /**
     * Cập nhật trạng thái lịch tái khám
     */
    public CheckupSchedule updateCheckupScheduleStatus(Long checkupId, CheckupSchedule.CheckupStatus status,
            LocalDate completedDate, String resultSummary) {
        return checkupScheduleRepository.findById(checkupId)
                .map(checkup -> {
                    checkup.setStatus(status);
                    if (status == CheckupSchedule.CheckupStatus.COMPLETED) {
                        checkup.setCompletedDate(completedDate != null ? completedDate : LocalDate.now());
                        if (resultSummary != null) {
                            checkup.setResultSummary(resultSummary);
                        }
                    }
                    return checkupScheduleRepository.save(checkup);
                })
                .orElse(null);
    }

    /**
     * Cập nhật lịch tái khám
     */
    public CheckupSchedule updateCheckupSchedule(Long checkupId, CheckupSchedule updatedCheckup) {
        return checkupScheduleRepository.findById(checkupId)
                .map(existing -> {
                    existing.setScheduledDate(updatedCheckup.getScheduledDate());
                    existing.setCheckupType(updatedCheckup.getCheckupType());
                    existing.setNotes(updatedCheckup.getNotes());
                    if (updatedCheckup.getStatus() != null) {
                        existing.setStatus(updatedCheckup.getStatus());
                    }
                    return checkupScheduleRepository.save(existing);
                })
                .orElse(null);
    }

    /**
     * Lấy lịch tái khám sắp tới của bác sĩ
     */
    @Transactional(readOnly = true)
    public List<CheckupSchedule> getUpcomingCheckupsByDoctorId(Long doctorId) {
        return checkupScheduleRepository.findUpcomingByDoctorId(doctorId, LocalDate.now());
    }

    /**
     * Lấy lịch tái khám quá hạn
     */
    @Transactional(readOnly = true)
    public List<CheckupSchedule> getOverdueCheckups() {
        return checkupScheduleRepository.findOverdue(LocalDate.now());
    }

    /**
     * Xóa lịch tái khám
     */
    public boolean deleteCheckupSchedule(Long checkupId) {
        if (checkupScheduleRepository.existsById(checkupId)) {
            checkupScheduleRepository.deleteById(checkupId);
            return true;
        }
        return false;
    }
}
