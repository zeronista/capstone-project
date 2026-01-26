package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.AITrainingData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho AITrainingData entity
 */
@Repository
public interface AITrainingDataRepository extends JpaRepository<AITrainingData, Long> {
    
    /**
     * Tìm dữ liệu huấn luyện từ cuộc gọi
     */
    List<AITrainingData> findByCallLogId(Long callLogId);
    
    /**
     * Tìm dữ liệu huấn luyện từ ticket
     */
    List<AITrainingData> findByTicketId(Long ticketId);
    
    /**
     * Tìm dữ liệu đã được dùng để huấn luyện
     */
    List<AITrainingData> findByIsUsedForTrainingTrue();
    
    /**
     * Tìm dữ liệu chưa được dùng để huấn luyện
     */
    List<AITrainingData> findByIsUsedForTrainingFalse();
    
    /**
     * Tìm theo batch ID
     */
    List<AITrainingData> findByTrainingBatchId(String trainingBatchId);
    
    /**
     * Tìm dữ liệu có feedback tốt (để huấn luyện)
     */
    @Query("SELECT t FROM AITrainingData t WHERE t.feedbackScore >= :minScore AND t.isUsedForTraining = false")
    List<AITrainingData> findHighQualityUnusedData(@Param("minScore") Integer minScore);
    
    /**
     * Phân trang dữ liệu huấn luyện
     */
    Page<AITrainingData> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Đếm dữ liệu chưa được dùng
     */
    long countByIsUsedForTrainingFalse();
    
    /**
     * Đếm dữ liệu theo batch
     */
    long countByTrainingBatchId(String trainingBatchId);
}
