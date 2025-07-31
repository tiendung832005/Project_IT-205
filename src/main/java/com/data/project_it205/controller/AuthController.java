package com.data.project_it205.controller;

import com.data.project_it205.model.dto.request.UserLoginRequestDTO;
import com.data.project_it205.model.dto.request.UserRegisterRequestDTO;
import com.data.project_it205.model.dto.response.UserAuthResponseDTO;
import com.data.project_it205.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequestDTO dto) {
        try {
            UserAuthResponseDTO response = authService.register(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new UserAuthResponseDTO(null, ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDTO dto) {
        try {
            UserAuthResponseDTO response = authService.login(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(new UserAuthResponseDTO(null, ex.getMessage()));
        }
    }
}