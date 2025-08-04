package com.data.project_it205.repository;

import com.data.project_it205.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Lấy payment theo invoice ID
    List<Payment> findByInvoiceId(Integer invoiceId);

    // Lấy payment theo invoice ID và user ID (để kiểm tra quyền)
    @Query("SELECT p FROM Payment p JOIN p.invoice i JOIN i.order o WHERE i.id = :invoiceId AND o.user.id = :userId")
    Optional<Payment> findByInvoiceIdAndUserId(@Param("invoiceId") Integer invoiceId, @Param("userId") Integer userId);

    // Lấy payment theo ID và user ID (để kiểm tra quyền)
    @Query("SELECT p FROM Payment p JOIN p.invoice i JOIN i.order o WHERE p.id = :paymentId AND o.user.id = :userId")
    Optional<Payment> findByIdAndUserId(@Param("paymentId") Integer paymentId, @Param("userId") Integer userId);

    // Lấy tất cả payment của user
    @Query("SELECT p FROM Payment p JOIN p.invoice i JOIN i.order o WHERE o.user.id = :userId")
    List<Payment> findByUserId(@Param("userId") Integer userId);
} 