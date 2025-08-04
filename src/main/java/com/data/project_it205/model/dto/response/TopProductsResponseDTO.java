package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductsResponseDTO {
    private List<TopProduct> products;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopProduct {
        private Integer productId;
        private String productName;
        private String categoryName;
        private Integer totalQuantity;
        private BigDecimal totalRevenue;
        private BigDecimal averagePrice;
        private Integer currentStock;
    }
} 