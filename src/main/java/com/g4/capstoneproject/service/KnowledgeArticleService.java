package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.response.KnowledgeArticleDetailResponse;
import com.g4.capstoneproject.dto.response.KnowledgeArticleResponse;
import com.g4.capstoneproject.entity.KnowledgeArticle;
import com.g4.capstoneproject.entity.KnowledgeArticle.ArticleStatus;
import com.g4.capstoneproject.entity.KnowledgeCategory;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.KnowledgeArticleRepository;
import com.g4.capstoneproject.repository.KnowledgeCategoryRepository;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeArticleService {

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Get all articles with pagination
     */
    public Page<KnowledgeArticleResponse> getAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable)
                .map(KnowledgeArticleResponse::fromEntity);
    }

    /**
     * Get articles by status
     */
    public Page<KnowledgeArticleResponse> getArticlesByStatus(ArticleStatus status, Pageable pageable) {
        return articleRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(KnowledgeArticleResponse::fromEntity);
    }

    /**
     * Get articles by category
     */
    public Page<KnowledgeArticleResponse> getArticlesByCategory(Long categoryId, Pageable pageable) {
        return articleRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable)
                .map(KnowledgeArticleResponse::fromEntity);
    }

    /**
     * Get articles by category and status
     */
    public Page<KnowledgeArticleResponse> getArticlesByCategoryAndStatus(
            Long categoryId, ArticleStatus status, Pageable pageable) {
        return articleRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(categoryId, status, pageable)
                .map(KnowledgeArticleResponse::fromEntity);
    }

    /**
     * Get published articles
     */
    public Page<KnowledgeArticleResponse> getPublishedArticles(Pageable pageable) {
        return articleRepository.findPublishedArticles(pageable)
                .map(KnowledgeArticleResponse::fromEntity);
    }

    /**
     * Get featured articles
     */
    public List<KnowledgeArticleResponse> getFeaturedArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return articleRepository.findByFeaturedTrueAndStatusOrderByViewsDesc(ArticleStatus.PUBLISHED, pageable)
                .stream()
                .map(KnowledgeArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get most viewed articles
     */
    public List<KnowledgeArticleResponse> getMostViewedArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return articleRepository.findMostViewed(pageable)
                .stream()
                .map(KnowledgeArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get recent published articles
     */
    public List<KnowledgeArticleResponse> getRecentArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return articleRepository.findRecentPublished(pageable)
                .stream()
                .map(KnowledgeArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search articles by keyword
     */
    public Page<KnowledgeArticleResponse> searchArticles(
            String keyword, ArticleStatus status, Pageable pageable) {
        return articleRepository.searchByKeyword(keyword, status.name(), pageable)
                .map(KnowledgeArticleResponse::fromEntity);
    }

    /**
     * Search articles by keyword and category
     */
    public Page<KnowledgeArticleResponse> searchArticlesByCategory(
            Long categoryId, String keyword, ArticleStatus status, Pageable pageable) {
        return articleRepository.searchByCategoryAndKeyword(categoryId, keyword, status.name(), pageable)
                .map(KnowledgeArticleResponse::fromEntity);
    }

    /**
     * Search articles by tag
     */
    public Page<KnowledgeArticleResponse> searchByTag(String tag, Pageable pageable) {
        return articleRepository.findByTag(tag, ArticleStatus.PUBLISHED, pageable)
                .map(KnowledgeArticleResponse::fromEntity);
    }

    /**
     * Get article by ID (with view increment)
     */
    @Transactional
    public KnowledgeArticleDetailResponse getArticleById(Long id, boolean incrementView) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        if (incrementView && article.isPublished()) {
            articleRepository.incrementViews(id);
            article.incrementViews();
        }

        return KnowledgeArticleDetailResponse.fromEntity(article);
    }

    /**
     * Get article by ID (without view increment, for editing)
     */
    public KnowledgeArticleDetailResponse getArticleForEdit(Long id) {
        return getArticleById(id, false);
    }

    /**
     * Create new article
     */
    @Transactional
    public KnowledgeArticleDetailResponse createArticle(
            String title,
            String summary,
            String content,
            Long categoryId,
            String[] tags,
            Long authorId,
            ArticleStatus status,
            Boolean featured) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        KnowledgeArticle article = KnowledgeArticle.builder()
                .title(title)
                .summary(summary)
                .content(content)
                .createdBy(author)
                .status(status != null ? status : ArticleStatus.DRAFT)
                .featured(featured != null ? featured : false)
                .views(0)
                .build();

        // Set category if provided
        if (categoryId != null) {
            KnowledgeCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            article.setCategory(category);
        }

        // Set tags
        if (tags != null && tags.length > 0) {
            article.setTagsFromArray(tags);
        }

        // Set published date if status is PUBLISHED
        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }

        KnowledgeArticle saved = articleRepository.save(article);
        log.info("Created article: {} (ID: {}) by {}", saved.getTitle(), saved.getId(), author.getFullName());

        return KnowledgeArticleDetailResponse.fromEntity(saved);
    }

    /**
     * Update article
     */
    @Transactional
    public KnowledgeArticleDetailResponse updateArticle(
            Long id,
            String title,
            String summary,
            String content,
            Long categoryId,
            String[] tags,
            Long updatedById,
            ArticleStatus status,
            Boolean featured) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User updatedBy = userRepository.findById(updatedById)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (title != null)
            article.setTitle(title);
        if (summary != null)
            article.setSummary(summary);
        if (content != null)
            article.setContent(content);
        if (featured != null)
            article.setFeatured(featured);

        // Update category
        if (categoryId != null) {
            if (categoryId == 0) {
                article.setCategory(null);
            } else {
                KnowledgeCategory category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new IllegalArgumentException("Category not found"));
                article.setCategory(category);
            }
        }

        // Update tags
        if (tags != null) {
            article.setTagsFromArray(tags);
        }

        // Update status and published date
        if (status != null && status != article.getStatus()) {
            ArticleStatus oldStatus = article.getStatus();
            article.setStatus(status);

            if (status == ArticleStatus.PUBLISHED && article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }

            log.info("Article {} status changed from {} to {}", id, oldStatus, status);
        }

        article.setUpdatedBy(updatedBy);

        KnowledgeArticle updated = articleRepository.save(article);
        log.info("Updated article: {} (ID: {}) by {}", updated.getTitle(), updated.getId(), updatedBy.getFullName());

        return KnowledgeArticleDetailResponse.fromEntity(updated);
    }

    /**
     * Delete article (hard delete)
     */
    @Transactional
    public void deleteArticle(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        articleRepository.delete(article);
        log.info("Deleted article: {} (ID: {})", article.getTitle(), article.getId());
    }

    /**
     * Publish article
     */
    @Transactional
    public KnowledgeArticleDetailResponse publishArticle(Long id, Long userId) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        article.publish();
        article.setUpdatedBy(user);

        KnowledgeArticle published = articleRepository.save(article);
        log.info("Published article: {} (ID: {})", published.getTitle(), published.getId());

        return KnowledgeArticleDetailResponse.fromEntity(published);
    }

    /**
     * Archive article
     */
    @Transactional
    public KnowledgeArticleDetailResponse archiveArticle(Long id, Long userId) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        article.archive();
        article.setUpdatedBy(user);

        KnowledgeArticle archived = articleRepository.save(article);
        log.info("Archived article: {} (ID: {})", archived.getTitle(), archived.getId());

        return KnowledgeArticleDetailResponse.fromEntity(archived);
    }

    /**
     * Toggle featured status
     */
    @Transactional
    public KnowledgeArticleDetailResponse toggleFeatured(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        article.setFeatured(!article.getFeatured());

        KnowledgeArticle updated = articleRepository.save(article);
        log.info("Toggled featured status for article {} to {}", id, updated.getFeatured());

        return KnowledgeArticleDetailResponse.fromEntity(updated);
    }

    /**
     * Get all unique tags
     */
    public List<String> getAllTags() {
        try {
            return articleRepository.findAllTags();
        } catch (Exception e) {
            // Fallback to alternative method if JSON_TABLE not supported
            log.warn("JSON_TABLE not supported, using alternative tag extraction method");
            return articleRepository.findAllTagsAlternative();
        }
    }

    /**
     * Get related articles by tags
     */
    public List<KnowledgeArticleResponse> getRelatedArticles(Long articleId, int limit) {
        KnowledgeArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        String[] tags = article.getTagsArray();
        if (tags.length == 0) {
            return List.of();
        }

        String tag1 = tags.length > 0 ? tags[0] : "";
        String tag2 = tags.length > 1 ? tags[1] : "";
        String tag3 = tags.length > 2 ? tags[2] : "";

        Pageable pageable = PageRequest.of(0, limit);
        return articleRepository.findRelatedByTags(articleId, tag1, tag2, tag3, pageable)
                .stream()
                .map(KnowledgeArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get articles by author
     */
    public Page<KnowledgeArticleResponse> getArticlesByAuthor(Long authorId, Pageable pageable) {
        return articleRepository.findByCreatedByIdOrderByCreatedAtDesc(authorId, pageable)
                .map(KnowledgeArticleResponse::fromEntity);
    }

    /**
     * Get article statistics
     */
    public ArticleStatistics getStatistics() {
        long totalCount = articleRepository.count();
        long publishedCount = articleRepository.countByStatus(ArticleStatus.PUBLISHED);
        long draftCount = articleRepository.countByStatus(ArticleStatus.DRAFT);
        long archivedCount = articleRepository.countByStatus(ArticleStatus.ARCHIVED);

        return new ArticleStatistics(totalCount, publishedCount, draftCount, archivedCount);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ArticleStatistics {
        private long totalCount;
        private long publishedCount;
        private long draftCount;
        private long archivedCount;
    }
}
