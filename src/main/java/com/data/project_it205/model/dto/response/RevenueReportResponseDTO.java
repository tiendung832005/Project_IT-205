package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueReportResponseDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private BigDecimal averageOrderValue;
    private List<DailyRevenue> dailyRevenues;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
        private Integer orderCount;
        private Integer productCount;
    }
} 