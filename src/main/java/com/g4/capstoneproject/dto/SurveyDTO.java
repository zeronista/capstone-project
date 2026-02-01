package com.g4.capstoneproject.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyDTO {
    private Long id;
    private String title;
    private String description;
    private String formUrl;
    private String iconType;
    private String iconColor;
    private String tag;
    private String tagColor;
    private Integer displayOrder;
    private Integer responseCount;
    private Boolean isActive;
    private Boolean showOnLanding;
    private String createdByName;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
