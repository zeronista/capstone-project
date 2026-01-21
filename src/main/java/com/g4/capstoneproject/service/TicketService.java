package com.g4.capstoneproject.service;

import com.g4.capstoneproject.model.Ticket;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service quản lý Ticket chuyên khoa
 * Chức năng: Cung cấp dữ liệu mock cho ticket management
 */
@Service
public class TicketService {
    
    private List<Ticket> tickets;

    public TicketService() {
        // Khởi tạo dữ liệu mock
        tickets = new ArrayList<>();
        
        tickets.add(new Ticket(
            "TK-2026-0042",
            "Tư vấn điều trị bệnh nhân suy tim độ III kèm suy thận mãn",
            "Bệnh nhân nam 68 tuổi, tiền sử suy tim mạn NYHA III, hiện tại creatinine 2.8 mg/dL, eGFR 28 ml/min. Cần tư vấn điều chỉnh liều thuốc và phác đồ điều trị phù hợp với chức năng thận giảm...",
            "BN-2026-0098",
            "Nguyễn Văn Tuấn",
            "NV. Trần Thị Mai",
            "Y tá",
            "BS. Nguyễn Văn A",
            "Ưu tiên cao",
            "Chờ phản hồi",
            "Tư vấn chuyên khoa",
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().plusHours(5),
            3,
            2,
            "Theo ESC 2021 Heart Failure Guidelines: Với eGFR 20-30 ml/min, khuyến cáo giảm liều ACE-I/ARB 50%, theo dõi K+ và creatinine hàng tuần..."
        ));
        
        tickets.add(new Ticket(
            "TK-2026-0041",
            "Hội chẩn trường hợp bệnh nhân ĐTĐ type 2 kiểm soát kém",
            "Bệnh nhân nữ 52 tuổi, ĐTĐ type 2 được 8 năm, HbA1c hiện tại 9.2%. Đang dùng Metformin 2000mg + Glimepiride 4mg nhưng đường huyết vẫn cao...",
            "BN-2026-0087",
            "Lê Thị Hương",
            "BS. Trần Văn Bình",
            "Bác sĩ nội tổng quát",
            "BS. Nguyễn Văn A",
            "Ưu tiên trung bình",
            "Đang xử lý",
            "Hội chẩn",
            LocalDateTime.now().minusHours(5),
            LocalDateTime.now().plusDays(1),
            5,
            4,
            "Theo ADA Standards of Care 2024, khuyến cáo bổ sung GLP-1 RA hoặc SGLT2i cho bệnh nhân với HbA1c >8%..."
        ));
    }

    /**
     * Lấy tất cả tickets
     */
    public List<Ticket> getAllTickets() {
        return tickets;
    }

    /**
     * Lấy ticket theo ID
     */
    public Ticket getTicketById(String id) {
        return tickets.stream()
            .filter(t -> t.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Lọc theo trạng thái
     */
    public List<Ticket> getTicketsByStatus(String status) {
        return tickets.stream()
            .filter(t -> t.getStatus().equals(status))
            .collect(Collectors.toList());
    }

    /**
     * Lọc theo priority
     */
    public List<Ticket> getTicketsByPriority(String priority) {
        return tickets.stream()
            .filter(t -> t.getPriority().equals(priority))
            .collect(Collectors.toList());
    }

    /**
     * Tạo ticket mới
     */
    public Ticket createTicket(Ticket ticket) {
        tickets.add(ticket);
        return ticket;
    }

    /**
     * Cập nhật ticket
     */
    public Ticket updateTicket(String id, Ticket updatedTicket) {
        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).getId().equals(id)) {
                tickets.set(i, updatedTicket);
                return updatedTicket;
            }
        }
        return null;
    }

    /**
     * Thống kê
     */
    public long getOpenCount() {
        return tickets.stream()
            .filter(t -> !t.getStatus().equals("Hoàn thành"))
            .count();
    }

    public long getInProgressCount() {
        return tickets.stream()
            .filter(t -> t.getStatus().equals("Đang xử lý"))
            .count();
    }

    public long getHighPriorityCount() {
        return tickets.stream()
            .filter(t -> t.getPriority().equals("Ưu tiên cao"))
            .count();
    }
}
