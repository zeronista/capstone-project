package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.CallLog;
import com.g4.capstoneproject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho CallLog entity
 */
@Repository
public interface CallLogRepository extends JpaRepository<CallLog, Long> {
    
    /**
     * Tìm cuộc gọi theo bệnh nhân
     */
    List<CallLog> findByPatient(User patient);
    
    /**
     * Tìm cuộc gọi theo ID bệnh nhân
     */
    List<CallLog> findByPatientId(Long patientId);
    
    // Removed: findByCampaignId() - CallCampaign entity removed in schema v4.0
    
    /**
     * Tìm cuộc gọi theo số điện thoại
     */
    List<CallLog> findByPhoneNumber(String phoneNumber);
    
    /**
     * Tìm cuộc gọi theo loại
     */
    List<CallLog> findByCallType(CallLog.CallType callType);
    
    /**
     * Tìm cuộc gọi theo trạng thái
     */
    List<CallLog> findByCallStatus(CallLog.CallStatus callStatus);
    
    /**
     * Tìm cuộc gọi đã escalate (chuyển cho người thật)
     */
    List<CallLog> findByIsEscalatedTrue();
    
    /**
     * Tìm cuộc gọi trong khoảng thời gian
     */
    @Query("SELECT c FROM CallLog c WHERE c.startTime BETWEEN :startDate AND :endDate ORDER BY c.startTime DESC")
    List<CallLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Tìm cuộc gọi do nhân viên xử lý
     */
    List<CallLog> findByHandledById(Long handledById);
    
    /**
     * Phân trang cuộc gọi
     */
    Page<CallLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Đếm cuộc gọi theo trạng thái
     */
    long countByCallStatus(CallLog.CallStatus callStatus);
    
    /**
     * Đếm cuộc gọi đã escalate
     */
    long countByIsEscalatedTrue();
    
    /**
     * Tìm cuộc gọi có độ tin cậy AI thấp
     */
    @Query("SELECT c FROM CallLog c WHERE c.aiConfidenceScore < :threshold ORDER BY c.aiConfidenceScore ASC")
    List<CallLog> findLowConfidenceCalls(@Param("threshold") Double threshold);
    
    /**
     * Thống kê cuộc gọi theo ngày
     */
    @Query("SELECT DATE(c.startTime) as date, COUNT(c) as count FROM CallLog c WHERE c.startTime >= :startDate GROUP BY DATE(c.startTime)")
    List<Object[]> countCallsByDate(@Param("startDate") LocalDateTime startDate);
}
