package com.data.project_it205.repository;

import com.data.project_it205.model.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    // Lấy hóa đơn theo user ID
    @Query("SELECT i FROM Invoice i JOIN i.order o WHERE o.user.id = :userId")
    Page<Invoice> findByUserId(@Param("userId") Integer userId, Pageable pageable);

    // Lấy hóa đơn theo user ID và status
    @Query("SELECT i FROM Invoice i JOIN i.order o WHERE o.user.id = :userId AND i.status = :status")
    Page<Invoice> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") Invoice.InvoiceStatus status, Pageable pageable);

    // Lấy hóa đơn theo status
    Page<Invoice> findByStatus(Invoice.InvoiceStatus status, Pageable pageable);

    // Lấy hóa đơn theo order ID
    Optional<Invoice> findByOrderId(Integer orderId);

    // Lấy hóa đơn theo user ID và order ID
    @Query("SELECT i FROM Invoice i JOIN i.order o WHERE o.user.id = :userId AND i.order.id = :orderId")
    Optional<Invoice> findByUserIdAndOrderId(@Param("userId") Integer userId, @Param("orderId") Integer orderId);

    // Lấy hóa đơn theo khoảng thời gian
    @Query("SELECT i FROM Invoice i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    Page<Invoice> findByCreatedAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    // Lấy hóa đơn theo user ID và khoảng thời gian
    @Query("SELECT i FROM Invoice i JOIN i.order o WHERE o.user.id = :userId AND i.createdAt BETWEEN :startDate AND :endDate")
    Page<Invoice> findByUserIdAndCreatedAtBetween(@Param("userId") Integer userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    // Lấy hóa đơn theo status và khoảng thời gian
    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.createdAt BETWEEN :startDate AND :endDate")
    Page<Invoice> findByStatusAndCreatedAtBetween(@Param("status") Invoice.InvoiceStatus status, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    // Lấy hóa đơn theo user ID, status và khoảng thời gian
    @Query("SELECT i FROM Invoice i JOIN i.order o WHERE o.user.id = :userId AND i.status = :status AND i.createdAt BETWEEN :startDate AND :endDate")
    Page<Invoice> findByUserIdAndStatusAndCreatedAtBetween(@Param("userId") Integer userId, @Param("status") Invoice.InvoiceStatus status, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
} 