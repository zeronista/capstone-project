package com.g4.capstoneproject.controller.api;

import com.g4.capstoneproject.dto.WebCallDTO;
import com.g4.capstoneproject.entity.User;
import com.g4.capstoneproject.entity.WebCallLog;
import com.g4.capstoneproject.entity.WebCallLog.WebCallStatus;
import com.g4.capstoneproject.repository.UserRepository;
import com.g4.capstoneproject.service.GeminiASRService;
import com.g4.capstoneproject.service.WhisperASRService;
import com.g4.capstoneproject.service.S3Service;
import com.g4.capstoneproject.service.StringeeService;
import com.g4.capstoneproject.service.WebCallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller cho Web Call (Web-to-Web Call giữa các user đã đăng nhập)
 */
@RestController
@RequestMapping("/api/web-call")
public class WebCallApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebCallApiController.class);
    
    @Autowired
    private WebCallService webCallService;
    
    @Autowired
    private StringeeService stringeeService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private GeminiASRService geminiASRService;
    
    @Autowired
    private WhisperASRService whisperASRService;
    
    /**
     * Lấy thông tin user hiện đang đăng nhập
     */
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
            }
            
            User user = getUserFromPrincipal(userDetails);
            String stringeeUserId = "user_" + user.getId();
            
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "userId", user.getId(),
                "stringeeUserId", stringeeUserId,
                "email", user.getEmail(),
                "fullName", user.getFullName() != null ? user.getFullName() : "User " + user.getId(),
                "role", user.getRole().name()
            ));
        } catch (Exception e) {
            logger.error("Error getting current user", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("authenticated", false, "error", e.getMessage()));
        }
    }
    
    /**
     * Lấy access token cho Stringee
     * User ID sẽ là "user_{userId}" để dễ tracking
     */
    @GetMapping("/token")
    public ResponseEntity<?> getAccessToken(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUserFromPrincipal(userDetails);
            String stringeeUserId = "user_" + user.getId();
            
            String token = stringeeService.getClientAccessToken(stringeeUserId);
            
            logger.info("Generated Stringee token for user {} ({})", user.getId(), user.getFullName());
            
            return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "stringeeUserId", stringeeUserId,
                "fullName", user.getFullName() != null ? user.getFullName() : "User " + user.getId(),
                "role", user.getRole().name(),
                "token", token
            ));
        } catch (Exception e) {
            logger.error("Error getting access token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Đăng ký user online khi kết nối Stringee thành công
     */
    @PostMapping("/online")
    public ResponseEntity<?> registerOnline(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        try {
            User user = getUserFromPrincipal(userDetails);
            String stringeeUserId = request.get("stringeeUserId");
            
            webCallService.registerOnline(user.getId(), stringeeUserId);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error registering online", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Đăng ký user offline
     */
    @PostMapping("/offline")
    public ResponseEntity<?> registerOffline(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUserFromPrincipal(userDetails);
            webCallService.registerOffline(user.getId());
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error registering offline", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách user đang online
     */
    @GetMapping("/online-users")
    public ResponseEntity<?> getOnlineUsers(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUserFromPrincipal(userDetails);
            List<WebCallService.OnlineUser> onlineUsers = webCallService.getOnlineUsers(user.getId());
            
            return ResponseEntity.ok(Map.of(
                "users", onlineUsers,
                "count", onlineUsers.size()
            ));
        } catch (Exception e) {
            logger.error("Error getting online users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Khởi tạo cuộc gọi
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateCall(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        try {
            User caller = getUserFromPrincipal(userDetails);
            Long receiverId = Long.valueOf(request.get("receiverId").toString());
            String stringeeCallId = (String) request.get("stringeeCallId");
            
            WebCallLog call = webCallService.initiateCall(caller.getId(), receiverId, stringeeCallId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "callLogId", call.getId(),
                "stringeeCallId", call.getStringeeCallId()
            ));
        } catch (Exception e) {
            logger.error("Error initiating call", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Cập nhật trạng thái cuộc gọi
     */
    @PostMapping("/{callId}/status")
    public ResponseEntity<?> updateCallStatus(
            @PathVariable Long callId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            WebCallStatus callStatus = WebCallStatus.valueOf(status);
            
            WebCallLog call = webCallService.updateCallStatus(callId, callStatus);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "callId", call.getId(),
                "status", call.getCallStatus().name()
            ));
        } catch (Exception e) {
            logger.error("Error updating call status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Upload file ghi âm và liên kết với cuộc gọi
     * Hỗ trợ 3 loại recording: caller, receiver, combined
     * 
     * @param callId ID cuộc gọi
     * @param file File ghi âm
     * @param recordingType Loại recording: "caller", "receiver", "combined" (mặc định: combined)
     */
    @PostMapping("/{callId}/recording")
    public ResponseEntity<?> uploadRecording(
            @PathVariable Long callId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "combined") String recordingType,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUserFromPrincipal(userDetails);
            
            logger.info("Uploading {} recording for call {}, file: {} ({}KB)", 
                recordingType, callId, file.getOriginalFilename(), file.getSize() / 1024);
            
            // Upload file lên S3 với folder structure mới: voice/calls/{callId}/{type}_{timestamp}.webm
            String s3Key = s3Service.uploadRecordingFile(file, String.valueOf(callId), "user_" + user.getId(), recordingType);
            String presignedUrl = s3Service.generatePresignedUrl(s3Key, 7 * 24 * 3600);
            
            // Lưu thông tin vào database
            WebCallLog call = webCallService.saveRecording(callId, recordingType, s3Key, presignedUrl);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "callId", call.getId(),
                "recordingType", recordingType,
                "s3Key", s3Key,
                "recordingUrl", presignedUrl,
                "recordingFolder", call.getRecordingFolder()
            ));
        } catch (Exception e) {
            logger.error("Error uploading recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lưu thông tin recording vào database (sau khi đã upload lên S3)
     * API endpoint dạng form params cho frontend dễ gọi
     */
    @PostMapping("/recording")
    public ResponseEntity<?> saveRecordingInfo(
            @RequestParam("callId") Long callId,
            @RequestParam("s3Key") String s3Key,
            @RequestParam(value = "presignedUrl", required = false) String presignedUrl,
            @RequestParam(value = "recordingType", defaultValue = "combined") String recordingType) {
        try {
            logger.info("Saving recording info for call {}: type={}, s3Key={}", callId, recordingType, s3Key);
            
            // Generate presigned URL nếu không được cung cấp
            if (presignedUrl == null || presignedUrl.isEmpty()) {
                presignedUrl = s3Service.generatePresignedUrl(s3Key, 7 * 24 * 3600);
            }
            
            // Lưu thông tin vào database
            WebCallLog call = webCallService.saveRecording(callId, recordingType, s3Key, presignedUrl);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "callId", call.getId(),
                "recordingType", recordingType,
                "s3Key", s3Key,
                "recordingUrl", presignedUrl,
                "recordingFolder", call.getRecordingFolder()
            ));
        } catch (Exception e) {
            logger.error("Error saving recording info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    /**
     * Lấy lịch sử cuộc gọi
     */
    @GetMapping("/history")
    public ResponseEntity<?> getCallHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User user = getUserFromPrincipal(userDetails);
            List<WebCallDTO> calls = webCallService.getCallHistory(user.getId(), page, size);
            
            return ResponseEntity.ok(Map.of(
                "calls", calls,
                "page", page,
                "size", size
            ));
        } catch (Exception e) {
            logger.error("Error getting call history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy cuộc gọi có ghi âm
     */
    @GetMapping("/recordings")
    public ResponseEntity<?> getCallsWithRecording(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUserFromPrincipal(userDetails);
            List<WebCallDTO> calls = webCallService.getCallsWithRecording(user.getId());
            
            return ResponseEntity.ok(calls);
        } catch (Exception e) {
            logger.error("Error getting calls with recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy cuộc gọi nhỡ
     */
    @GetMapping("/missed")
    public ResponseEntity<?> getMissedCalls(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUserFromPrincipal(userDetails);
            List<WebCallDTO> calls = webCallService.getMissedCalls(user.getId());
            
            return ResponseEntity.ok(calls);
        } catch (Exception e) {
            logger.error("Error getting missed calls", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy chi tiết cuộc gọi
     */
    @GetMapping("/{callId}")
    public ResponseEntity<?> getCallDetail(
            @PathVariable Long callId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUserFromPrincipal(userDetails);
            WebCallDTO call = webCallService.getCallDetail(callId, user.getId());
            
            return ResponseEntity.ok(call);
        } catch (Exception e) {
            logger.error("Error getting call detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy thống kê cuộc gọi
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUserFromPrincipal(userDetails);
            Map<String, Object> stats = webCallService.getCallStatistics(user.getId());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Đánh giá cuộc gọi
     */
    @PostMapping("/{callId}/rate")
    public ResponseEntity<?> rateCall(
            @PathVariable Long callId,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUserFromPrincipal(userDetails);
            int rating = Integer.parseInt(request.get("rating").toString());
            String notes = (String) request.get("notes");
            
            webCallService.rateCall(callId, user.getId(), rating, notes);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Error rating call", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Transcribe cuộc gọi - chuyển đổi audio thành text
     * 
     * Luồng xử lý:
     * 1. Tìm file recording trên S3 theo filename
     * 2. Kiểm tra transcript đã tồn tại chưa
     * 3. Nếu có -> trả về nội dung transcript
     * 4. Nếu chưa -> download audio từ S3, gọi OpenAI API, lưu transcript lên S3
     * 
     * @param filename Tên file recording (vd: call_user_3_to_user_1_xxx.webm)
     */
    @PostMapping("/transcribe")
    public ResponseEntity<?> transcribeRecording(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Lấy s3Key hoặc recordingKey/filename từ request
            String s3Key = request.get("s3Key");
            String recordingKey = request.get("recordingKey");
            String filename = request.get("filename");
            
            // Xác định actualRecordingKey
            String actualRecordingKey;
            
            if (s3Key != null && !s3Key.isEmpty()) {
                // Web call - dùng s3Key trực tiếp
                actualRecordingKey = s3Key;
            } else {
                // AI call - extract filename và tìm trên S3
                if (filename == null && recordingKey != null) {
                    // recordingKey có thể là "recordings/filename" hoặc full path
                    filename = recordingKey;
                    if (filename.contains("/")) {
                        filename = filename.substring(filename.lastIndexOf("/") + 1);
                    }
                }
                
                if (filename == null || filename.isEmpty()) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "s3Key, filename hoặc recordingKey là bắt buộc"));
                }
                
                logger.info("Transcribe request for filename: {}", filename);
                
                // Tìm file recording trên S3
                actualRecordingKey = s3Service.findRecordingKeyByFilename(filename);
                if (actualRecordingKey == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy file recording: " + filename));
                }
            }
            
            logger.info("Using S3 key for transcription: {}", actualRecordingKey);
            
            // Tạo transcript key từ recording key
            String transcriptKey = "transcripts/" + 
                actualRecordingKey.substring(actualRecordingKey.lastIndexOf("/") + 1)
                    .replaceAll("\\.(webm|mp3|wav|ogg|m4a)$", ".txt");
            
            // Kiểm tra transcript đã tồn tại chưa
            if (s3Service.doesFileExist(transcriptKey)) {
                logger.info("Transcript đã tồn tại: {}", transcriptKey);
                String transcript = s3Service.downloadTextContent(transcriptKey);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transcript", transcript,
                    "transcriptKey", transcriptKey,
                    "cached", true
                ));
            }
            
            // Download audio từ S3
            logger.info("Downloading audio from S3: {}", actualRecordingKey);
            byte[] audioBytes = s3Service.downloadFileBytes(actualRecordingKey);
            
            // Xác định filename để gửi đến ASR service
            String audioFilename = actualRecordingKey.substring(actualRecordingKey.lastIndexOf("/") + 1);
            
            // Gọi self-hosted Whisper ASR Service để transcribe
            logger.info("Calling self-hosted Whisper ASR to transcribe {} bytes ({})", audioBytes.length, audioFilename);
            String transcript = whisperASRService.transcribeBytes(audioBytes, audioFilename, "vi");
            
            // Lưu transcript lên S3
            logger.info("Saving transcript to S3: {}", transcriptKey);
            s3Service.uploadTextContent(transcript, transcriptKey);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "transcript", transcript,
                "transcriptKey", transcriptKey,
                "cached", false
            ));
            
        } catch (Exception e) {
            logger.error("Error transcribing recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi transcribe: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy transcript của một recording (nếu đã có)
     */
    @GetMapping("/transcript")
    public ResponseEntity<?> getTranscript(@RequestParam String recordingKey) {
        try {
            String transcriptKey = s3Service.getTranscriptKeyFromRecordingKey(recordingKey);
            
            if (!s3Service.doesFileExist(transcriptKey)) {
                return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "transcriptKey", transcriptKey
                ));
            }
            
            String transcript = s3Service.downloadTextContent(transcriptKey);
            return ResponseEntity.ok(Map.of(
                "exists", true,
                "transcript", transcript,
                "transcriptKey", transcriptKey
            ));
            
        } catch (Exception e) {
            logger.error("Error getting transcript", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Cập nhật transcript đã có
     */
    @PutMapping("/transcript")
    public ResponseEntity<?> updateTranscript(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String filename = request.get("filename");
            String transcript = request.get("transcript");
            
            if (filename == null || filename.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "filename là bắt buộc"));
            }
            if (transcript == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "transcript là bắt buộc"));
            }
            
            String transcriptKey = "transcripts/" + filename.replaceAll("\\.(webm|mp3|wav|ogg|m4a)$", ".txt");
            
            logger.info("Updating transcript: {}", transcriptKey);
            s3Service.uploadTextContent(transcript, transcriptKey);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "transcriptKey", transcriptKey,
                "message", "Cập nhật transcript thành công"
            ));
            
        } catch (Exception e) {
            logger.error("Error updating transcript", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi cập nhật transcript: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa transcript
     */
    @DeleteMapping("/transcript")
    public ResponseEntity<?> deleteTranscript(
            @RequestParam String filename,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (filename == null || filename.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "filename là bắt buộc"));
            }
            
            String transcriptKey = "transcripts/" + filename.replaceAll("\\.(webm|mp3|wav|ogg|m4a)$", ".txt");
            
            if (!s3Service.doesFileExist(transcriptKey)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Transcript không tồn tại"));
            }
            
            logger.info("Deleting transcript: {}", transcriptKey);
            s3Service.deleteFile(transcriptKey);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa transcript thành công"
            ));
            
        } catch (Exception e) {
            logger.error("Error deleting transcript", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi xóa transcript: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa recording (file ghi âm)
     */
    @DeleteMapping("/recording")
    public ResponseEntity<?> deleteRecording(
            @RequestParam String s3Key,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (s3Key == null || s3Key.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "s3Key là bắt buộc"));
            }
            
            if (!s3Service.doesFileExist(s3Key)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Recording không tồn tại"));
            }
            
            logger.info("Deleting recording: {}", s3Key);
            s3Service.deleteFile(s3Key);
            
            // Xóa transcript liên quan nếu có
            String transcriptKey = s3Key.replaceAll("\\.(webm|mp3|wav|ogg|m4a)$", ".txt")
                                        .replaceAll("^recordings/", "transcripts/");
            if (s3Service.doesFileExist(transcriptKey)) {
                logger.info("Deleting related transcript: {}", transcriptKey);
                s3Service.deleteFile(transcriptKey);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa recording thành công"
            ));
            
        } catch (Exception e) {
            logger.error("Error deleting recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi xóa recording: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách web call recordings của một bệnh nhân
     * Dành cho Receptionist xem lịch sử cuộc gọi
     */
    @GetMapping("/patient/{patientId}/recordings")
    public ResponseEntity<?> getPatientWebCallRecordings(@PathVariable Long patientId) {
        try {
            List<WebCallDTO> calls = webCallService.getCallsByPatientId(patientId);
            logger.info("Found {} web calls for patient {}", calls.size(), patientId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "calls", calls,
                "total", calls.size()
            ));
        } catch (Exception e) {
            logger.error("Error getting web calls for patient {}: {}", patientId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
    
    /**
     * Helper: Lấy User từ UserDetails
     */
    private User getUserFromPrincipal(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
