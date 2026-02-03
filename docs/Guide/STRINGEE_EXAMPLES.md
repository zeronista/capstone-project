# Ví dụ Sử dụng Stringee Integration

## 1. Ví dụ Gọi ra Đơn giản

```java
@RestController
@RequestMapping("/api/demo")
public class DemoController {
    
    @Autowired
    private StringeeService stringeeService;
    
    // Ví dụ: Gọi nhắc lịch khám cho bệnh nhân
    @PostMapping("/appointment/reminder")
    public ResponseEntity<?> sendAppointmentReminder(
            @RequestParam String patientPhone,
            @RequestParam String appointmentTime) {
        
        String fromNumber = "842873008xxx"; // Số của phòng khám
        String message = "Xin chào, bạn có lịch khám vào " + appointmentTime + 
                        ". Vui lòng xác nhận bằng cách bấm phím 1.";
        
        // Tạo custom actions
        List<Map<String, Object>> actions = new ArrayList<>();
        
        // Action 1: Thông báo lịch hẹn
        Map<String, Object> talk = new HashMap<>();
        talk.put("action", "talk");
        talk.put("text", message);
        talk.put("voice", "southern_female_1");
        actions.add(talk);
        
        // Action 2: Thu thập xác nhận (DTMF)
        Map<String, Object> gather = new HashMap<>();
        gather.put("action", "gather");
        gather.put("numDigits", 1);
        gather.put("timeout", 10);
        actions.add(gather);
        
        String response = stringeeService.makeOutboundCallWithCustomActions(
            fromNumber, 
            patientPhone, 
            actions
        );
        
        return ResponseEntity.ok(response);
    }
}
```

## 2. Ví dụ AI Callbot Real-time

```java
@Service
public class AiCallbotService {
    
    @Autowired
    private StringeeService stringeeService;
    
    /**
     * Khởi tạo cuộc gọi AI Callbot
     * Khi khách hàng bắt máy, cuộc gọi sẽ được chuyển sang SIP Server
     * để AI xử lý real-time
     */
    public String initiateAiCallbot(String customerPhone, String purpose) {
        String fromNumber = "842873008xxx";
        
        // Actions sẽ được thực thi khi khách hàng bắt máy
        List<Map<String, Object>> actions = new ArrayList<>();
        
        // Bước 1: Chào khách hàng
        Map<String, Object> greeting = new HashMap<>();
        greeting.put("action", "talk");
        greeting.put("text", "Xin chào, tôi là trợ lý AI của phòng khám. " +
                           "Tôi có thể giúp gì cho bạn?");
        greeting.put("voice", "southern_female_1");
        actions.add(greeting);
        
        // Bước 2: Kết nối sang AI SIP Server
        Map<String, Object> connect = new HashMap<>();
        connect.put("action", "connect");
        
        Map<String, String> sipTo = new HashMap<>();
        sipTo.put("type", "sip");
        sipTo.put("number", "ai-callbot@your-ai-server.com");
        sipTo.put("custom_data", purpose); // Truyền metadata
        
        connect.put("to", sipTo);
        actions.add(connect);
        
        return stringeeService.makeOutboundCallWithCustomActions(
            fromNumber,
            customerPhone,
            actions
        );
    }
}
```

## 3. Ví dụ Survey Call (Khảo sát)

