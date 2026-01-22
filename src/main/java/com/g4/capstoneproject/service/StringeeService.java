package com.g4.capstoneproject.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service xử lý tích hợp Stringee Call API
 * Chức năng chính:
 * - Tạo JWT Access Token để xác thực với Stringee
 * - Thực hiện cuộc gọi ra (Outbound Call) 
 * - Quản lý kết nối với Stringee API
 */
@Service
public class StringeeService {

    private static final Logger logger = LoggerFactory.getLogger(StringeeService.class);

    @Value("${stringee.key.sid}")
    private String keySid;

    @Value("${stringee.key.secret}")
    private String keySecret;

    @Value("${stringee.api.base-url}")
    private String apiBaseUrl;

    @Value("${stringee.webhook.domain}")
    private String webhookDomain;

    private final RestTemplate restTemplate;

    public StringeeService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Tạo JWT Access Token để xác thực với Stringee API
     * Token hết hạn sau 1 giờ (3600 giây)
     * 
     * @return JWT token string
     * @throws RuntimeException nếu không thể tạo token
     */
    public String getAccessToken() {
        try {
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            // Token hết hạn sau 1 giờ (3600000 milliseconds)
            long expMillis = nowMillis + 3600000; 
            Date exp = new Date(expMillis);

            Algorithm algorithm = Algorithm.HMAC256(keySecret);
            
            // Header theo chuẩn Stringee
            Map<String, Object> headerClaims = new HashMap<>();
            headerClaims.put("cty", "stringee-api;v=1");

            String token = JWT.create()
                    .withHeader(headerClaims)
                    .withKeyId(keySid)
                    .withIssuedAt(now)
                    .withExpiresAt(exp)
                    .withClaim("rest_api", true)
                    .sign(algorithm);

            logger.debug("Stringee Access Token created successfully");
            return token;
        } catch (Exception e) {
            logger.error("Error creating Stringee Access Token", e);
            throw new RuntimeException("Error creating Stringee Access Token", e);
        }
    }

    /**
     * Tạo Access Token cho Client (Web/Mobile App) kết nối tới Stringee
     * Token này cần chứa userId để định danh người dùng
     * 
     * @param userId ID của người dùng cần kết nối
     * @return JWT token string cho client
     * @throws RuntimeException nếu không thể tạo token
     */
    public String getClientAccessToken(String userId) {
        try {
            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            // Token hết hạn sau 1 giờ
            long expMillis = nowMillis + 3600000; 
            Date exp = new Date(expMillis);

            Algorithm algorithm = Algorithm.HMAC256(keySecret);
            
            Map<String, Object> headerClaims = new HashMap<>();
            headerClaims.put("cty", "stringee-api;v=1");

            // Tạo unique JWT ID
            String jti = keySid + "-" + nowMillis;

            String token = JWT.create()
                    .withHeader(headerClaims)
                    .withKeyId(keySid)
                    .withJWTId(jti) // QUAN TRỌNG: JWT ID là required cho Stringee
                    .withIssuer(keySid) // QUAN TRỌNG: Issuer phải là keySid
                    .withIssuedAt(now)
                    .withExpiresAt(exp)
                    .withClaim("userId", userId) // QUAN TRỌNG: Định danh user
                    // TUYỆT ĐỐI KHÔNG thêm .withClaim("rest_api", true) cho Client Token
                    .sign(algorithm);

            logger.info("Stringee Client Access Token created successfully for userId: {} (jti: {})", userId, jti);
            return token;
        } catch (Exception e) {
            logger.error("Error creating Client Access Token for userId: " + userId, e);
            throw new RuntimeException("Error creating Client Access Token", e);
        }
    }

