package com.g4.capstoneproject.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho nhóm bệnh nhân theo tháng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthPatientsGroup {
    
    /**
     * Tháng (1-12)
     */
    private Integer month;
    
    /**
     * Tên tháng hiển thị (VD: "Tháng 1", "Tháng 12")
     */
    private String monthName;
    
    /**
     * Số lượng bệnh nhân trong tháng
     */
    private Long count;
    
    /**
     * Danh sách bệnh nhân trong tháng
     */
    private List<UserResponse> patients;
}
