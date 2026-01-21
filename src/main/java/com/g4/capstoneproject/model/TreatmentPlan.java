package com.g4.capstoneproject.model;

import java.time.LocalDateTime;

/**
 * Model lộ trình điều trị (Treatment Plan)
 * Chức năng: Quản lý kế hoạch điều trị dài hạn cho bệnh nhân mãn tính
 */
public class TreatmentPlan {
    private String id;
    private String patientId;
    private String patientName;
    private int patientAge;
    private String patientGender;
    private String diagnosis;
    private String goals;
    private int progress; // 0-100%
    private String status; // "Đang thực hiện", "Hoàn thành", "Đã hủy"
    private LocalDateTime nextFollowUp;
    private String followUpPeriod; // "Mỗi 2 tuần", "Mỗi 3 tháng", etc.
    private String doctorName;
    private LocalDateTime lastUpdated;
    private String priority; // "Ưu tiên cao", "Bình thường", "Thấp"

    // Constructors
    public TreatmentPlan() {}

    public TreatmentPlan(String id, String patientId, String patientName, int patientAge, 
                        String patientGender, String diagnosis, String goals, int progress,
                        String status, LocalDateTime nextFollowUp, String followUpPeriod,
                        String doctorName, LocalDateTime lastUpdated, String priority) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.diagnosis = diagnosis;
        this.goals = goals;
        this.progress = progress;
        this.status = status;
        this.nextFollowUp = nextFollowUp;
        this.followUpPeriod = followUpPeriod;
        this.doctorName = doctorName;
        this.lastUpdated = lastUpdated;
        this.priority = priority;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public int getPatientAge() { return patientAge; }
    public void setPatientAge(int patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getNextFollowUp() { return nextFollowUp; }
    public void setNextFollowUp(LocalDateTime nextFollowUp) { this.nextFollowUp = nextFollowUp; }

    public String getFollowUpPeriod() { return followUpPeriod; }
    public void setFollowUpPeriod(String followUpPeriod) { this.followUpPeriod = followUpPeriod; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
