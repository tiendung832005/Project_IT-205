package com.data.project_it205.model.dto.request;

import lombok.Data;

@Data
public class UserSearchRequestDTO {
    private String username;
    private String email;
    private String fullname;
    private String phone;
    private Integer roleId;
    private Boolean status;
    private Boolean isVerify;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "desc";
} 