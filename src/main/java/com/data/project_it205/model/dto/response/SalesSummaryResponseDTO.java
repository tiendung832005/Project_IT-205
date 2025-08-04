package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesSummaryResponseDTO {
    private String period;
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer totalProducts;
    private List<SalesDataPoint> dataPoints;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SalesDataPoint {
        private String label;
        private BigDecimal value;
        private Integer orderCount;
    }
} 