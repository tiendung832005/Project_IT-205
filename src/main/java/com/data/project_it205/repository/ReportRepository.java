package com.data.project_it205.repository;

import com.data.project_it205.model.entity.Order;
import com.data.project_it205.model.entity.OrderItem;
import com.data.project_it205.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Order, Integer> {

    // Báo cáo doanh số theo khoảng thời gian
    @Query("SELECT SUM(oi.quantity * oi.price) FROM OrderItem oi " +
           "JOIN oi.order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "AND o.status != 'CANCELLED'")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "AND o.status != 'CANCELLED'")
    Integer getTotalOrdersByDateRange(@Param("startDate") LocalDate startDate, 
                                    @Param("endDate") LocalDate endDate);

    // Top sản phẩm bán chạy
    @Query("SELECT oi.product.id, oi.product.name, oi.product.category.name, " +
           "SUM(oi.quantity) as totalQuantity, " +
           "SUM(oi.quantity * oi.price) as totalRevenue, " +
           "oi.product.stock as currentStock " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status != 'CANCELLED' " +
           "GROUP BY oi.product.id, oi.product.name, oi.product.category.name, oi.product.stock " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> getTopProductsByQuantity();

    // Doanh thu theo ngày
    @Query("SELECT o.createdAt as date, " +
           "SUM(oi.quantity * oi.price) as revenue, " +
           "COUNT(DISTINCT o.id) as orderCount, " +
           "COUNT(oi.id) as productCount " +
           "FROM Order o " +
           "JOIN o.orderItems oi " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "AND o.status != 'CANCELLED' " +
           "GROUP BY o.createdAt " +
           "ORDER BY o.createdAt")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDate startDate, 
                                  @Param("endDate") LocalDate endDate);

    // Sản phẩm tồn kho thấp
    @Query("SELECT p.id, p.name, p.category.name, p.stock, p.price " +
           "FROM Product p " +
           "WHERE p.stock <= :threshold AND p.isDeleted = false " +
           "ORDER BY p.stock ASC")
    List<Object[]> getLowStockProducts(@Param("threshold") Integer threshold);

    // Thống kê doanh số theo tuần/tháng/quý/năm
    @Query(value = "SELECT DATE_FORMAT(o.created_at, :dateFormat) as period, " +
           "SUM(oi.quantity * oi.price) as revenue, " +
           "COUNT(DISTINCT o.id) as orderCount " +
           "FROM orders o " +
           "JOIN order_items oi ON o.id = oi.order_id " +
           "WHERE o.created_at BETWEEN :startDate AND :endDate " +
           "AND o.status != 'CANCELLED' " +
           "GROUP BY period " +
           "ORDER BY period", nativeQuery = true)
    List<Object[]> getSalesSummaryByPeriod(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("dateFormat") String dateFormat);
} 