package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.TreatmentPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho TreatmentPlanItem entity
 */
@Repository
public interface TreatmentPlanItemRepository extends JpaRepository<TreatmentPlanItem, Long> {
    
    /**
     * Tìm item theo kế hoạch
     */
    List<TreatmentPlanItem> findByTreatmentPlanId(Long treatmentPlanId);
    
    /**
     * Tìm item theo loại
     */
    List<TreatmentPlanItem> findByTreatmentPlanIdAndItemType(Long treatmentPlanId, TreatmentPlanItem.ItemType itemType);
    
    /**
     * Tìm item theo trạng thái
     */
    List<TreatmentPlanItem> findByTreatmentPlanIdAndStatus(Long treatmentPlanId, TreatmentPlanItem.ItemStatus status);
    
    /**
     * Đếm item theo kế hoạch
     */
    long countByTreatmentPlanId(Long treatmentPlanId);
    
    /**
     * Đếm item hoàn thành trong kế hoạch
     */
    long countByTreatmentPlanIdAndStatus(Long treatmentPlanId, TreatmentPlanItem.ItemStatus status);
}
