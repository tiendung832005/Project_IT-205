package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LowStockResponseDTO {
    private Integer warningThreshold;
    private Integer totalLowStockProducts;
    private List<LowStockProduct> products;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LowStockProduct {
        private Integer productId;
        private String productName;
        private String categoryName;
        private Integer currentStock;
        private BigDecimal price;
        private String status; // "CRITICAL", "LOW", "WARNING"
    }
} 