package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private Integer id;
    private Integer invoiceId;
    private String paymentMethod;
    private BigDecimal amount;
    private String paymentStatus;
    private String transactionId;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private String customerName;
    private String customerEmail;
} 