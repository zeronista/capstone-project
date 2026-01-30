package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.KnowledgeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeCategoryRepository extends JpaRepository<KnowledgeCategory, Long> {

    // Find root categories (no parent)
    List<KnowledgeCategory> findByParentIsNullAndActiveTrue();

    // Find children of a category
    List<KnowledgeCategory> findByParentIdAndActiveTrue(Long parentId);

    // Find all active categories ordered
    List<KnowledgeCategory> findByActiveTrueOrderByDisplayOrderAscNameAsc();

    // Find category by name
    Optional<KnowledgeCategory> findByNameAndActiveTrue(String name);

    // Check if name exists (for validation)
    boolean existsByNameAndIdNot(String name, Long id);

    // Get category with all children (recursive)
    @Query("SELECT c FROM KnowledgeCategory c " +
            "LEFT JOIN FETCH c.children " +
            "WHERE c.id = :id AND c.active = true")
    Optional<KnowledgeCategory> findByIdWithChildren(@Param("id") Long id);

    // Get full category tree
    @Query("SELECT c FROM KnowledgeCategory c " +
            "LEFT JOIN FETCH c.parent " +
            "WHERE c.active = true " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<KnowledgeCategory> findAllWithParent();

    // Count articles in category (including subcategories)
    @Query("SELECT COUNT(a) FROM KnowledgeArticle a " +
            "WHERE a.category.id = :categoryId " +
            "OR a.category.parent.id = :categoryId")
    long countArticlesInCategoryTree(@Param("categoryId") Long categoryId);

    // Find categories with article count
    @Query("SELECT c, COUNT(a) as articleCount FROM KnowledgeCategory c " +
            "LEFT JOIN c.articles a " +
            "WHERE c.active = true " +
            "GROUP BY c " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Object[]> findAllWithArticleCount();

    // Search categories by name
    @Query("SELECT c FROM KnowledgeCategory c " +
            "WHERE c.active = true " +
            "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY c.displayOrder ASC")
    List<KnowledgeCategory> searchByName(@Param("keyword") String keyword);
}
