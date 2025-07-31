package com.data.project_it205.repository;

import com.data.project_it205.model.entity.User;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(@Size(max = 15) String phone);

    // Thêm method để fetch user với role
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username")
    Optional<User> findByUsernameWithRole(@Param("username") String username);

    // Method để fetch role name trực tiếp
    @Query("SELECT r.name FROM User u JOIN u.role r WHERE u.username = :username")
    String findRoleNameByUsername(@Param("username") String username);
}