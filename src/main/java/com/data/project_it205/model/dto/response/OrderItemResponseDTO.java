package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private String productDescription;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
}