    /**
     * Thực hiện cuộc gọi ra cho khách hàng (Outbound Call)
     * 
     * @param fromNumber Số Voice Brandname đã đăng ký (hoặc số test của Stringee)
     * @param toNumber Số điện thoại khách hàng cần gọi
     * @return Response JSON từ Stringee API
     */
    public String makeOutboundCall(String fromNumber, String toNumber) {
        return makeOutboundCall(fromNumber, toNumber, "Phòng Khám");
    }

    /**
     * Thực hiện cuộc gọi ra với tùy chỉnh tên hiển thị
     * 
     * @param fromNumber Số Voice Brandname
     * @param toNumber Số khách hàng
     * @param brandName Tên hiển thị trên điện thoại khách hàng
     * @return Response JSON từ Stringee API
     */
    public String makeOutboundCall(String fromNumber, String toNumber, String brandName) {
        try {
            String url = apiBaseUrl + "/call2/callout";
            String token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-STRINGEE-AUTH", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Cấu hình số gọi đi (from)
            Map<String, String> from = new HashMap<>();
            from.put("type", "external");
            from.put("number", fromNumber);
            from.put("alias", brandName); // Tên hiển thị Brandname

            // Cấu hình số nhận (to)
            Map<String, String> to = new HashMap<>();
            to.put("type", "external");
            to.put("number", toNumber);
            to.put("alias", "Khách Hàng");

            // Webhook URL - Stringee sẽ gọi URL này khi khách hàng bắt máy
            String answerUrl = webhookDomain + "/api/stringee/answer";

            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", from);
            requestBody.put("to", to);
            requestBody.put("answer_url", new String[]{answerUrl});
            
            // Cấu hình actions ban đầu (optional)
            // Actions này sẽ thực thi khi khách hàng bắt máy
            List<Map<String, Object>> actions = new ArrayList<>();
            Map<String, Object> talkAction = new HashMap<>();
            talkAction.put("action", "talk");
            talkAction.put("text", "Xin chào, đây là trợ lý ảo từ phòng khám. Vui lòng chờ trong giây lát.");
            talkAction.put("voice", "southern_female_1"); // Giọng nữ miền nam
            actions.add(talkAction);
            
            requestBody.put("actions", actions);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            logger.info("Making outbound call from {} to {}", fromNumber, toNumber);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            logger.info("Stringee API response: {}", response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Error making outbound call", e);
            throw new RuntimeException("Error making outbound call to " + toNumber, e);
        }
    }

    /**
     * Thực hiện cuộc gọi với custom SCCO actions
     * 
     * @param fromNumber Số gọi đi
     * @param toNumber Số nhận
     * @param customActions Danh sách actions tùy chỉnh
     * @return Response từ Stringee
     */
    public String makeOutboundCallWithCustomActions(
            String fromNumber, 
            String toNumber, 
            List<Map<String, Object>> customActions) {
        
        try {
            String url = apiBaseUrl + "/call2/callout";
            String token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-STRINGEE-AUTH", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> from = new HashMap<>();
            from.put("type", "external");
            from.put("number", fromNumber);
            from.put("alias", "Phòng Khám AI");

            Map<String, String> to = new HashMap<>();
            to.put("type", "external");
            to.put("number", toNumber);
            to.put("alias", "Bệnh Nhân");

            String answerUrl = webhookDomain + "/api/stringee/answer";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", from);
            requestBody.put("to", to);
            requestBody.put("answer_url", new String[]{answerUrl});
            requestBody.put("actions", customActions);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Error making outbound call with custom actions", e);
            throw new RuntimeException("Error making custom outbound call", e);
        }
    }

    /**
     * Lấy thông tin cuộc gọi từ Stringee
     * 
     * @param callId ID của cuộc gọi
     * @return Thông tin cuộc gọi dưới dạng JSON
     */
    public String getCallInfo(String callId) {
        try {
            String url = apiBaseUrl + "/call2/info/" + callId;
            String token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-STRINGEE-AUTH", token);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error getting call info for callId: {}", callId, e);
            throw new RuntimeException("Error getting call info", e);
        }
    }
}
