package com.g4.capstoneproject.service;

import com.g4.capstoneproject.model.TreatmentPlan;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý lộ trình điều trị
 * Chức năng: Cung cấp dữ liệu mock cho treatment plan management
 */
@Service
public class TreatmentPlanService {
    
    private List<TreatmentPlan> treatmentPlans;

    public TreatmentPlanService() {
        // Khởi tạo dữ liệu mock
        treatmentPlans = new ArrayList<>();
        
        treatmentPlans.add(new TreatmentPlan(
            "TP-2026-001",
            "BN-2026-0142",
            "Trần Văn Hùng",
            45,
            "Nam",
            "Tăng huyết áp độ II, Đái tháo đường type 2",
            "Kiểm soát huyết áp <140/90 mmHg, Đưa HbA1c về <7%",
            65,
            "Đang thực hiện",
            LocalDateTime.now().plusDays(7),
            "Mỗi 3 tháng",
            "BS. Nguyễn Văn A",
            LocalDateTime.now(),
            "Bình thường"
        ));
        
        treatmentPlans.add(new TreatmentPlan(
            "TP-2026-002",
            "BN-2026-0138",
            "Võ Thị Thanh",
            65,
            "Nữ",
            "Suy tim độ II, Rung nhĩ mạn tính",
            "Kiểm soát nhịp tim 60-80 bpm, Duy trì INR 2-3",
            50,
            "Đang thực hiện",
            LocalDateTime.now().plusDays(2),
            "Mỗi 2 tuần",
            "BS. Lê Minh Tuấn",
            LocalDateTime.now().minusDays(3),
            "Ưu tiên cao"
        ));
        
        treatmentPlans.add(new TreatmentPlan(
            "TP-2026-003",
            "BN-2026-0141",
            "Lê Thị Mai",
            32,
            "Nữ",
            "Rối loạn chức năng tuyến giáp, Thiếu máu",
            "Ổn định TSH trong giới hạn bình thường, Nâng Hemoglobin >12 g/dL",
            40,
            "Cần theo dõi",
            LocalDateTime.now().plusDays(4),
            "Mỗi 6 tuần",
            "BS. Trần Thị Hoa",
            LocalDateTime.now(),
            "Bình thường"
        ));
    }

    /**
     * Lấy tất cả lộ trình điều trị
     */
    public List<TreatmentPlan> getAllTreatmentPlans() {
        return treatmentPlans;
    }

    /**
     * Lấy lộ trình theo ID
     */
    public TreatmentPlan getTreatmentPlanById(String id) {
        return treatmentPlans.stream()
            .filter(tp -> tp.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Lọc theo trạng thái
     */
    public List<TreatmentPlan> getTreatmentPlansByStatus(String status) {
        return treatmentPlans.stream()
            .filter(tp -> tp.getStatus().equals(status))
            .collect(Collectors.toList());
    }

    /**
     * Tạo lộ trình mới
     */
    public TreatmentPlan createTreatmentPlan(TreatmentPlan plan) {
        treatmentPlans.add(plan);
        return plan;
    }

    /**
     * Cập nhật lộ trình
     */
    public TreatmentPlan updateTreatmentPlan(String id, TreatmentPlan updatedPlan) {
        for (int i = 0; i < treatmentPlans.size(); i++) {
            if (treatmentPlans.get(i).getId().equals(id)) {
                treatmentPlans.set(i, updatedPlan);
                return updatedPlan;
            }
        }
        return null;
    }

    /**
     * Xóa lộ trình
     */
    public boolean deleteTreatmentPlan(String id) {
        return treatmentPlans.removeIf(tp -> tp.getId().equals(id));
    }

    /**
     * Thống kê
     */
    public long getActiveCount() {
        return treatmentPlans.stream()
            .filter(tp -> tp.getStatus().equals("Đang thực hiện"))
            .count();
    }

    public long getUpcomingFollowUpCount() {
        LocalDateTime weekFromNow = LocalDateTime.now().plusWeeks(1);
        return treatmentPlans.stream()
            .filter(tp -> tp.getNextFollowUp().isBefore(weekFromNow))
            .count();
    }
}
