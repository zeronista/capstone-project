package com.g4.capstoneproject.dto.response;

import com.g4.capstoneproject.entity.KnowledgeCategory;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private Integer displayOrder;
    private Boolean active;
    private Integer articleCount;
    private Integer depth;
    private Boolean hasChildren;

    @Builder.Default
    private List<KnowledgeCategoryResponse> children = new ArrayList<>();

    public static KnowledgeCategoryResponse fromEntity(KnowledgeCategory category) {
        if (category == null)
            return null;

        return KnowledgeCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .displayOrder(category.getDisplayOrder())
                .active(category.getActive())
                .articleCount(category.getArticleCount())
                .depth(category.getDepth())
                .hasChildren(category.hasChildren())
                .children(new ArrayList<>())
                .build();
    }

    public static KnowledgeCategoryResponse fromEntityWithChildren(KnowledgeCategory category) {
        KnowledgeCategoryResponse response = fromEntity(category);
        if (category.hasChildren()) {
            response.setChildren(
                    category.getChildren().stream()
                            .map(KnowledgeCategoryResponse::fromEntityWithChildren)
                            .collect(Collectors.toList()));
        }
        return response;
    }
}
