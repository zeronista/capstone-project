package com.g4.capstoneproject.service;

import com.g4.capstoneproject.dto.WebCallDTO;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.WebCallLog;
import com.g4.capstoneproject.entity.WebCallLog.WebCallStatus;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.repository.WebCallLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service quản lý cuộc gọi Web-to-Web
 */
@Service
public class WebCallService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebCallService.class);
    
    @Autowired
    private WebCallLogRepository webCallLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private S3Service s3Service;
    
    /**
     * Lưu trữ danh sách user đang online
     * Key: userId (Long), Value: stringeeUserId (String)
     */
    private final Map<Long, OnlineUser> onlineUsers = new ConcurrentHashMap<>();
    
    /**
     * Đăng ký user online
     */
    public void registerOnline(Long userId, String stringeeUserId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            onlineUsers.put(userId, new OnlineUser(
                userId,
                stringeeUserId,
                user.getFullName(),
                user.getRole().name(),
                user.getAvatarUrl(),
                LocalDateTime.now()
            ));
            logger.info("User {} ({}) registered as online with stringeeId: {}", 
                userId, user.getFullName(), stringeeUserId);
        }
    }
    
    /**
     * Đăng ký user offline
     */
    public void registerOffline(Long userId) {
        OnlineUser removed = onlineUsers.remove(userId);
        if (removed != null) {
            logger.info("User {} ({}) is now offline", userId, removed.getFullName());
        }
    }
    
    /**
     * Lấy danh sách user đang online (trừ bản thân)
     */
    public List<OnlineUser> getOnlineUsers(Long excludeUserId) {
        return onlineUsers.values().stream()
            .filter(u -> !u.getUserId().equals(excludeUserId))
            .sorted(Comparator.comparing(OnlineUser::getFullName))
            .collect(Collectors.toList());
    }
    
    /**
     * Kiểm tra user có online không
     */
    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }
    
    /**
     * Lấy Stringee User ID của user
     */
    public String getStringeeUserId(Long userId) {
        OnlineUser user = onlineUsers.get(userId);
        return user != null ? user.getStringeeUserId() : null;
    }
    
    /**
     * Khởi tạo cuộc gọi mới
     */
    @Transactional
    public WebCallLog initiateCall(Long callerId, Long receiverId, String stringeeCallId) {
        User caller = userRepository.findById(callerId)
            .orElseThrow(() -> new RuntimeException("Caller not found"));
        User receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        WebCallLog callLog = WebCallLog.builder()
            .stringeeCallId(stringeeCallId)
            .caller(caller)
            .receiver(receiver)
            .callStatus(WebCallStatus.INITIATED)
            .startTime(LocalDateTime.now())
            .hasRecording(false)
            .build();
        
        callLog = webCallLogRepository.save(callLog);
        logger.info("Call initiated: {} -> {} (callId: {})", callerId, receiverId, callLog.getId());
        
        return callLog;
    }
    
    /**
     * Cập nhật trạng thái cuộc gọi
     */
    @Transactional
    public WebCallLog updateCallStatus(Long callId, WebCallStatus status) {
        WebCallLog callLog = webCallLogRepository.findById(callId)
            .orElseThrow(() -> new RuntimeException("Call not found"));
        
        callLog.setCallStatus(status);
        
        // Nếu kết thúc cuộc gọi, tính duration
        if (status == WebCallStatus.COMPLETED || status == WebCallStatus.MISSED || 
            status == WebCallStatus.REJECTED || status == WebCallStatus.CANCELLED ||
            status == WebCallStatus.FAILED) {
            callLog.setEndTime(LocalDateTime.now());
            if (callLog.getStartTime() != null) {
                long seconds = java.time.Duration.between(callLog.getStartTime(), callLog.getEndTime()).getSeconds();
                callLog.setDuration((int) seconds);
            }
        }
        
        callLog = webCallLogRepository.save(callLog);
        logger.info("Call {} status updated to {}", callId, status);
        
        return callLog;
    }
    
    /**
     * Cập nhật trạng thái cuộc gọi theo Stringee Call ID
     */
    @Transactional
    public WebCallLog updateCallStatusByStringeeId(String stringeeCallId, WebCallStatus status) {
        Optional<WebCallLog> callLogOpt = webCallLogRepository.findByStringeeCallId(stringeeCallId);
        if (callLogOpt.isPresent()) {
            return updateCallStatus(callLogOpt.get().getId(), status);
        }
        logger.warn("Call not found for stringeeCallId: {}", stringeeCallId);
        return null;
    }
    
    /**
     * Lưu thông tin ghi âm (hỗ trợ cả 3 loại: caller, receiver, combined)
     * @param callId ID cuộc gọi
     * @param recordingType Loại recording: "caller", "receiver", hoặc "combined"
     * @param s3Key S3 key của file
     * @param presignedUrl Pre-signed URL
     */
    @Transactional
    public WebCallLog saveRecording(Long callId, String recordingType, String s3Key, String presignedUrl) {
        WebCallLog callLog = webCallLogRepository.findById(callId)
            .orElseThrow(() -> new RuntimeException("Call not found"));
        
        // Tạo folder nếu chưa có
        if (callLog.getRecordingFolder() == null) {
            String folder = "voice/calls/" + callId + "/";
            callLog.setRecordingFolder(folder);
        }
        
        // Lưu theo loại recording
        switch (recordingType.toLowerCase()) {
            case "caller":
                callLog.setRecordingCallerS3Key(s3Key);
                callLog.setRecordingCallerUrl(presignedUrl);
                break;
            case "receiver":
                callLog.setRecordingReceiverS3Key(s3Key);
                callLog.setRecordingReceiverUrl(presignedUrl);
                break;
            case "combined":
            default:
                callLog.setRecordingS3Key(s3Key);
                callLog.setRecordingUrl(presignedUrl);
                break;
        }
        
        callLog.setRecordingUrlExpiry(LocalDateTime.now().plusDays(7));
        callLog.setHasRecording(true);
        
        callLog = webCallLogRepository.save(callLog);
        logger.info("Recording ({}) saved for call {}: {}", recordingType, callId, s3Key);
        
        return callLog;
    }
    
    /**
     * Lưu thông tin ghi âm (backward compatible - mặc định là combined)
     */
    @Transactional
    public WebCallLog saveRecording(Long callId, String s3Key, String presignedUrl) {
        return saveRecording(callId, "combined", s3Key, presignedUrl);
    }
    
    /**
     * Lấy lịch sử cuộc gọi của user
     */
    public List<WebCallDTO> getCallHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WebCallLog> calls = webCallLogRepository.findAllByUserIdPaged(userId, pageable);
        
        return calls.getContent().stream()
            .map(call -> convertToDTO(call, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả cuộc gọi của user
     */
    public List<WebCallDTO> getAllCalls(Long userId) {
        List<WebCallLog> calls = webCallLogRepository.findAllByUserId(userId);
        return calls.stream()
            .map(call -> convertToDTO(call, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy cuộc gọi có ghi âm
     */
    public List<WebCallDTO> getCallsWithRecording(Long userId) {
        List<WebCallLog> calls = webCallLogRepository.findCallsWithRecording(userId);
        return calls.stream()
            .map(call -> convertToDTO(call, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả cuộc gọi của bệnh nhân (cho receptionist xem)
     * @param patientId ID của bệnh nhân
     * @return Danh sách cuộc gọi của bệnh nhân
     */
    public List<WebCallDTO> getCallsByPatientId(Long patientId) {
        List<WebCallLog> calls = webCallLogRepository.findAllByUserId(patientId);
        return calls.stream()
            .map(call -> convertToDTO(call, patientId))
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy cuộc gọi nhỡ
     */
    public List<WebCallDTO> getMissedCalls(Long userId) {
        List<WebCallLog> calls = webCallLogRepository.findMissedCallsByReceiverId(userId);
        return calls.stream()
            .map(call -> convertToDTO(call, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy chi tiết cuộc gọi
     */
    public WebCallDTO getCallDetail(Long callId, Long userId) {
        WebCallLog call = webCallLogRepository.findById(callId)
            .orElseThrow(() -> new RuntimeException("Call not found"));
        
        // Kiểm tra quyền xem
        if (!call.getCaller().getId().equals(userId) && !call.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Refresh presigned URLs nếu hết hạn
        if (call.getHasRecording() && (call.getRecordingUrlExpiry() == null || call.getRecordingUrlExpiry().isBefore(LocalDateTime.now()))) {
            // Refresh combined recording URL
            if (call.getRecordingS3Key() != null) {
                String newUrl = s3Service.generatePresignedUrl(call.getRecordingS3Key(), 7 * 24 * 3600);
                call.setRecordingUrl(newUrl);
            }
            // Refresh caller recording URL
            if (call.getRecordingCallerS3Key() != null) {
                String newUrl = s3Service.generatePresignedUrl(call.getRecordingCallerS3Key(), 7 * 24 * 3600);
                call.setRecordingCallerUrl(newUrl);
            }
            // Refresh receiver recording URL
            if (call.getRecordingReceiverS3Key() != null) {
                String newUrl = s3Service.generatePresignedUrl(call.getRecordingReceiverS3Key(), 7 * 24 * 3600);
                call.setRecordingReceiverUrl(newUrl);
            }
            call.setRecordingUrlExpiry(LocalDateTime.now().plusDays(7));
            webCallLogRepository.save(call);
        }
        
        return convertToDTO(call, userId);
    }
    
    /**
     * Thống kê cuộc gọi
     */
    public Map<String, Object> getCallStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Tổng cuộc gọi
        stats.put("totalCalls", webCallLogRepository.countByUserId(userId));
        
        // Cuộc gọi nhỡ
        stats.put("missedCalls", webCallLogRepository.countMissedCalls(userId));
        
        // Tổng thời gian gọi (phút)
        Long totalSeconds = webCallLogRepository.sumDurationByUserId(userId);
        stats.put("totalDurationMinutes", totalSeconds != null ? totalSeconds / 60 : 0);
        
        // Thống kê theo trạng thái
        List<Object[]> statusStats = webCallLogRepository.countByUserIdGroupByStatus(userId);
        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] row : statusStats) {
            byStatus.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byStatus", byStatus);
        
        return stats;
    }
    
    /**
     * Đánh giá cuộc gọi
     */
    @Transactional
    public WebCallLog rateCall(Long callId, Long userId, int rating, String notes) {
        WebCallLog call = webCallLogRepository.findById(callId)
            .orElseThrow(() -> new RuntimeException("Call not found"));
        
        // Chỉ người tham gia mới được đánh giá
        if (!call.getCaller().getId().equals(userId) && !call.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        call.setRating(Math.min(5, Math.max(1, rating)));
        if (notes != null) {
            call.setNotes(notes);
        }
        
        return webCallLogRepository.save(call);
    }
    
    /**
     * Convert entity sang DTO
     */
    private WebCallDTO convertToDTO(WebCallLog call, Long currentUserId) {
        boolean isOutgoing = call.getCaller().getId().equals(currentUserId);
        User otherUser = isOutgoing ? call.getReceiver() : call.getCaller();
        
        return WebCallDTO.builder()
            .id(call.getId())
            .stringeeCallId(call.getStringeeCallId())
            .isOutgoing(isOutgoing)
            .otherUserId(otherUser.getId())
            .otherUserName(otherUser.getFullName())
            .otherUserAvatar(otherUser.getAvatarUrl())
            .otherUserRole(otherUser.getRole().name())
            .callStatus(call.getCallStatus().name())
            .startTime(call.getStartTime())
            .endTime(call.getEndTime())
            .duration(call.getDuration())
            .durationFormatted(formatDuration(call.getDuration()))
            .hasRecording(call.getHasRecording())
            .recordingFolder(call.getRecordingFolder())
            .recordingS3Key(call.getRecordingS3Key())
            .recordingCallerS3Key(call.getRecordingCallerS3Key())
            .recordingReceiverS3Key(call.getRecordingReceiverS3Key())
            .recordingUrl(call.getRecordingUrl())
            .recordingCallerUrl(call.getRecordingCallerUrl())
            .recordingReceiverUrl(call.getRecordingReceiverUrl())
            .rating(call.getRating())
            .notes(call.getNotes())
            .createdAt(call.getCreatedAt())
            .build();
    }
    
    /**
     * Format duration thành mm:ss
     */
    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0) return "0:00";
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", mins, secs);
    }
    
    /**
     * Inner class cho online user
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class OnlineUser {
        private Long userId;
        private String stringeeUserId;
        private String fullName;
        private String role;
        private String avatarUrl;
        private LocalDateTime connectedAt;
    }
}
