package com.data.project_it205.repository;

import com.data.project_it205.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Lấy đơn hàng theo user
    Page<Order> findByUserId(Integer userId, Pageable pageable);

    // Lấy đơn hàng theo user và status
    Page<Order> findByUserIdAndStatus(Integer userId, Order.OrderStatus status, Pageable pageable);

    // Lấy đơn hàng theo status
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    // Lấy đơn hàng theo ID và user ID (user chỉ xem đơn của mình)
    Optional<Order> findByIdAndUserId(Integer id, Integer userId);

    // Đếm số đơn hàng theo user
    long countByUserId(Integer userId);

    // Lấy đơn hàng theo ID với order items
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithOrderItems(@Param("orderId") Integer orderId);
}