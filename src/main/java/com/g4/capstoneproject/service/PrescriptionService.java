package com.g4.capstoneproject.service;

import com.g4.capstoneproject.model.Prescription;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý đơn thuốc
 * Chức năng: Cung cấp dữ liệu mock cho prescription management
 */
@Service
public class PrescriptionService {
    
    private List<Prescription> prescriptions;

    public PrescriptionService() {
        // Khởi tạo dữ liệu mock
        prescriptions = new ArrayList<>();
        prescriptions.add(new Prescription(
            "RX-2026-0089",
            "BN-2026-0142",
            "Trần Văn Hùng",
            "BS. Nguyễn Văn A",
            4,
            LocalDate.now(),
            "Đã xác nhận",
            false
        ));
        prescriptions.add(new Prescription(
            "RX-2026-0088",
            "BN-2026-0141",
            "Lê Thị Mai",
            "BS. Trần Thị Hoa",
            6,
            LocalDate.now().minusDays(1),
            "Chờ xử lý",
            true // Có tương tác thuốc
        ));
        prescriptions.add(new Prescription(
            "RX-2026-0087",
            "BN-2026-0140",
            "Phạm Văn Đức",
            "BS. Lê Minh Tuấn",
            3,
            LocalDate.now().minusDays(2),
            "Đã phát thuốc",
            false
        ));
    }

    /**
     * Lấy tất cả đơn thuốc
     */
    public List<Prescription> getAllPrescriptions() {
        return prescriptions;
    }

    /**
     * Lấy đơn thuốc theo ID
     */
    public Prescription getPrescriptionById(String id) {
        return prescriptions.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Lọc đơn thuốc theo trạng thái
     */
    public List<Prescription> getPrescriptionsByStatus(String status) {
        return prescriptions.stream()
            .filter(p -> p.getStatus().equals(status))
            .collect(Collectors.toList());
    }

    /**
     * Tạo đơn thuốc mới
     */
    public Prescription createPrescription(Prescription prescription) {
        prescriptions.add(prescription);
        return prescription;
    }

    /**
     * Cập nhật đơn thuốc
     */
    public Prescription updatePrescription(String id, Prescription updatedPrescription) {
        for (int i = 0; i < prescriptions.size(); i++) {
            if (prescriptions.get(i).getId().equals(id)) {
                prescriptions.set(i, updatedPrescription);
                return updatedPrescription;
            }
        }
        return null;
    }

    /**
     * Xóa đơn thuốc
     */
    public boolean deletePrescription(String id) {
        return prescriptions.removeIf(p -> p.getId().equals(id));
    }

    /**
     * Thống kê số lượng đơn thuốc
     */
    public long getTotalCount() {
        return prescriptions.size();
    }

    public long getPendingCount() {
        return prescriptions.stream()
            .filter(p -> p.getStatus().equals("Chờ xử lý"))
            .count();
    }

    public long getWarningCount() {
        return prescriptions.stream()
            .filter(Prescription::isHasDrugInteraction)
            .count();
    }
}
