package com.g4.capstoneproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO cho SCCO Action (Stringee Call Control Object)
 * Các loại action phổ biến:
 * - talk: Phát văn bản thành giọng nói
 * - play: Phát file âm thanh
 * - record: Ghi âm
 * - connect: Kết nối sang số khác hoặc SIP
 * - gather: Thu thập DTMF (phím bấm)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StringeeAction {
    
    /**
     * Loại action
     */
    private String action;
    
    /**
     * Text to Speech (cho action "talk")
     */
    private String text;
    
    /**
     * Giọng đọc (cho action "talk")
     * Ví dụ: "southern_female_1", "northern_male_1"
     */
    private String voice;
    
    /**
     * URL file audio (cho action "play")
     */
    private String url;
    
    /**
     * Thời gian ghi âm tối đa (giây) - cho action "record"
     */
    private Integer maxTime;
    
    /**
     * Callback URL khi ghi âm xong
     */
    private String recordingStatusCallback;
    
    /**
     * Số/SIP để kết nối (cho action "connect")
     */
    private Map<String, String> to;
    
    /**
     * Số lượng số cần thu thập (cho action "gather")
     */
    private Integer numDigits;
    
    /**
     * Timeout khi chờ nhập số (giây)
     */
    private Integer timeout;
    
    /**
     * URL callback khi đã thu thập đủ số
     */
    private String finishOnKey;
    
    /**
     * Actions tiếp theo sau khi gather
     */
    private List<StringeeAction> next;
}
