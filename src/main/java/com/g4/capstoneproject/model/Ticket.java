package com.g4.capstoneproject.model;

import java.time.LocalDateTime;

/**
 * Model Ticket chuyên khoa
 * Chức năng: Quản lý yêu cầu tư vấn chuyên khoa từ nhân viên/hệ thống
 */
public class Ticket {
    private String id;
    private String title;
    private String description;
    private String patientId;
    private String patientName;
    private String creatorName;
    private String creatorRole;
    private String assignedDoctor;
    private String priority; // "Ưu tiên cao", "Ưu tiên trung bình", "Ưu tiên thấp"
    private String status; // "Chờ phản hồi", "Đang xử lý", "Hoàn thành"
    private String category; // "Tư vấn chuyên khoa", "Xét nghiệm phức tạp", "Điều trị đặc biệt", "Hội chẩn"
    private LocalDateTime createdDate;
    private LocalDateTime slaDeadline;
    private int attachmentCount;
    private int commentCount;
    private String aiSuggestion;

    // Constructors
    public Ticket() {}

    public Ticket(String id, String title, String description, String patientId, String patientName,
                 String creatorName, String creatorRole, String assignedDoctor, String priority,
                 String status, String category, LocalDateTime createdDate, LocalDateTime slaDeadline,
                 int attachmentCount, int commentCount, String aiSuggestion) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.patientId = patientId;
        this.patientName = patientName;
        this.creatorName = creatorName;
        this.creatorRole = creatorRole;
        this.assignedDoctor = assignedDoctor;
        this.priority = priority;
        this.status = status;
        this.category = category;
        this.createdDate = createdDate;
        this.slaDeadline = slaDeadline;
        this.attachmentCount = attachmentCount;
        this.commentCount = commentCount;
        this.aiSuggestion = aiSuggestion;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorRole() { return creatorRole; }
    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }

    public String getAssignedDoctor() { return assignedDoctor; }
    public void setAssignedDoctor(String assignedDoctor) { this.assignedDoctor = assignedDoctor; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public int getAttachmentCount() { return attachmentCount; }
    public void setAttachmentCount(int attachmentCount) { this.attachmentCount = attachmentCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public String getAiSuggestion() { return aiSuggestion; }
    public void setAiSuggestion(String aiSuggestion) { this.aiSuggestion = aiSuggestion; }
}
