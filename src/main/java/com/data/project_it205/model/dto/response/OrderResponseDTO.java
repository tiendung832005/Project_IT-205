package com.data.project_it205.model.dto.response;

import com.data.project_it205.model.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
    private Integer id;
    private Integer userId;
    private String username;
    private Order.OrderStatus status;
    private String shippingAddress;
    private String internalNotes;
    private BigDecimal totalPrice;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private List<OrderItemResponseDTO> orderItems;
}