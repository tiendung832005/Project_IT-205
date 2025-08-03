package com.data.project_it205.repository;

import com.data.project_it205.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    // Lấy order items theo order ID
    List<OrderItem> findByOrderId(Integer orderId);
}