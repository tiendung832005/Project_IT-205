package com.data.project_it205.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailVerificationRequestDTO {
    @NotBlank(message = "Mã xác thực không được để trống")
    private String verificationCode;
}