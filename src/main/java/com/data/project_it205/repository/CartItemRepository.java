package com.data.project_it205.repository;

import com.data.project_it205.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    // Lấy tất cả cart items của user
    List<CartItem> findByUserId(Integer userId);

    // Lấy cart item theo user và product
    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);

    // Kiểm tra user có cart item với product này không
    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

    // Đếm số lượng cart items của user
    long countByUserId(Integer userId);

    // Xóa tất cả cart items của user
    void deleteByUserId(Integer userId);

    // Lấy cart item theo ID và user ID (để đảm bảo user chỉ xóa item của mình)
    Optional<CartItem> findByIdAndUserId(Integer id, Integer userId);
}