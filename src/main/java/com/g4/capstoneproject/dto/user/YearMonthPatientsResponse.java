package com.g4.capstoneproject.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho response danh sách bệnh nhân theo năm và tháng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearMonthPatientsResponse {
    
    /**
     * Năm
     */
    private Integer year;
    
    /**
     * Tổng số bệnh nhân trong năm
     */
    private Long totalCount;
    
    /**
     * Danh sách các tháng có bệnh nhân
     * Sắp xếp từ tháng 12 đến tháng 1 (mới nhất trước)
     */
    private List<MonthPatientsGroup> months;
}
