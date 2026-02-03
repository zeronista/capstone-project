package com.g4.capstoneproject.service;

import com.g4.capstoneproject.entity.TreatmentPlan;
import com.g4.capstoneproject.entity.TreatmentPlanItem;
import com.g4.capstoneproject.repository.TreatmentPlanRepository;
import com.g4.capstoneproject.repository.TreatmentPlanItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Lấy tất cả treatment plan
     */
    @Transactional(readOnly = true)
    public List<TreatmentPlan> getAllTreatmentPlans() {
        return treatmentPlanRepository.findAll();
    }

    /**
     * Lấy treatment plan theo ID
     * Cached for 30 minutes
     */
    @Cacheable(value = "treatmentPlans", key = "#id")
    @Transactional(readOnly = true)
    public Optional<TreatmentPlan> getTreatmentPlanById(Long id) {
        return treatmentPlanRepository.findById(id);
    }

    /**
     * Lấy treatment plan theo bệnh nhân
     * Cached for 30 minutes
     */
    @Cacheable(value = "treatmentPlans", key = "'patient-' + #patientId")
    @Transactional(readOnly = true)
    public List<TreatmentPlan> getTreatmentPlansByPatientId(Long patientId) {
        return treatmentPlanRepository.findByPatientId(patientId);
    }

    /**
     * Lấy treatment plan theo bác sĩ
     * Cached for 30 minutes
     */
    @Cacheable(value = "treatmentPlans", key = "'doctor-' + #doctorId")
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
     * Clears treatment plan caches
     */
    @CacheEvict(value = "treatmentPlans", allEntries = true)
    public TreatmentPlan createTreatmentPlan(TreatmentPlan plan) {
        return treatmentPlanRepository.save(plan);
    }

    /**
     * Cập nhật treatment plan
     * Clears treatment plan caches
     */
    @CacheEvict(value = "treatmentPlans", allEntries = true)
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
}
