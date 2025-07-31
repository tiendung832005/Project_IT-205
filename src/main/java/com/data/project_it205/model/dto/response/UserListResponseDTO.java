package com.data.project_it205.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListResponseDTO {
    private List<UserResponseDTO.UserResponse> users;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
} 