package com.data.project_it205.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequestDTO {
    @NotBlank
    @Size(min = 6, max = 100)
    private String username;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String fullname;

    @NotBlank
    @Size(min = 6, max = 255)
    private String password;

    @Size(max = 255)
    private String avatar;

    @Size(max = 15)
    private String phone;

    @NotBlank
    @Size(max = 255)
    private String address;
}