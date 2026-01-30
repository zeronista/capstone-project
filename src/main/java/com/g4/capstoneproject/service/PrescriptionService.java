package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.PrescriptionRequest;
import com.g4.capstoneproject.dto.Precription.PrescriptionCreateRequest;
import com.g4.capstoneproject.entity.Prescription;
import com.g4.capstoneproject.entity.PrescriptionDetail;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.PrescriptionRepository;
import com.g4.capstoneproject.repository.PrescriptionDetailRepository;
import com.g4.capstoneproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service quản lý đơn thuốc
 * Refactored to use database in Phase 2
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDetailRepository prescriptionDetailRepository;
    private final UserRepository userRepository;

    /**
     * Lấy tất cả đơn thuốc
     */
    @Transactional(readOnly = true)
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    /**
     * Lấy đơn thuốc theo ID (trả về Prescription object, không phải Optional)
     */
    @Transactional(readOnly = true)
    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id).orElse(null);
    }

    /**
     * Lọc đơn thuốc theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<Prescription> getPrescriptionsByStatus(Prescription.PrescriptionStatus status) {
        return prescriptionRepository.findByStatus(status);
    }

    /**
     * Lấy đơn thuốc theo bệnh nhân
     */
    @Transactional(readOnly = true)
    public List<Prescription> getPrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    /**
     * Lấy đơn thuốc theo bác sĩ
     */
    @Transactional(readOnly = true)
    public List<Prescription> getPrescriptionsByDoctorId(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }

    /**
     * Lấy đơn thuốc theo ngày
     */
    @Transactional(readOnly = true)
    public List<Prescription> getPrescriptionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return prescriptionRepository.findByPrescriptionDateBetween(startDate, endDate);
    }

    /**
     * Tạo đơn thuốc mới
     */
    public Prescription createPrescription(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    /**
     * Cập nhật đơn thuốc
     */
    public Prescription updatePrescription(Long id, Prescription updatedPrescription) {
        return prescriptionRepository.findById(id)
                .map(existing -> {
                    existing.setDiagnosis(updatedPrescription.getDiagnosis());
                    existing.setNotes(updatedPrescription.getNotes());
                    existing.setStatus(updatedPrescription.getStatus());
                    existing.setPrescriptionDate(updatedPrescription.getPrescriptionDate());
                    return prescriptionRepository.save(existing);
                })
                .orElse(null);
    }

    /**
     * Xóa đơn thuốc
     */
    public boolean deletePrescription(Long id) {
        if (prescriptionRepository.existsById(id)) {
            prescriptionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Thống kê số lượng đơn thuốc
     */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return prescriptionRepository.count();
    }

    @Transactional(readOnly = true)
    public long getActiveCount() {
        return prescriptionRepository.countByStatus(Prescription.PrescriptionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long getCompletedCount() {
        return prescriptionRepository.countByStatus(Prescription.PrescriptionStatus.COMPLETED);
    }

    /**
     * Thêm thuốc vào đơn
     */
    public PrescriptionDetail addDetail(Long prescriptionId, PrescriptionDetail detail) {
        return prescriptionRepository.findById(prescriptionId)
                .map(prescription -> {
                    detail.setPrescription(prescription);
                    return prescriptionDetailRepository.save(detail);
                })
                .orElse(null);
    }

    /**
     * Lấy chi tiết đơn thuốc
     */
    @Transactional(readOnly = true)
    public List<PrescriptionDetail> getPrescriptionDetails(Long prescriptionId) {
        return prescriptionDetailRepository.findByPrescriptionId(prescriptionId);
    }

    /**
     * Lấy lịch sử kê đơn của bệnh nhân (sắp xếp theo ngày mới nhất)
     */
    @Transactional(readOnly = true)
    public List<Prescription> getPrescriptionHistory(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId);
    }

    /**
     * Kiểm tra trạng thái sử dụng thuốc
     * Trả về số lần đã kê thuốc cho một loại thuốc cụ thể của bệnh nhân
     */
    @Transactional(readOnly = true)
    public int checkMedicationUsageCount(Long patientId, String medicineName) {
        List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId);
        int count = 0;

        for (Prescription prescription : prescriptions) {
            for (PrescriptionDetail detail : prescription.getDetails()) {
                if (detail.getMedicineName().equalsIgnoreCase(medicineName)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Tạo đơn thuốc từ DTO với validation
     */
    public Prescription createPrescriptionFromRequest(PrescriptionRequest request) {
        // Validate patient exists
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy bệnh nhân với ID: " + request.getPatientId()));

        if (patient.getRole() != User.UserRole.PATIENT) {
            throw new IllegalArgumentException("User không phải là bệnh nhân");
        }

        // Validate doctor exists
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));

        if (doctor.getRole() != User.UserRole.DOCTOR) {
            throw new IllegalArgumentException("User không phải là bác sĩ");
        }

        // Create prescription
        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .diagnosis(request.getDiagnosis())
                .notes(request.getNotes())
                .prescriptionDate(LocalDate.now())
                .status(Prescription.PrescriptionStatus.ACTIVE)
                .build();

        // Save prescription first
        prescription = prescriptionRepository.save(prescription);

        // Create prescription details
        final Prescription savedPrescription = prescription;
        for (PrescriptionRequest.MedicationItem medication : request.getMedications()) {
            PrescriptionDetail detail = PrescriptionDetail.builder()
                    .prescription(savedPrescription)
                    .medicineName(medication.getName())
                    .dosage(medication.getDosage())
                    .quantity(medication.getQuantity())
                    .instructions(medication.getInstructions())
                    .build();

            prescriptionDetailRepository.save(detail);
        }

        // Update revisit date if required
        if (request.getRequireRevisit() != null && request.getRequireRevisit()) {
            // TODO: Create follow-up appointment or set revisit date in prescription
            // For now, we can add this to notes
            String revisitNote = "\nTái khám: " + request.getRevisitDate();
            prescription.setNotes((prescription.getNotes() != null ? prescription.getNotes() : "") + revisitNote);
            prescription = prescriptionRepository.save(prescription);
        }

        return prescription;
    }

    /**
     * Tạo đơn thuốc từ PrescriptionCreateRequest DTO (for REST API)
     */
    public Prescription createPrescriptionFromRequest(PrescriptionCreateRequest request, User doctor, User patient) {
        // Validate roles
        if (patient.getRole() != User.UserRole.PATIENT) {
            throw new IllegalArgumentException("User không phải là bệnh nhân");
        }

        if (doctor.getRole() != User.UserRole.DOCTOR) {
            throw new IllegalArgumentException("User không phải là bác sĩ");
        }

        // Create prescription
        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .diagnosis(request.getDiagnosis())
                .notes(request.getNotes())
                .prescriptionDate(LocalDate.now())
                .status(request.getStatus() != null ? request.getStatus() : Prescription.PrescriptionStatus.ACTIVE)
                .build();

        // Save prescription first
        prescription = prescriptionRepository.save(prescription);

        // Create prescription details
        final Prescription savedPrescription = prescription;
        for (PrescriptionCreateRequest.MedicationItemDTO medication : request.getMedications()) {
            PrescriptionDetail detail = PrescriptionDetail.builder()
                    .prescription(savedPrescription)
                    .medicineName(medication.getMedicineName())
                    .dosage(medication.getDosage())
                    .frequency(medication.getFrequency())
                    .duration(medication.getDuration())
                    .quantity(medication.getQuantity())
                    .instructions(medication.getInstructions())
                    .build();

            prescriptionDetailRepository.save(detail);
        }

        return prescription;
    }

    /**
     * Cập nhật đơn thuốc từ PrescriptionCreateRequest DTO (for REST API)
     */
    public Prescription updatePrescriptionFromRequest(Long id, PrescriptionCreateRequest request, User doctor) {
        Prescription existingPrescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn thuốc với ID: " + id));

        // Validate doctor ownership
        if (!existingPrescription.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền cập nhật đơn thuốc này");
        }

        // Update prescription fields
        existingPrescription.setDiagnosis(request.getDiagnosis());
        existingPrescription.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            existingPrescription.setStatus(request.getStatus());
        }

        // Delete old prescription details
        prescriptionDetailRepository.deleteByPrescriptionId(id);

        // Create new prescription details
        for (PrescriptionCreateRequest.MedicationItemDTO medication : request.getMedications()) {
            PrescriptionDetail detail = PrescriptionDetail.builder()
                    .prescription(existingPrescription)
                    .medicineName(medication.getMedicineName())
                    .dosage(medication.getDosage())
                    .frequency(medication.getFrequency())
                    .duration(medication.getDuration())
                    .quantity(medication.getQuantity())
                    .instructions(medication.getInstructions())
                    .build();

            prescriptionDetailRepository.save(detail);
        }

        return prescriptionRepository.save(existingPrescription);
    }
}
