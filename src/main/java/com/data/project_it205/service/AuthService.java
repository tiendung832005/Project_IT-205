package com.data.project_it205.service;

import com.data.project_it205.model.dto.request.EmailVerificationRequestDTO;
import com.data.project_it205.model.dto.request.UserLoginRequestDTO;
import com.data.project_it205.model.dto.request.UserRegisterRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.UserAuthResponseDTO;
import com.data.project_it205.model.entity.EmailVerification;
import com.data.project_it205.model.entity.Role;
import com.data.project_it205.model.entity.User;
import com.data.project_it205.repository.EmailVerificationRepository;
import com.data.project_it205.repository.RoleRepository;
import com.data.project_it205.repository.UserRepository;
import com.data.project_it205.security.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserAuthResponseDTO register(UserRegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Số điện thoại đã tồn tại!");
        }

        Role role = roleRepository.findByName(Role.RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER không tồn tại"));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullname(dto.getFullname());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setAvatar(dto.getAvatar());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(role);
        user.setStatus(true);
        user.setIsVerify(false);
        user.setIsDeleted(false);

        userRepository.save(user);

        // Gửi email xác thực
        sendVerificationEmail(user.getEmail(), user.getUsername());

        return new UserAuthResponseDTO(null, "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
    }

    public UserAuthResponseDTO login(UserLoginRequestDTO dto) {
        User user = userRepository.findByUsernameOrEmail(dto.getUsernameOrEmail(), dto.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        if (!user.getStatus()) {
            throw new RuntimeException("Tài khoản đã bị khóa!");
        }
        if (!user.getIsVerify()) {
            throw new RuntimeException("Tài khoản chưa xác minh! Vui lòng kiểm tra email để xác thực.");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng!");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().getName().name());
        return new UserAuthResponseDTO(token, "Đăng nhập thành công!");
    }

    /**
     * Gửi email xác thực
     */
    public ApiResponseDTO sendVerificationEmail(String email, String username) {
        try {
                    // Xóa verification code cũ nếu có
        emailVerificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());

            // Tạo verification code mới
            String verificationCode = emailService.generateVerificationCode();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(10); // 10 phút

            EmailVerification emailVerification = new EmailVerification();
            emailVerification.setEmail(email);
            emailVerification.setVerificationCode(verificationCode);
            emailVerification.setCreatedAt(now);
            emailVerification.setExpiresAt(expiresAt);
            emailVerification.setIsUsed(false);

            emailVerificationRepository.save(emailVerification);

            // Gửi email
            emailService.sendVerificationEmail(email, username, verificationCode);

            return new ApiResponseDTO(true, "Email xác thực đã được gửi thành công!", null);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email xác thực: " + e.getMessage());
        }
    }

    /**
     * Xác thực email
     */
    @Transactional
    public ApiResponseDTO verifyEmail(EmailVerificationRequestDTO dto) {
        try {
            // Tìm verification code hợp lệ
            EmailVerification verification = emailVerificationRepository
                    .findValidVerification(dto.getEmail(), dto.getVerificationCode(), LocalDateTime.now())
                    .orElseThrow(() -> new RuntimeException("Mã xác thực không hợp lệ hoặc đã hết hạn!"));

            // Đánh dấu đã sử dụng
            verification.setIsUsed(true);
            emailVerificationRepository.save(verification);

            // Cập nhật trạng thái user
            User user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email này!"));

            user.setIsVerify(true);
            userRepository.save(user);

            return new ApiResponseDTO(true, "Xác thực email thành công!", null);
        } catch (Exception e) {
            throw new RuntimeException("Xác thực email thất bại: " + e.getMessage());
        }
    }

    /**
     * Gửi lại email xác thực
     */
    public ApiResponseDTO resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email này!"));

        if (user.getIsVerify()) {
            throw new RuntimeException("Tài khoản đã được xác thực!");
        }

        return sendVerificationEmail(email, user.getUsername());
    }
}