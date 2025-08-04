package com.data.project_it205.model.dto.request;

import com.data.project_it205.model.entity.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentRequestDTO {

    @NotNull(message = "Invoice ID không được để trống")
    private Integer invoiceId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private Payment.PaymentMethod method;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private java.math.BigDecimal amount;

    private String transactionId;
} 