```java
@Service
public class SurveyService {
    
    @Autowired
    private StringeeService stringeeService;
    
    public String conductSurvey(String customerPhone) {
        String fromNumber = "842873008xxx";
        
        List<Map<String, Object>> actions = new ArrayList<>();
        
        // Câu hỏi 1
        Map<String, Object> question1 = new HashMap<>();
        question1.put("action", "talk");
        question1.put("text", "Xin chào! Bạn đánh giá dịch vụ của chúng tôi " +
                             "từ 1 đến 5 sao. Vui lòng bấm số.");
        question1.put("voice", "southern_female_1");
        actions.add(question1);
        
        // Thu thập đánh giá
        Map<String, Object> gather1 = new HashMap<>();
        gather1.put("action", "gather");
        gather1.put("numDigits", 1);
        gather1.put("timeout", 10);
        gather1.put("action_url", "https://your-domain.com/api/stringee/survey/process");
        actions.add(gather1);
        
        // Lời cảm ơn
        Map<String, Object> thanks = new HashMap<>();
        thanks.put("action", "talk");
        thanks.put("text", "Cảm ơn bạn đã tham gia khảo sát.");
        thanks.put("voice", "southern_female_1");
        actions.add(thanks);
        
        return stringeeService.makeOutboundCallWithCustomActions(
            fromNumber,
            customerPhone,
            actions
        );
    }
}
```

## 4. Ví dụ IVR Menu (Tổng đài tự động)

```java
@GetMapping("/answer/ivr")
public List<Map<String, Object>> handleIvrMenu() {
    List<Map<String, Object>> scco = new ArrayList<>();
    
    // Menu chính
    Map<String, Object> mainMenu = new HashMap<>();
    mainMenu.put("action", "talk");
    mainMenu.put("text", "Xin chào! Bấm 1 để đặt lịch khám. " +
                        "Bấm 2 để hỏi về giá dịch vụ. " +
                        "Bấm 3 để nói chuyện với tư vấn viên.");
    mainMenu.put("voice", "southern_female_1");
    scco.add(mainMenu);
    
    // Thu thập lựa chọn
    Map<String, Object> gather = new HashMap<>();
    gather.put("action", "gather");
    gather.put("numDigits", 1);
    gather.put("timeout", 10);
    gather.put("action_url", "https://your-domain.com/api/stringee/ivr/process");
    scco.add(gather);
    
    return scco;
}

@PostMapping("/ivr/process")
public List<Map<String, Object>> processIvrChoice(@RequestBody Map<String, Object> request) {
    String digits = (String) request.get("Digits");
    
    List<Map<String, Object>> scco = new ArrayList<>();
    Map<String, Object> response = new HashMap<>();
    response.put("action", "talk");
    response.put("voice", "southern_female_1");
    
    switch (digits) {
        case "1":
            response.put("text", "Bạn đã chọn đặt lịch khám. " +
                               "Vui lòng chờ kết nối với nhân viên.");
            scco.add(response);
            // Kết nối với nhân viên
            Map<String, Object> connect = new HashMap<>();
            connect.put("action", "connect");
            connect.put("to", Map.of("type", "internal", "number", "1001"));
            scco.add(connect);
            break;
            
        case "2":
            response.put("text", "Bảng giá dịch vụ đã được gửi qua SMS. " +
                               "Cảm ơn bạn đã gọi.");
            scco.add(response);
            // TODO: Gửi SMS bảng giá
            break;
            
        case "3":
            response.put("text", "Đang kết nối với tư vấn viên...");
            scco.add(response);
            // Kết nối tư vấn viên
            break;
            
        default:
            response.put("text", "Lựa chọn không hợp lệ. Vui lòng thử lại.");
            scco.add(response);
    }
    
    return scco;
}
```

## 5. Ví dụ Ghi âm và Phân tích

