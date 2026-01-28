package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho Ticket entity
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    /**
     * Tìm ticket theo bệnh nhân
     */
    List<Ticket> findByPatient(User patient);
    
    /**
     * Tìm ticket theo ID bệnh nhân
     */
    List<Ticket> findByPatientId(Long patientId);
    
    /**
     * Tìm ticket theo trạng thái
     */
    List<Ticket> findByStatus(Ticket.Status status);
    
    /**
     * Tìm ticket theo mức độ ưu tiên
     */
    List<Ticket> findByPriority(Ticket.Priority priority);
    
    /**
     * Tìm ticket theo danh mục
     */
    List<Ticket> findByCategory(Ticket.Category category);
    
    /**
     * Tìm ticket được gán cho user
     */
    List<Ticket> findByAssignedTo(User assignedTo);
    
    /**
     * Tìm ticket được gán cho user ID
     */
    List<Ticket> findByAssignedToId(Long assignedToId);
    
    /**
     * Tìm ticket do user tạo
     */
    List<Ticket> findByCreatedById(Long createdById);
    
    /**
     * Tìm ticket chưa được gán
     */
    List<Ticket> findByAssignedToIsNullAndStatus(Ticket.Status status);
    
    /**
     * Tìm ticket đang mở và chưa gán
     */
    @Query("SELECT t FROM Ticket t WHERE t.assignedTo IS NULL AND t.status = 'OPEN' ORDER BY t.priority DESC, t.createdAt ASC")
    List<Ticket> findUnassignedOpenTickets();
    
    /**
     * Tìm ticket khẩn cấp
     */
    @Query("SELECT t FROM Ticket t WHERE t.priority = 'URGENT' AND t.status NOT IN ('RESOLVED', 'CLOSED') ORDER BY t.createdAt ASC")
    List<Ticket> findUrgentUnresolvedTickets();
    
    /**
     * Phân trang ticket
     */
    Page<Ticket> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Phân trang ticket theo trạng thái
     */
    Page<Ticket> findByStatusOrderByCreatedAtDesc(Ticket.Status status, Pageable pageable);
    
    /**
     * Đếm ticket theo trạng thái
     */
    long countByStatus(Ticket.Status status);
    
    /**
     * Đếm ticket của bệnh nhân
     */
    long countByPatient(User patient);
    
    /**
     * Đếm ticket của bệnh nhân theo trạng thái
     */
    long countByPatientAndStatus(User patient, Ticket.Status status);
    
    /**
     * Đếm ticket được gán cho user
     */
    long countByAssignedToIdAndStatus(Long assignedToId, Ticket.Status status);
    
    /**
     * Tìm ticket từ cuộc gọi
     */
    List<Ticket> findByCallLogId(Long callLogId);
    
    /**
     * Thống kê ticket theo ngày
     */
    @Query("SELECT DATE(t.createdAt) as date, COUNT(t) as count FROM Ticket t WHERE t.createdAt >= :startDate GROUP BY DATE(t.createdAt)")
    List<Object[]> countTicketsByDate(@Param("startDate") LocalDateTime startDate);
}
