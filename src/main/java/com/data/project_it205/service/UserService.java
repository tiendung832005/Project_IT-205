package com.data.project_it205.service;

import com.data.project_it205.model.dto.request.*;
import com.data.project_it205.model.dto.response.*;
import com.data.project_it205.model.entity.User;
import com.data.project_it205.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 1. Lấy thông tin profile của user hiện tại
    public UserResponseDTO.UserResponse getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        return convertToUserResponse(user);
    }

    // 2. Cập nhật thông tin cá nhân của user hiện tại
    @Transactional
    public UserResponseDTO.UserResponse updateCurrentUserProfile(String username, UserProfileRequestDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        // Kiểm tra email có bị trùng không (nếu thay đổi email)
        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã tồn tại!");
            }
        }

        user.setFullname(dto.getFullname());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setUpdatedAt(LocalDate.now());

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    // 3. Thay đổi mật khẩu người dùng
    @Transactional
    public void changePassword(String username, ChangePasswordRequestDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng!");
        }

        // Kiểm tra mật khẩu mới và xác nhận
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp!");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDate.now());
        userRepository.save(user);
    }

//    // 4. Xác thực email
//    @Transactional
//    public void verifyEmail(String username, EmailVerificationRequestDTO dto) {
//        User user = userRepository.findByUsernameWithRole(username)
//                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
//
//        if (user.getIsVerify()) {
//            throw new RuntimeException("Tài khoản đã được xác thực!");
//        }
//
//        // mã xác thực là "123456"
//        if (!"123456".equals(dto.getVerificationCode())) {
//            throw new RuntimeException("Mã xác thực không đúng!");
//        }
//
//        user.setIsVerify(true);
//        user.setUpdatedAt(LocalDate.now());
//        userRepository.save(user);
//    }

    // 5. Lấy danh sách tất cả users (phân trang)
    public UserListResponseDTO getAllUsers(UserSearchRequestDTO searchRequest) {
        // Tạo Pageable
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(searchRequest.getSortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                searchRequest.getSortBy());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        // specification cho tìm kiếm
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponseDTO.UserResponse> userResponses = userPage.getContent()
                .stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return new UserListResponseDTO(
                userResponses,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getSize());
    }

    // 6. Lấy thông tin chi tiết của 1 user cụ thể
    public UserResponseDTO.UserDetailResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        return convertToUserDetailResponse(user);
    }

    // 7. Cập nhật thông tin của user (bởi admin/sales)
    @Transactional
    public UserResponseDTO.UserResponse updateUser(Integer userId, UserUpdateRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        // Kiểm tra email có bị trùng không (nếu thay đổi email)
        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã tồn tại!");
            }
        }

        user.setFullname(dto.getFullname());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());

        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        if (dto.getIsVerify() != null) {
            user.setIsVerify(dto.getIsVerify());
        }

        user.setUpdatedAt(LocalDate.now());
        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    // 8. Cập nhật trạng thái user
    @Transactional
    public void updateUserStatus(Integer userId, Boolean status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        user.setStatus(status);
        user.setUpdatedAt(LocalDate.now());
        userRepository.save(user);
    }

    // 9. Xóa mềm tài khoản người dùng
    @Transactional
    public void softDeleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        user.setIsDeleted(true);
        user.setDeletedAt(LocalDate.now());
        userRepository.save(user);
    }

    // Helper methods
    private UserResponseDTO.UserResponse convertToUserResponse(User user) {
        UserResponseDTO.UserResponse response = new UserResponseDTO.UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullname(user.getFullname());
        response.setStatus(user.getStatus());
        response.setAvatar(user.getAvatar());
        response.setPhone(user.getPhone());
        response.setAddress(user.getAddress());
        response.setIsVerify(user.getIsVerify());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        // Convert role
        if (user.getRole() != null) {
            RoleResponseDTO.RoleResponse roleResponse = new RoleResponseDTO.RoleResponse();
            roleResponse.setId(user.getRole().getId());
            roleResponse.setName(user.getRole().getName().name());
            response.setRole(roleResponse);
        }

        return response;
    }

    private UserResponseDTO.UserDetailResponse convertToUserDetailResponse(User user) {
        UserResponseDTO.UserDetailResponse response = new UserResponseDTO.UserDetailResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullname(user.getFullname());
        response.setStatus(user.getStatus());
        response.setAvatar(user.getAvatar());
        response.setPhone(user.getPhone());
        response.setAddress(user.getAddress());
        response.setIsVerify(user.getIsVerify());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setIsDeleted(user.getIsDeleted());
        response.setDeletedAt(user.getDeletedAt());

        // Convert role
        if (user.getRole() != null) {
            RoleResponseDTO.RoleResponse roleResponse = new RoleResponseDTO.RoleResponse();
            roleResponse.setId(user.getRole().getId());
            roleResponse.setName(user.getRole().getName().name());
            response.setRole(roleResponse);
        }

        return response;
    }
}