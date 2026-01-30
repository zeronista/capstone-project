package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.response.KnowledgeCategoryResponse;
import com.g4.capstoneproject.entity.KnowledgeCategory;
import com.g4.capstoneproject.repository.KnowledgeCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeCategoryService {

    private final KnowledgeCategoryRepository categoryRepository;

    /**
     * Get all root categories (categories without parent)
     */
    public List<KnowledgeCategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrue()
                .stream()
                .map(KnowledgeCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get full category tree with all children
     */
    public List<KnowledgeCategoryResponse> getCategoryTree() {
        List<KnowledgeCategory> rootCategories = categoryRepository.findByParentIsNullAndActiveTrue();
        return rootCategories.stream()
                .map(KnowledgeCategoryResponse::fromEntityWithChildren)
                .collect(Collectors.toList());
    }

    /**
     * Get children of a specific category
     */
    public List<KnowledgeCategoryResponse> getChildrenCategories(Long parentId) {
        return categoryRepository.findByParentIdAndActiveTrue(parentId)
                .stream()
                .map(KnowledgeCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    public Optional<KnowledgeCategoryResponse> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(KnowledgeCategoryResponse::fromEntity);
    }

    /**
     * Get category with children
     */
    public Optional<KnowledgeCategoryResponse> getCategoryWithChildren(Long id) {
        return categoryRepository.findByIdWithChildren(id)
                .map(KnowledgeCategoryResponse::fromEntityWithChildren);
    }

    /**
     * Create new category
     */
    @Transactional
    public KnowledgeCategoryResponse createCategory(
            String name,
            String description,
            Long parentId,
            Integer displayOrder) {
        // Validate name uniqueness
        Optional<KnowledgeCategory> existing = categoryRepository.findByNameAndActiveTrue(name);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        KnowledgeCategory category = KnowledgeCategory.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .active(true)
                .build();

        // Set parent if provided
        if (parentId != null) {
            KnowledgeCategory parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            category.setParent(parent);
        }

        KnowledgeCategory saved = categoryRepository.save(category);
        log.info("Created category: {} (ID: {})", saved.getName(), saved.getId());

        return KnowledgeCategoryResponse.fromEntity(saved);
    }

    /**
     * Update category
     */
    @Transactional
    public KnowledgeCategoryResponse updateCategory(
            Long id,
            String name,
            String description,
            Long parentId,
            Integer displayOrder,
            Boolean active) {
        KnowledgeCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Check name uniqueness (excluding current category)
        if (name != null && !name.equals(category.getName())) {
            if (categoryRepository.existsByNameAndIdNot(name, id)) {
                throw new IllegalArgumentException("Category with this name already exists");
            }
            category.setName(name);
        }

        if (description != null) {
            category.setDescription(description);
        }

        if (displayOrder != null) {
            category.setDisplayOrder(displayOrder);
        }

        if (active != null) {
            category.setActive(active);
        }

        // Update parent if provided and different
        if (parentId != null) {
            if (!Objects.equals(
                    category.getParent() != null ? category.getParent().getId() : null,
                    parentId)) {
                // Prevent circular reference
                if (id.equals(parentId)) {
                    throw new IllegalArgumentException("Category cannot be its own parent");
                }

                KnowledgeCategory newParent = categoryRepository.findById(parentId)
                        .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));

                // Check if newParent is a descendant of current category
                if (isDescendant(category, newParent)) {
                    throw new IllegalArgumentException("Cannot set a descendant as parent");
                }

                category.setParent(newParent);
            }
        }

        KnowledgeCategory updated = categoryRepository.save(category);
        log.info("Updated category: {} (ID: {})", updated.getName(), updated.getId());

        return KnowledgeCategoryResponse.fromEntity(updated);
    }

    /**
     * Delete category (soft delete)
     */
    @Transactional
    public void deleteCategory(Long id) {
        KnowledgeCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Check if category has articles
        long articleCount = categoryRepository.countArticlesInCategoryTree(id);
        if (articleCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete category with articles. Please move or delete articles first.");
        }

        // Soft delete
        category.setActive(false);
        categoryRepository.save(category);

        log.info("Deleted category: {} (ID: {})", category.getName(), category.getId());
    }

    /**
     * Search categories by name
     */
    public List<KnowledgeCategoryResponse> searchCategories(String keyword) {
        return categoryRepository.searchByName(keyword)
                .stream()
                .map(KnowledgeCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all categories (flat list)
     */
    public List<KnowledgeCategoryResponse> getAllCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(KnowledgeCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Move category to different parent
     */
    @Transactional
    public KnowledgeCategoryResponse moveCategory(Long categoryId, Long newParentId) {
        KnowledgeCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (newParentId == null) {
            // Move to root level
            category.setParent(null);
        } else {
            // Prevent circular reference
            if (categoryId.equals(newParentId)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }

            KnowledgeCategory newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));

            // Check if newParent is a descendant
            if (isDescendant(category, newParent)) {
                throw new IllegalArgumentException("Cannot move to a descendant category");
            }

            category.setParent(newParent);
        }

        KnowledgeCategory moved = categoryRepository.save(category);
        log.info("Moved category {} to parent {}", categoryId, newParentId);

        return KnowledgeCategoryResponse.fromEntity(moved);
    }

    /**
     * Reorder categories
     */
    @Transactional
    public void reorderCategories(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            final int order = i;
            Long categoryId = categoryIds.get(i);
            categoryRepository.findById(categoryId).ifPresent(category -> {
                category.setDisplayOrder(order);
                categoryRepository.save(category);
            });
        }
        log.info("Reordered {} categories", categoryIds.size());
    }

    /**
     * Check if target is a descendant of category
     */
    private boolean isDescendant(KnowledgeCategory category, KnowledgeCategory target) {
        KnowledgeCategory current = target.getParent();
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
