package com.data.project_it205.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateInvoiceRequestDTO {

    @NotNull(message = "Order ID không được để trống")
    private Integer orderId;
} 