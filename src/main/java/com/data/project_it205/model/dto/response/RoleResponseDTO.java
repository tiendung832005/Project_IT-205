package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RoleResponseDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleResponse {
        private Integer id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleDetailResponse {
        private Integer id;
        private String name;
        private Integer userCount;
    }
} 