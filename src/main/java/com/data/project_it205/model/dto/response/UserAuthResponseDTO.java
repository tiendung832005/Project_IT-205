package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAuthResponseDTO {
    private String token;
    private String message;
}