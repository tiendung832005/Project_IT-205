package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Integer productCount;
} 