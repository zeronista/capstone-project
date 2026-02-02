package com.g4.capstoneproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_articles", indexes = {
        @Index(name = "idx_category", columnList = "category_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_by", columnList = "created_by"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private KnowledgeCategory category;

    @Column(length = 1000)
    private String tags; // Comma-separated tags

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(nullable = false)
    @Builder.Default
    private Integer views = 0;

    @Column(name = "featured")
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ArticleStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    // Helper methods
    public void incrementViews() {
        this.views = (this.views == null ? 0 : this.views) + 1;
    }

    public boolean isPublished() {
        return status == ArticleStatus.PUBLISHED;
    }

    public void publish() {
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = ArticleStatus.ARCHIVED;
    }

    public String[] getTagsArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }

    public void setTagsFromArray(String[] tagsArray) {
        if (tagsArray == null || tagsArray.length == 0) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagsArray);
        }
    }

    public String getCategoryName() {
        return category != null ? category.getName() : "Chưa phân loại";
    }

    public String getAuthorName() {
        return createdBy != null ? createdBy.getFullName() : "Unknown";
    }
}