```java
@Service
public class RecordingService {
    
    @Autowired
    private StringeeService stringeeService;
    
    /**
     * Gọi khách hàng và ghi âm phản hồi
     */
    public String recordCustomerFeedback(String customerPhone) {
        String fromNumber = "842873008xxx";
        
        List<Map<String, Object>> actions = new ArrayList<>();
        
        // Giới thiệu
        Map<String, Object> intro = new HashMap<>();
        intro.put("action", "talk");
        intro.put("text", "Xin chào! Chúng tôi muốn lắng nghe ý kiến của bạn. " +
                         "Vui lòng nói sau tiếng beep.");
        intro.put("voice", "southern_female_1");
        actions.add(intro);
        
        // Ghi âm
        Map<String, Object> record = new HashMap<>();
        record.put("action", "record");
        record.put("maxTime", 120); // Ghi tối đa 2 phút
        record.put("recordingStatusCallback", 
                  "https://your-domain.com/api/stringee/recording/analyze");
        actions.add(record);
        
        // Cảm ơn
        Map<String, Object> thanks = new HashMap<>();
        thanks.put("action", "talk");
        thanks.put("text", "Cảm ơn bạn đã chia sẻ. Chúc bạn một ngày tốt lành.");
        thanks.put("voice", "southern_female_1");
        actions.add(thanks);
        
        return stringeeService.makeOutboundCallWithCustomActions(
            fromNumber,
            customerPhone,
            actions
        );
    }
    
    /**
     * Xử lý khi file ghi âm sẵn sàng
     */
    @PostMapping("/recording/analyze")
    public ResponseEntity<?> analyzeRecording(@RequestBody Map<String, Object> data) {
        String recordingUrl = (String) data.get("recordingUrl");
        String callId = (String) data.get("callId");
        
        // TODO: Download file ghi âm
        // TODO: STT - chuyển audio thành text
        // TODO: LLM - phân tích sentiment, extract insights
        // TODO: Lưu kết quả vào database
        
        logger.info("Analyzing recording for call {}: {}", callId, recordingUrl);
        
        return ResponseEntity.ok(Map.of("status", "processing"));
    }
}
```

## 6. Ví dụ Emergency Call (Cuộc gọi khẩn cấp)

```java
@Service
public class EmergencyCallService {
    
    @Autowired
    private StringeeService stringeeService;
    
    /**
     * Gọi khẩn cấp cho bệnh nhân hoặc người thân
     */
    public void sendEmergencyAlert(String patientPhone, String emergencyInfo) {
        String fromNumber = "842873008xxx";
        
        List<Map<String, Object>> actions = new ArrayList<>();
        
        // Thông báo khẩn cấp (lặp lại 2 lần)
        for (int i = 0; i < 2; i++) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("action", "talk");
            alert.put("text", "CẢNH BÁO KHẨN CẤP! " + emergencyInfo + 
                             " Vui lòng liên hệ phòng khám ngay.");
            alert.put("voice", "southern_female_1");
            actions.add(alert);
            
            // Pause 2 giây giữa 2 lần lặp
            if (i == 0) {
                Map<String, Object> pause = new HashMap<>();
                pause.put("action", "pause");
                pause.put("duration", 2);
                actions.add(pause);
            }
        }
        
        // Tùy chọn kết nối với nhân viên
        Map<String, Object> option = new HashMap<>();
        option.put("action", "talk");
        option.put("text", "Bấm 0 để nói chuyện với nhân viên ngay.");
        option.put("voice", "southern_female_1");
        actions.add(option);
        
        Map<String, Object> gather = new HashMap<>();
        gather.put("action", "gather");
        gather.put("numDigits", 1);
        gather.put("timeout", 10);
        actions.add(gather);
        
        stringeeService.makeOutboundCallWithCustomActions(
            fromNumber,
            patientPhone,
            actions
        );
    }
}
```

## 7. Ví dụ Multi-language Support

```java
@Service
public class MultiLanguageCallService {
    
    public String callWithLanguageOption(String customerPhone) {
        String fromNumber = "842873008xxx";
        
        List<Map<String, Object>> actions = new ArrayList<>();
        
        // Menu chọn ngôn ngữ
        Map<String, Object> languageMenu = new HashMap<>();
        languageMenu.put("action", "talk");
        languageMenu.put("text", "Xin chào. Hello. Press 1 for Vietnamese. Press 2 for English.");
        languageMenu.put("voice", "southern_female_1");
        actions.add(languageMenu);
        
        Map<String, Object> gather = new HashMap<>();
        gather.put("action", "gather");
        gather.put("numDigits", 1);
        gather.put("timeout", 10);
        gather.put("action_url", "https://your-domain.com/api/stringee/language/select");
        actions.add(gather);
        
        return stringeeService.makeOutboundCallWithCustomActions(
            fromNumber,
            customerPhone,
            actions
        );
    }
    
    @PostMapping("/language/select")
    public List<Map<String, Object>> handleLanguageSelection(
            @RequestBody Map<String, Object> request) {
        
        String language = "1".equals(request.get("Digits")) ? "vi" : "en";
        
        List<Map<String, Object>> scco = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();
        response.put("action", "talk");
        
        if ("vi".equals(language)) {
            response.put("text", "Bạn đã chọn tiếng Việt. Xin vui lòng chờ...");
            response.put("voice", "southern_female_1");
        } else {
            response.put("text", "You have selected English. Please wait...");
            response.put("voice", "en-US-male-1");
        }
        
        scco.add(response);
        return scco;
    }
}
```

