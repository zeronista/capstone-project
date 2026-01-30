package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.KnowledgeArticle;
import com.g4.capstoneproject.entity.KnowledgeArticle.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    // Find all articles by status
    Page<KnowledgeArticle> findByStatusOrderByCreatedAtDesc(ArticleStatus status, Pageable pageable);

    // Find articles by category
    Page<KnowledgeArticle> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    // Find articles by category and status
    Page<KnowledgeArticle> findByCategoryIdAndStatusOrderByCreatedAtDesc(
            Long categoryId, ArticleStatus status, Pageable pageable);

    // Find featured articles
    List<KnowledgeArticle> findByFeaturedTrueAndStatusOrderByViewsDesc(
            ArticleStatus status, Pageable pageable);

    // Find articles by author
    Page<KnowledgeArticle> findByCreatedByIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // Find published articles
    @Query("SELECT a FROM KnowledgeArticle a " +
            "WHERE a.status = 'PUBLISHED' " +
            "ORDER BY a.publishedAt DESC")
    Page<KnowledgeArticle> findPublishedArticles(Pageable pageable);

    // Full-text search in title and content
    @Query(value = "SELECT * FROM knowledge_articles " +
            "WHERE status = :status " +
            "AND (title LIKE CONCAT('%', :keyword, '%') " +
            "OR content LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY created_at DESC", nativeQuery = true)
    Page<KnowledgeArticle> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable);

    // Search with category filter
    @Query(value = "SELECT * FROM knowledge_articles " +
            "WHERE category_id = :categoryId " +
            "AND status = :status " +
            "AND (title LIKE CONCAT('%', :keyword, '%') " +
            "OR content LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY created_at DESC", nativeQuery = true)
    Page<KnowledgeArticle> searchByCategoryAndKeyword(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable);

    // Find articles by tag
    @Query("SELECT a FROM KnowledgeArticle a " +
            "WHERE a.status = :status " +
            "AND a.tags LIKE CONCAT('%', :tag, '%') " +
            "ORDER BY a.createdAt DESC")
    Page<KnowledgeArticle> findByTag(
            @Param("tag") String tag,
            @Param("status") ArticleStatus status,
            Pageable pageable);

    // Get most viewed articles
    @Query("SELECT a FROM KnowledgeArticle a " +
            "WHERE a.status = 'PUBLISHED' " +
            "ORDER BY a.views DESC, a.createdAt DESC")
    List<KnowledgeArticle> findMostViewed(Pageable pageable);

    // Get recent articles
    @Query("SELECT a FROM KnowledgeArticle a " +
            "WHERE a.status = 'PUBLISHED' " +
            "ORDER BY a.publishedAt DESC")
    List<KnowledgeArticle> findRecentPublished(Pageable pageable);

    // Increment view count
    @Modifying
    @Query("UPDATE KnowledgeArticle a SET a.views = a.views + 1 WHERE a.id = :id")
    void incrementViews(@Param("id") Long id);

    // Count articles by status
    long countByStatus(ArticleStatus status);

    // Count articles by category
    long countByCategoryId(Long categoryId);

    // Count articles by author
    long countByCreatedById(Long authorId);

    // Get all unique tags
    @Query(value = "SELECT DISTINCT TRIM(value) as tag FROM knowledge_articles " +
            "CROSS JOIN JSON_TABLE(CONCAT('[\"', REPLACE(tags, ',', '\",\"'), '\"]'), '$[*]' " +
            "COLUMNS(value VARCHAR(100) PATH '$')) AS jt " +
            "WHERE tags IS NOT NULL AND tags != '' " +
            "ORDER BY tag", nativeQuery = true)
    List<String> findAllTags();

    // Alternative: Get tags using string manipulation (if MySQL version doesn't
    // support JSON_TABLE)
    @Query(value = "SELECT DISTINCT SUBSTRING_INDEX(SUBSTRING_INDEX(a.tags, ',', numbers.n), ',', -1) as tag " +
            "FROM knowledge_articles a " +
            "INNER JOIN (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 " +
            "UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) numbers " +
            "ON CHAR_LENGTH(a.tags) - CHAR_LENGTH(REPLACE(a.tags, ',', '')) >= numbers.n - 1 " +
            "WHERE a.tags IS NOT NULL AND a.tags != '' " +
            "ORDER BY tag", nativeQuery = true)
    List<String> findAllTagsAlternative();

    // Find related articles by tags
    @Query("SELECT a FROM KnowledgeArticle a " +
            "WHERE a.id != :articleId " +
            "AND a.status = 'PUBLISHED' " +
            "AND (a.tags LIKE CONCAT('%', :tag1, '%') " +
            "OR a.tags LIKE CONCAT('%', :tag2, '%') " +
            "OR a.tags LIKE CONCAT('%', :tag3, '%')) " +
            "ORDER BY a.views DESC")
    List<KnowledgeArticle> findRelatedByTags(
            @Param("articleId") Long articleId,
            @Param("tag1") String tag1,
            @Param("tag2") String tag2,
            @Param("tag3") String tag3,
            Pageable pageable);
}
