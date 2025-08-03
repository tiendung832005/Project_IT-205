package com.data.project_it205.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchRequestDTO {
    private String name;
    private Integer categoryId;
    private Integer page = 0;
    private Integer size = 10;
}