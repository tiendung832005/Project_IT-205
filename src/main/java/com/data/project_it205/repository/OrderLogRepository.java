package com.data.project_it205.repository;

import com.data.project_it205.model.entity.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLogRepository extends JpaRepository<OrderLog, Integer> {

    // Lấy logs theo order ID
    List<OrderLog> findByOrderIdOrderByCreatedAtDesc(Integer orderId);

    // Lấy logs theo order ID và action
    List<OrderLog> findByOrderIdAndActionOrderByCreatedAtDesc(Integer orderId, OrderLog.LogAction action);
} 