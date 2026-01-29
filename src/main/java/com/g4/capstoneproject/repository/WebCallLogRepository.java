package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.WebCallLog;
import com.g4.capstoneproject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho WebCallLog entity
 * Quản lý lịch sử cuộc gọi Web-to-Web
 */
@Repository
public interface WebCallLogRepository extends JpaRepository<WebCallLog, Long> {
    
    /**
     * Tìm cuộc gọi theo Stringee Call ID
     */
    Optional<WebCallLog> findByStringeeCallId(String stringeeCallId);
    
    /**
     * Tìm tất cả cuộc gọi của một user (cả gọi đi và gọi đến)
     */
    @Query("SELECT w FROM WebCallLog w WHERE w.caller.id = :userId OR w.receiver.id = :userId ORDER BY w.createdAt DESC")
    List<WebCallLog> findAllByUserId(@Param("userId") Long userId);
    
    /**
     * Phân trang cuộc gọi của một user
     */
    @Query("SELECT w FROM WebCallLog w WHERE w.caller.id = :userId OR w.receiver.id = :userId ORDER BY w.createdAt DESC")
    Page<WebCallLog> findAllByUserIdPaged(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Tìm cuộc gọi đi của user
     */
    List<WebCallLog> findByCallerIdOrderByCreatedAtDesc(Long callerId);
    
    /**
     * Tìm cuộc gọi đến của user
     */
    List<WebCallLog> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    
    /**
     * Tìm cuộc gọi nhỡ của user
     */
    @Query("SELECT w FROM WebCallLog w WHERE w.receiver.id = :userId AND w.callStatus = 'MISSED' ORDER BY w.createdAt DESC")
    List<WebCallLog> findMissedCallsByReceiverId(@Param("userId") Long userId);
    
    /**
     * Tìm cuộc gọi có ghi âm của user
     */
    @Query("SELECT w FROM WebCallLog w WHERE (w.caller.id = :userId OR w.receiver.id = :userId) AND w.hasRecording = true ORDER BY w.createdAt DESC")
    List<WebCallLog> findCallsWithRecording(@Param("userId") Long userId);
    
    /**
     * Tìm cuộc gọi trong khoảng thời gian
     */
    @Query("SELECT w FROM WebCallLog w WHERE (w.caller.id = :userId OR w.receiver.id = :userId) AND w.startTime BETWEEN :startDate AND :endDate ORDER BY w.startTime DESC")
    List<WebCallLog> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Đếm cuộc gọi của user theo trạng thái
     */
    @Query("SELECT w.callStatus, COUNT(w) FROM WebCallLog w WHERE w.caller.id = :userId OR w.receiver.id = :userId GROUP BY w.callStatus")
    List<Object[]> countByUserIdGroupByStatus(@Param("userId") Long userId);
    
    /**
     * Đếm tổng cuộc gọi của user
     */
    @Query("SELECT COUNT(w) FROM WebCallLog w WHERE w.caller.id = :userId OR w.receiver.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * Đếm cuộc gọi nhỡ chưa xem
     */
    @Query("SELECT COUNT(w) FROM WebCallLog w WHERE w.receiver.id = :userId AND w.callStatus = 'MISSED'")
    long countMissedCalls(@Param("userId") Long userId);
    
    /**
     * Tính tổng thời gian gọi của user (phút)
     */
    @Query("SELECT SUM(w.duration) FROM WebCallLog w WHERE (w.caller.id = :userId OR w.receiver.id = :userId) AND w.callStatus = 'COMPLETED'")
    Long sumDurationByUserId(@Param("userId") Long userId);
    
    /**
     * Thống kê cuộc gọi theo ngày
     */
    @Query("SELECT DATE(w.startTime) as date, COUNT(w) as count FROM WebCallLog w WHERE (w.caller.id = :userId OR w.receiver.id = :userId) AND w.startTime >= :startDate GROUP BY DATE(w.startTime)")
    List<Object[]> countCallsByDate(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    /**
     * Tìm cuộc gọi gần nhất giữa 2 user
     */
    @Query("SELECT w FROM WebCallLog w WHERE (w.caller.id = :userId1 AND w.receiver.id = :userId2) OR (w.caller.id = :userId2 AND w.receiver.id = :userId1) ORDER BY w.createdAt DESC")
    List<WebCallLog> findCallsBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
