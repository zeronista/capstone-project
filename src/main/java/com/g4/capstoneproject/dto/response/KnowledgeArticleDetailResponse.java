package com.g4.capstoneproject.dto.response;

import com.g4.capstoneproject.entity.KnowledgeArticle;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeArticleDetailResponse {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long categoryId;
    private String categoryName;
    private String[] tags;
    private String status;
    private Integer views;
    private Boolean featured;

    private Long authorId;
    private String authorName;

    private Long updatedById;
    private String updatedByName;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KnowledgeArticleDetailResponse fromEntity(KnowledgeArticle article) {
        if (article == null)
            return null;

        return KnowledgeArticleDetailResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .categoryId(article.getCategory() != null ? article.getCategory().getId() : null)
                .categoryName(article.getCategoryName())
                .tags(article.getTagsArray())
                .status(article.getStatus().name())
                .views(article.getViews())
                .featured(article.getFeatured())
                .authorId(article.getCreatedBy() != null ? article.getCreatedBy().getId() : null)
                .authorName(article.getAuthorName())
                .updatedById(article.getUpdatedBy() != null ? article.getUpdatedBy().getId() : null)
                .updatedByName(article.getUpdatedBy() != null ? article.getUpdatedBy().getFullName() : null)
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
