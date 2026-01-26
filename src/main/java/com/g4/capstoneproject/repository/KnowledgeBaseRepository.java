package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.KnowledgeBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho KnowledgeBase entity
 */
@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    
    /**
     * Tìm theo danh mục
     */
    List<KnowledgeBase> findByCategory(String category);
    
    /**
     * Tìm theo nguồn
     */
    List<KnowledgeBase> findBySourceType(KnowledgeBase.SourceType sourceType);
    
    /**
     * Tìm tri thức đã được duyệt
     */
    List<KnowledgeBase> findByIsApprovedTrue();
    
    /**
     * Tìm tri thức chưa được duyệt
     */
    List<KnowledgeBase> findByIsApprovedFalse();
    
    /**
     * Tìm kiếm theo câu hỏi
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.question LIKE %:keyword% AND kb.isApproved = true")
    List<KnowledgeBase> searchByQuestion(@Param("keyword") String keyword);
    
    /**
     * Tìm kiếm theo câu hỏi và câu trả lời
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE (kb.question LIKE %:keyword% OR kb.answer LIKE %:keyword%) AND kb.isApproved = true")
    List<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Tìm tri thức phổ biến nhất
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.isApproved = true ORDER BY kb.usageCount DESC")
    List<KnowledgeBase> findMostUsed(Pageable pageable);
    
    /**
     * Tìm tri thức có độ tin cậy cao
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.confidenceScore >= :minScore AND kb.isApproved = true ORDER BY kb.confidenceScore DESC")
    List<KnowledgeBase> findHighConfidence(@Param("minScore") Double minScore);
    
    /**
     * Cập nhật số lần sử dụng
     */
    @Modifying
    @Query("UPDATE KnowledgeBase kb SET kb.usageCount = kb.usageCount + 1, kb.lastUsedAt = CURRENT_TIMESTAMP WHERE kb.id = :id")
    void incrementUsageCount(@Param("id") Long id);
    
    /**
     * Phân trang tri thức
     */
    Page<KnowledgeBase> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Đếm tri thức chưa duyệt
     */
    long countByIsApprovedFalse();
    
    /**
     * Tìm tất cả danh mục
     */
    @Query("SELECT DISTINCT kb.category FROM KnowledgeBase kb WHERE kb.category IS NOT NULL")
    List<String> findAllCategories();
}
