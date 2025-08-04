package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLogResponseDTO {
    private Integer id;
    private Integer orderId;
    private String username;
    private String action;
    private String reason;
    private LocalDateTime createdAt;
} 