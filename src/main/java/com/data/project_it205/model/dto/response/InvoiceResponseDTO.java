package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponseDTO {
    private Integer id;
    private Integer orderId;
    private String orderStatus;
    private String invoiceStatus;
    private BigDecimal totalAmount;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private String customerName;
    private String customerEmail;
} 