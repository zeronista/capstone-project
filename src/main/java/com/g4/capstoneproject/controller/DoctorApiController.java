package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.dto.Precription.PrescriptionCreateRequest;
import com.g4.capstoneproject.dto.Precription.PrescriptionDetailResponse;
import com.g4.capstoneproject.dto.Precription.PrescriptionResponse;
import com.g4.capstoneproject.dto.Ticket.TicketDetailResponse;
import com.g4.capstoneproject.dto.Ticket.TicketMessageRequest;
import com.g4.capstoneproject.dto.Ticket.TicketResponse;
import com.g4.capstoneproject.dto.Ticket.TicketStatusUpdateRequest;
import com.g4.capstoneproject.entity.Prescription;
import com.g4.capstoneproject.entity.Ticket;
import com.g4.capstoneproject.entity.TicketMessage;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.PrescriptionService;
import com.g4.capstoneproject.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pure REST controller cho các API của Bác sĩ dưới prefix /api/doctor.
 * Tách riêng khỏi {@link DoctorController} (controller render HTML) để tránh
 * xung đột với class-level @RequestMapping("/doctor") và đảm bảo URL API
 * đúng như FE đang gọi: /api/doctor/...
 */
@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
@Slf4j
public class DoctorApiController {

    private final TicketService ticketService;
    private final PrescriptionService prescriptionService;
    private final UserRepository userRepository;

    // ==================== TICKETS ====================

    /**
     * GET /api/doctor/tickets
     * Lấy tất cả ticket được assign cho bác sĩ hiện tại.
     */
    @GetMapping("/tickets")
    public ResponseEntity<Map<String, Object>> getTickets(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Ticket.Status status,
            @RequestParam(required = false) Ticket.Priority priority) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Ticket> tickets = ticketService.getTicketsByAssignedUserId(doctor.getId());

        // Apply filters if provided
        if (status != null) {
            tickets = tickets.stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
        }

        if (priority != null) {
            tickets = tickets.stream()
                    .filter(t -> t.getPriority() == priority)
                    .collect(Collectors.toList());
        }

        List<TicketResponse> ticketResponses = tickets.stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("tickets", ticketResponses);
        response.put("total", ticketResponses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/doctor/tickets/{id}
     * Lấy chi tiết ticket + message, chỉ khi ticket được assign cho bác sĩ hiện tại.
     */
    @GetMapping("/tickets/{id}")
    public ResponseEntity<TicketDetailResponse> getTicketDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<TicketMessage> messages = ticketService.getTicketMessages(id);
        TicketDetailResponse response = TicketDetailResponse.fromEntity(ticket, messages);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/doctor/tickets/{id}/status
     * Bác sĩ cập nhật trạng thái ticket được assign cho mình.
     */
    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<Map<String, Object>> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Ticket updatedTicket = ticketService.updateTicketStatus(id, request.getStatus(), doctor);

        if (request.getNote() != null && !request.getNote().isEmpty()) {
            TicketMessage systemMessage = TicketMessage.builder()
                    .ticket(updatedTicket)
                    .sender(doctor)
                    .messageText("Trạng thái thay đổi: " + request.getStatus() + ". " + request.getNote())
                    .messageType(TicketMessage.MessageType.SYSTEM)
                    .isInternalNote(false)
                    .build();
            ticketService.addMessage(id, systemMessage);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật trạng thái thành công");
        response.put("ticket", TicketResponse.fromEntity(updatedTicket));

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/doctor/tickets/{id}/messages
     * Bác sĩ gửi tin nhắn trong ticket được assign cho mình.
     */
    @PostMapping("/tickets/{id}/messages")
    public ResponseEntity<Map<String, Object>> addTicketMessage(
            @PathVariable Long id,
            @Valid @RequestBody TicketMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Ticket ticket = ticketService.getTicketById(id.toString());
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }

        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .sender(doctor)
                .messageText(request.getMessageText())
                .messageType(request.getMessageType())
                .attachmentUrl(request.getAttachmentUrl())
                .isInternalNote(request.getIsInternalNote())
                .build();

        TicketMessage savedMessage = ticketService.addMessage(id, message);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Gửi tin nhắn thành công");
        response.put("data", TicketDetailResponse.TicketMessageDTO.fromEntity(savedMessage));

        return ResponseEntity.ok(response);
    }

    // ==================== PRESCRIPTIONS ====================

    /**
     * GET /api/doctor/prescriptions
     * Lấy tất cả đơn thuốc do bác sĩ hiện tại kê.
     */
    @GetMapping("/prescriptions")
    public ResponseEntity<Map<String, Object>> getPrescriptions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Prescription.PrescriptionStatus status,
            @RequestParam(required = false) Long patientId) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctorId(doctor.getId());

        if (status != null) {
            prescriptions = prescriptions.stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        }

        if (patientId != null) {
            prescriptions = prescriptions.stream()
                    .filter(p -> p.getPatient() != null && p.getPatient().getId().equals(patientId))
                    .collect(Collectors.toList());
        }

        List<PrescriptionResponse> prescriptionResponses = prescriptions.stream()
                .map(PrescriptionResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("prescriptions", prescriptionResponses);
        response.put("total", prescriptionResponses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/doctor/prescriptions/{id}
     * Lấy chi tiết đơn thuốc, chỉ khi thuộc về bác sĩ hiện tại.
     */
    @GetMapping("/prescriptions/{id}")
    public ResponseEntity<PrescriptionDetailResponse> getPrescriptionDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Prescription prescription = prescriptionService.getPrescriptionById(id);
        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }

        if (prescription.getDoctor() == null || !prescription.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PrescriptionDetailResponse response = PrescriptionDetailResponse.fromEntity(prescription);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/doctor/prescriptions
     * Tạo đơn thuốc mới.
     */
    @PostMapping("/prescriptions")
    public ResponseEntity<Map<String, Object>> createPrescription(
            @Valid @RequestBody PrescriptionCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy bệnh nhân từ request
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh nhân"));

        Prescription prescription = prescriptionService.createPrescriptionFromRequest(request, doctor, patient);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("prescription", PrescriptionResponse.fromEntity(prescription));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/doctor/prescriptions/{id}
     * Cập nhật đơn thuốc.
     */
    @PutMapping("/prescriptions/{id}")
    public ResponseEntity<Map<String, Object>> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Prescription updated = prescriptionService.updatePrescriptionFromRequest(id, request, doctor);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("prescription", PrescriptionResponse.fromEntity(updated));

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/doctor/prescriptions/{id}
     * Xóa đơn thuốc.
     */
    @DeleteMapping("/prescriptions/{id}")
    public ResponseEntity<Map<String, Object>> deletePrescription(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean deleted = prescriptionService.deletePrescription(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Không tìm thấy đơn thuốc"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa đơn thuốc thành công");

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/doctor/prescriptions/patient/{patientId}/medication/{medicineName}/usage
     * Kiểm tra số lần kê đơn 1 thuốc cho 1 bệnh nhân.
     */
    @GetMapping("/prescriptions/patient/{patientId}/medication/{medicineName}/usage")
    public ResponseEntity<Map<String, Object>> getMedicationUsage(
            @PathVariable Long patientId,
            @PathVariable String medicineName,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User doctor = userRepository.findByEmailOrPhoneNumber(username, username).orElse(null);

        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int usageCount = prescriptionService.checkMedicationUsageCount(patientId, medicineName);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("patientId", patientId);
        response.put("medicineName", medicineName);
        response.put("usageCount", usageCount);

        return ResponseEntity.ok(response);
    }
}

