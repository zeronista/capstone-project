package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Feedback entity
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * Tìm feedback của cuộc gọi
     */
    List<Feedback> findByCallLogId(Long callLogId);
    
    /**
     * Tìm feedback của ticket
     */
    List<Feedback> findByTicketId(Long ticketId);
    
    /**
     * Tìm feedback theo user
     */
    List<Feedback> findByUserId(Long userId);
    
    /**
     * Tìm feedback theo loại
     */
    List<Feedback> findByFeedbackType(Feedback.FeedbackType feedbackType);
    
    /**
     * Tìm feedback chưa được xem xét
     */
    List<Feedback> findByIsReviewedFalse();
    
    /**
     * Tìm feedback theo mức đánh giá
     */
    List<Feedback> findByRating(Integer rating);
    
    /**
     * Tìm feedback có đánh giá thấp
     */
    @Query("SELECT f FROM Feedback f WHERE f.rating <= :maxRating ORDER BY f.createdAt DESC")
    List<Feedback> findLowRatingFeedback(@Param("maxRating") Integer maxRating);
    
    /**
     * Tính điểm trung bình theo loại
     */
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.feedbackType = :type")
    Double getAverageRatingByType(@Param("type") Feedback.FeedbackType type);
    
    /**
     * Phân trang feedback
     */
    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Đếm feedback chưa xem xét
     */
    long countByIsReviewedFalse();
    
    /**
     * Thống kê feedback theo rating
     */
    @Query("SELECT f.rating, COUNT(f) FROM Feedback f GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> countByRating();
}
