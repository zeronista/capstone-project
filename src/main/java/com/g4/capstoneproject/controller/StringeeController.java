package com.g4.capstoneproject.controller;

import com.g4.capstoneproject.service.StringeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller xử lý Stringee Call API
 * Endpoints:
 * - POST /api/stringee/call/outbound: Thực hiện cuộc gọi ra
 * - GET/POST /api/stringee/answer: Webhook xử lý khi khách hàng bắt máy
 * - POST /api/stringee/event: Webhook nhận events từ Stringee
 */
@RestController
@RequestMapping("/api/stringee")
public class StringeeController {

    private static final Logger logger = LoggerFactory.getLogger(StringeeController.class);

    @Autowired
    private StringeeService stringeeService;

    /**
     * API để thực hiện cuộc gọi ra cho khách hàng
     * 
     * Request body:
     * {
     *   "fromNumber": "842873008xxx",
     *   "toNumber": "84987654321",
     *   "brandName": "Phòng Khám ABC" (optional)
     * }
     */
    @PostMapping("/call/outbound")
    public ResponseEntity<?> makeOutboundCall(@RequestBody Map<String, String> request) {
        try {
            String fromNumber = request.get("fromNumber");
            String toNumber = request.get("toNumber");
            String brandName = request.getOrDefault("brandName", "Phòng Khám");

            if (fromNumber == null || toNumber == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "fromNumber và toNumber là bắt buộc"));
            }

            logger.info("Initiating outbound call from {} to {}", fromNumber, toNumber);
            
