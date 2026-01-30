package com.g4.capstoneproject.dto.response;

import com.g4.capstoneproject.entity.KnowledgeArticle;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeArticleResponse {
    private Long id;
    private String title;
    private String summary;
    private Long categoryId;
    private String categoryName;
    private String[] tags;
    private String status;
    private Integer views;
    private Boolean featured;
    private Long authorId;
    private String authorName;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KnowledgeArticleResponse fromEntity(KnowledgeArticle article) {
        if (article == null)
            return null;

        return KnowledgeArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .categoryId(article.getCategory() != null ? article.getCategory().getId() : null)
                .categoryName(article.getCategoryName())
                .tags(article.getTagsArray())
                .status(article.getStatus().name())
                .views(article.getViews())
                .featured(article.getFeatured())
                .authorId(article.getCreatedBy() != null ? article.getCreatedBy().getId() : null)
                .authorName(article.getAuthorName())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
