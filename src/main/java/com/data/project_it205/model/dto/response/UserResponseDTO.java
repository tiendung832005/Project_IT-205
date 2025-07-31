package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class UserResponseDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse {
        private Integer id;
        private String username;
        private String email;
        private String fullname;
        private Boolean status;
        private String avatar;
        private String phone;
        private String address;
        private RoleResponseDTO.RoleResponse role;
        private Boolean isVerify;
        private LocalDate createdAt;
        private LocalDate updatedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDetailResponse {
        private Integer id;
        private String username;
        private String email;
        private String fullname;
        private Boolean status;
        private String avatar;
        private String phone;
        private String address;
        private RoleResponseDTO.RoleResponse role;
        private Boolean isVerify;
        private LocalDate createdAt;
        private LocalDate updatedAt;
        private Boolean isDeleted;
        private LocalDate deletedAt;
    }
} 