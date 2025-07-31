package com.data.project_it205.service;

import com.data.project_it205.model.dto.request.UserLoginRequestDTO;
import com.data.project_it205.model.dto.request.UserRegisterRequestDTO;
import com.data.project_it205.model.dto.response.UserAuthResponseDTO;
import com.data.project_it205.model.entity.Role;
import com.data.project_it205.model.entity.User;
import com.data.project_it205.repository.RoleRepository;
import com.data.project_it205.repository.UserRepository;
import com.data.project_it205.security.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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

        return new UserAuthResponseDTO(null, "Đăng ký thành công!");
    }

    public UserAuthResponseDTO login(UserLoginRequestDTO dto) {
        User user = userRepository.findByUsernameOrEmail(dto.getUsernameOrEmail(), dto.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        if (!user.getStatus()) {
            throw new RuntimeException("Tài khoản đã bị khóa!");
        }
//        if (!user.getIsVerify()) {
//            throw new RuntimeException("Tài khoản chưa xác minh!");
//        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng!");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().getName().name());
        return new UserAuthResponseDTO(token, "Đăng nhập thành công!");
    }
}