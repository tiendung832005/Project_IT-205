package com.data.project_it205.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequestDTO {

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    private String internalNotes;
} 