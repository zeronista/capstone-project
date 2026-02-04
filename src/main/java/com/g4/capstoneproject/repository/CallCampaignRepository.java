package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.CallCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository cho CallCampaign entity
 */
@Repository
public interface CallCampaignRepository extends JpaRepository<CallCampaign, Long> {
    
    /**
     * Tìm theo tên chiến dịch
     */
    List<CallCampaign> findByCampaignNameContaining(String campaignName);
    
    /**
     * Tìm theo loại chiến dịch
     */
    List<CallCampaign> findByCampaignType(CallCampaign.CampaignType campaignType);
    
    /**
     * Tìm theo trạng thái
     */
    List<CallCampaign> findByStatus(CallCampaign.CampaignStatus status);
    
    /**
     * Tìm chiến dịch đang hoạt động
     */
    @Query("SELECT c FROM CallCampaign c WHERE c.status = 'ACTIVE' AND c.startDate <= :today AND (c.endDate IS NULL OR c.endDate >= :today)")
    List<CallCampaign> findActiveCampaigns(@Param("today") LocalDate today);
    
    /**
     * Tìm theo đối tượng mục tiêu
     */
    List<CallCampaign> findByTargetAudience(CallCampaign.TargetAudience targetAudience);
    
    /**
     * Tìm chiến dịch do user tạo
     */
    List<CallCampaign> findByCreatedById(Long userId);
    
    /**
     * Đếm số chiến dịch theo trạng thái
     */
    long countByStatus(CallCampaign.CampaignStatus status);
}
