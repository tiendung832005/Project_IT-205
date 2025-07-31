package com.data.project_it205.controller;

import com.data.project_it205.model.dto.request.*;
import com.data.project_it205.model.dto.response.*;
import com.data.project_it205.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 1. Lấy thông tin profile của user hiện tại
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO.UserResponse>> getCurrentUserProfile() {
        try {
            String username = getCurrentUsername();
            UserResponseDTO.UserResponse profile = userService.getCurrentUserProfile(username);
            return ResponseEntity.ok(ApiResponseDTO.success("Lấy thông tin profile thành công", profile));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(ex.getMessage()));
        }
    }

    // 2. Cập nhật thông tin cá nhân của user hiện tại
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO.UserResponse>> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileRequestDTO dto) {
        try {
            String username = getCurrentUsername();
            UserResponseDTO.UserResponse updatedProfile = userService.updateCurrentUserProfile(username, dto);
            return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật profile thành công", updatedProfile));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(ex.getMessage()));
        }
    }

    // 3. Thay đổi mật khẩu người dùng
    @PutMapping("/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDTO dto) {
        try {
            String username = getCurrentUsername();
            userService.changePassword(username, dto);
            return ResponseEntity.ok(ApiResponseDTO.success("Đổi mật khẩu thành công"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(ex.getMessage()));
        }
    }

    // 5. Xác thực email
    @PostMapping("/verify-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'CUSTOMER')")
    public ResponseEntity<ApiResponseDTO<Void>> verifyEmail(@Valid @RequestBody EmailVerificationRequestDTO dto) {
        try {
            String username = getCurrentUsername();
            userService.verifyEmail(username, dto);
            return ResponseEntity.ok(ApiResponseDTO.success("Xác thực email thành công"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(ex.getMessage()));
        }
    }

    // 5. Lấy danh sách tất cả users (phân trang) - Chỉ ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<UserListResponseDTO>> getAllUsers(UserSearchRequestDTO searchRequest) {
        try {
            UserListResponseDTO users = userService.getAllUsers(searchRequest);
            return ResponseEntity.ok(ApiResponseDTO.success("Lấy danh sách users thành công", users));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(ex.getMessage()));
        }
    }

    // 6. Lấy thông tin chi tiết của 1 user cụ thể - ADMIN và SALES
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO.UserDetailResponse>> getUserById(
            @PathVariable Integer userId) {
        try {
            UserResponseDTO.UserDetailResponse user = userService.getUserById(userId);
            return ResponseEntity.ok(ApiResponseDTO.success("Lấy thông tin user thành công", user));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(ex.getMessage()));
        }
    }

    // 7. Cập nhật thông tin của user (bởi admin/sales)
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO.UserResponse>> updateUser(
            @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateRequestDTO dto) {
        try {
            UserResponseDTO.UserResponse updatedUser = userService.updateUser(userId, dto);
            return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật user thành công", updatedUser));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(ex.getMessage()));
        }
    }

    // 8. Cập nhật trạng thái user - Chỉ ADMIN
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> updateUserStatus(
            @PathVariable Integer userId,
            @RequestParam Boolean status) {
        try {
            userService.updateUserStatus(userId, status);
            return ResponseEntity.ok(ApiResponseDTO.success("Cập nhật trạng thái user thành công"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(ex.getMessage()));
        }
    }

    // 9. Xóa mềm tài khoản người dùng - Chỉ ADMIN
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> softDeleteUser(@PathVariable Integer userId) {
        try {
            userService.softDeleteUser(userId);
            return ResponseEntity.ok(ApiResponseDTO.success("Xóa user thành công"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(ex.getMessage()));
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}