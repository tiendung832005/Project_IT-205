package com.data.project_it205.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelOrderRequestDTO {

    @NotBlank(message = "Lý do hủy đơn hàng không được để trống")
    private String cancelReason;
} 