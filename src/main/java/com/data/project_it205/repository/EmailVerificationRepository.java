package com.data.project_it205.repository;

import com.data.project_it205.model.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {

    // Tìm verification code chưa sử dụng và chưa hết hạn
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.verificationCode = :code AND ev.isUsed = false AND ev.expiresAt > :now")
    Optional<EmailVerification> findValidVerification(@Param("email") String email,
                                                      @Param("code") String code,
                                                      @Param("now") LocalDateTime now);

    // Xóa các verification code đã hết hạn
    void deleteByExpiresAtBefore(LocalDateTime now);

    // Kiểm tra email có verification code chưa sử dụng không
    boolean existsByEmailAndIsUsedFalse(String email);
}