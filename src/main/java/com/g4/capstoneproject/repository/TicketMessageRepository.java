package com.g4.capstoneproject.repository;

import com.g4.capstoneproject.entity.TicketMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho TicketMessage entity
 */
@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
    
    /**
     * Tìm tin nhắn theo ticket
     */
    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
    
    /**
     * Tìm tin nhắn theo ticket (phân trang)
     */
    Page<TicketMessage> findByTicketIdOrderByCreatedAtDesc(Long ticketId, Pageable pageable);
    
    /**
     * Tìm tin nhắn do user gửi
     */
    List<TicketMessage> findBySenderId(Long senderId);
    
    /**
     * Tìm tin nhắn không phải ghi chú nội bộ
     */
    @Query("SELECT m FROM TicketMessage m WHERE m.ticket.id = :ticketId AND m.isInternalNote = false ORDER BY m.createdAt ASC")
    List<TicketMessage> findPublicMessagesByTicketId(@Param("ticketId") Long ticketId);
    
    /**
     * Tìm ghi chú nội bộ
     */
    @Query("SELECT m FROM TicketMessage m WHERE m.ticket.id = :ticketId AND m.isInternalNote = true ORDER BY m.createdAt DESC")
    List<TicketMessage> findInternalNotesByTicketId(@Param("ticketId") Long ticketId);
    
    /**
     * Đếm số tin nhắn trong ticket
     */
    long countByTicketId(Long ticketId);
    
    /**
     * Lấy tin nhắn mới nhất của ticket
     */
    @Query("SELECT m FROM TicketMessage m WHERE m.ticket.id = :ticketId ORDER BY m.createdAt DESC LIMIT 1")
    TicketMessage findLatestByTicketId(@Param("ticketId") Long ticketId);
}
