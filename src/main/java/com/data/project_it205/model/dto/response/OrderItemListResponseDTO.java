package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemListResponseDTO {
    private Integer orderId;
    private String orderStatus;
    private List<OrderItemResponseDTO> items;
    private Integer totalItems;
} 