            String response = stringeeService.makeOutboundCall(fromNumber, toNumber, brandName);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cuộc gọi đã được khởi tạo",
                "data", response
            ));
            
        } catch (Exception e) {
            logger.error("Error in makeOutboundCall", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }

    /**
     * Webhook xử lý khi khách hàng bắt máy
     * Stringee sẽ gọi endpoint này và mong đợi nhận về SCCO (Stringee Call Control Object)
     * 
     * SCCO là một JSON array chứa các actions mà Stringee sẽ thực thi
     * 
     * Hỗ trợ cả GET và POST
     */
    @RequestMapping(value = "/answer", method = {RequestMethod.GET, RequestMethod.POST})
    public List<Map<String, Object>> handleAnswerWebhook(
            @RequestParam(required = false) Map<String, String> params,
            @RequestBody(required = false) Map<String, Object> body) {
        
        logger.info("Answer webhook called");
        logger.debug("Query params: {}", params);
        logger.debug("Request body: {}", body);

        // Tạo SCCO (Stringee Call Control Object)
        List<Map<String, Object>> scco = new ArrayList<>();

        // Action 1: Phát câu chào
        Map<String, Object> talkAction = new HashMap<>();
        talkAction.put("action", "talk");
        talkAction.put("text", "Xin chào! Tôi là trợ lý ảo từ phòng khám. Bạn cần hỗ trợ gì?");
        talkAction.put("voice", "southern_female_1"); // Giọng nữ miền nam
        scco.add(talkAction);

        // Action 2: Ghi âm câu trả lời của khách hàng (optional)
        // Map<String, Object> recordAction = new HashMap<>();
        // recordAction.put("action", "record");
        // recordAction.put("maxTime", 60); // Ghi âm tối đa 60 giây
        // recordAction.put("recordingStatusCallback", webhookDomain + "/api/stringee/recording");
        // scco.add(recordAction);

        // Action 3: Kết nối sang SIP Server để AI xử lý (cho AI real-time)
        // Map<String, Object> connectAction = new HashMap<>();
        // connectAction.put("action", "connect");
        // Map<String, String> sipTo = new HashMap<>();
        // sipTo.put("type", "sip");
        // sipTo.put("number", "ai-bot@your-sip-server.com"); // SIP URI của AI Server
        // connectAction.put("to", sipTo);
        // scco.add(connectAction);

        logger.info("Returning SCCO with {} actions", scco.size());
        return scco;
    }

    /**
     * Webhook nhận các events từ Stringee
     * Ví dụ: call.started, call.answered, call.ended, recording.available, etc.
     */
    @PostMapping("/event")
    public ResponseEntity<?> handleEventWebhook(@RequestBody Map<String, Object> event) {
        try {
            logger.info("Received Stringee event: {}", event);
            
            String eventType = (String) event.get("event");
            
            switch (eventType) {
                case "call.started":
                    handleCallStarted(event);
                    break;
                case "call.answered":
                    handleCallAnswered(event);
                    break;
                case "call.ended":
                    handleCallEnded(event);
                    break;
                case "recording.available":
                    handleRecordingAvailable(event);
                    break;
                default:
                    logger.debug("Unhandled event type: {}", eventType);
            }
            
            return ResponseEntity.ok(Map.of("received", true));
            
        } catch (Exception e) {
            logger.error("Error handling event webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xử lý khi cuộc gọi bắt đầu
     */
    private void handleCallStarted(Map<String, Object> event) {
        String callId = (String) event.get("callId");
        logger.info("Call started: {}", callId);
        // TODO: Lưu thông tin cuộc gọi vào database
    }

    /**
     * Xử lý khi khách hàng bắt máy
     */
    private void handleCallAnswered(Map<String, Object> event) {
        String callId = (String) event.get("callId");
        logger.info("Call answered: {}", callId);
        // TODO: Cập nhật trạng thái cuộc gọi
    }

    /**
     * Xử lý khi cuộc gọi kết thúc
     */
    private void handleCallEnded(Map<String, Object> event) {
        String callId = (String) event.get("callId");
        Integer duration = (Integer) event.get("duration");
        logger.info("Call ended: {} (duration: {}s)", callId, duration);
        // TODO: Lưu thông tin cuộc gọi đã kết thúc
    }

    /**
     * Xử lý khi file ghi âm sẵn sàng
     */
    private void handleRecordingAvailable(Map<String, Object> event) {
        String callId = (String) event.get("callId");
        String recordingUrl = (String) event.get("recordingUrl");
        logger.info("Recording available for call {}: {}", callId, recordingUrl);
        // TODO: Tải file ghi âm về để AI phân tích
        // TODO: Hoặc lưu URL vào database
    }

    /**
     * API để lấy Access Token cho Client (Web-to-Web Call)
     * Token này dùng để client kết nối tới Stringee Server
     * 
     * Query param:
     * - userId: ID của người dùng (ví dụ: user1, user2)
     * 
     * Response:
     * {
     *   "userId": "user1",
     *   "token": "eyJhbGc..."
     * }
     */
    @GetMapping("/access-token")
    public ResponseEntity<?> getClientAccessToken(@RequestParam String userId) {
        try {
            logger.info("Requesting access token for userId: {}", userId);
            
            String token = stringeeService.getClientAccessToken(userId);
            
            logger.info("Successfully created token for userId: {} (length: {})", userId, token.length());
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "token", token
            ));
        } catch (Exception e) {
            logger.error("Error getting client access token for userId: " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API để lấy thông tin cuộc gọi
     */
    @GetMapping("/call/info/{callId}")
    public ResponseEntity<?> getCallInfo(@PathVariable String callId) {
        try {
            String info = stringeeService.getCallInfo(callId);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("Error getting call info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API test để tạo SCCO cho AI real-time processing
     * Ví dụ kết nối sang SIP Server
     */
    @GetMapping("/answer/ai-mode")
    public List<Map<String, Object>> getAiModeScco() {
        List<Map<String, Object>> scco = new ArrayList<>();

        // Bước 1: Chào khách hàng
        Map<String, Object> greeting = new HashMap<>();
        greeting.put("action", "talk");
        greeting.put("text", "Xin chào, bạn đang kết nối với trợ lý AI của phòng khám.");
        greeting.put("voice", "southern_female_1");
        scco.add(greeting);

        // Bước 2: Kết nối sang SIP Server để AI xử lý real-time
        Map<String, Object> connect = new HashMap<>();
        connect.put("action", "connect");
        
        Map<String, String> sipDestination = new HashMap<>();
        sipDestination.put("type", "sip");
        sipDestination.put("number", "ai-callbot@your-sip-server.com"); // Thay bằng SIP URI thực
        
        connect.put("to", sipDestination);
        scco.add(connect);

        return scco;
    }
}
