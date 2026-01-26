package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.SurveyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho SurveyTemplate entity
 */
@Repository
public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplate, Long> {
    
    /**
     * Tìm theo tên template
     */
    List<SurveyTemplate> findByTemplateNameContaining(String templateName);
    
    /**
     * Tìm các template đang hoạt động
     */
    List<SurveyTemplate> findByIsActiveTrue();
    
    /**
     * Tìm template do user tạo
     */
    List<SurveyTemplate> findByCreatedById(Long userId);
    
    /**
     * Đếm số template đang hoạt động
     */
    long countByIsActiveTrue();
}
