package com.g4.capstoneproject.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Model đơn thuốc (Prescription)
 * Chức năng: Quản lý đơn thuốc của bệnh nhân
 */
public class Prescription {
    private String id;
    private String patientId;
    private String patientName;
    private String doctorName;
    private int medicationCount;
    private LocalDate prescriptionDate;
    private String status; // "Chờ xử lý", "Đã xác nhận", "Đã phát thuốc"
    private boolean hasDrugInteraction;
    private List<Medication> medications;
    private String diagnosis;
    private String notes;

    // Constructors
    public Prescription() {}

    public Prescription(String id, String patientId, String patientName, String doctorName, 
                       int medicationCount, LocalDate prescriptionDate, String status, boolean hasDrugInteraction) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.medicationCount = medicationCount;
        this.prescriptionDate = prescriptionDate;
        this.status = status;
        this.hasDrugInteraction = hasDrugInteraction;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public int getMedicationCount() { return medicationCount; }
    public void setMedicationCount(int medicationCount) { this.medicationCount = medicationCount; }

    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isHasDrugInteraction() { return hasDrugInteraction; }
    public void setHasDrugInteraction(boolean hasDrugInteraction) { this.hasDrugInteraction = hasDrugInteraction; }

    public List<Medication> getMedications() { return medications; }
    public void setMedications(List<Medication> medications) { this.medications = medications; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

/**
 * Model thuốc trong đơn
 */
class Medication {
    private String name;
    private String dosage;
    private String frequency;
    private int duration;

    public Medication(String name, String dosage, String frequency, int duration) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}