## 8. Ví dụ Call Queue (Hàng đợi)

```java
@Service
public class CallQueueService {
    
    private Queue<String> callQueue = new ConcurrentLinkedQueue<>();
    
    public List<Map<String, Object>> handleQueuedCall() {
        List<Map<String, Object>> scco = new ArrayList<>();
        
        // Thông báo vị trí trong hàng đợi
        int position = callQueue.size() + 1;
        
        Map<String, Object> queueInfo = new HashMap<>();
        queueInfo.put("action", "talk");
        queueInfo.put("text", String.format(
            "Hiện tại có %d người đang chờ trước bạn. " +
            "Vui lòng giữ máy, chúng tôi sẽ phục vụ bạn trong giây lát.",
            position - 1
        ));
        queueInfo.put("voice", "southern_female_1");
        scco.add(queueInfo);
        
        // Phát nhạc chờ
        Map<String, Object> holdMusic = new HashMap<>();
        holdMusic.put("action", "play");
        holdMusic.put("url", "https://your-domain.com/assets/hold-music.mp3");
        holdMusic.put("loop", 3); // Lặp lại 3 lần
        scco.add(holdMusic);
        
        return scco;
    }
}
```

## 9. Test với cURL

```bash
# 1. Gọi nhắc lịch hẹn
curl -X POST http://localhost:8080/api/demo/appointment/reminder \
  -d "patientPhone=84987654321" \
  -d "appointmentTime=9:00 ngày 25/01/2026"

# 2. Khởi tạo AI Callbot
curl -X POST http://localhost:8080/api/stringee/call/outbound \
  -H "Content-Type: application/json" \
  -d '{
    "fromNumber": "842873008xxx",
    "toNumber": "84987654321",
    "brandName": "AI Callbot"
  }'

# 3. Lấy thông tin cuộc gọi
curl http://localhost:8080/api/stringee/call/info/call-xxxxx-xxxxx-xxxxx
```

## 10. Monitoring và Analytics

```java
@Service
public class CallAnalyticsService {
    
    @Autowired
    private CallRepository callRepository; // JPA Repository
    
    /**
     * Lưu thông tin cuộc gọi vào database
     */
    public void logCall(StringeeEvent event) {
        CallLog log = new CallLog();
        log.setCallId(event.getCallId());
        log.setFromNumber(event.getFrom());
        log.setToNumber(event.getTo());
        log.setDuration(event.getDuration());
        log.setStatus(event.getStatus());
        log.setRecordingUrl(event.getRecordingUrl());
        log.setStartTime(new Date(event.getStartTime()));
        log.setEndTime(new Date(event.getEndTime()));
        
        callRepository.save(log);
    }
    
    /**
     * Thống kê cuộc gọi
     */
    public Map<String, Object> getCallStatistics(LocalDate date) {
        List<CallLog> calls = callRepository.findByDate(date);
        
        return Map.of(
            "totalCalls", calls.size(),
            "answeredCalls", calls.stream()
                .filter(c -> "answered".equals(c.getStatus()))
                .count(),
            "averageDuration", calls.stream()
                .mapToInt(CallLog::getDuration)
                .average()
                .orElse(0),
            "successRate", calculateSuccessRate(calls)
        );
    }
}
```
