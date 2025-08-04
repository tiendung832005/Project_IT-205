package com.data.project_it205.service;

import com.data.project_it205.model.dto.response.*;
import com.data.project_it205.repository.ReportRepository;
import com.data.project_it205.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    // Kiểm tra quyền ADMIN
    private void checkAdminPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String userRole = userRepository.findRoleNameByUsername(username);
        
        if (!"ADMIN".equals(userRole)) {
            throw new RuntimeException("Chỉ ADMIN mới có quyền truy cập báo cáo này");
        }
    }

    // Báo cáo tổng quan doanh số theo tuần, tháng, quý, năm
    public SalesSummaryResponseDTO getSalesSummary(String range) {
        checkAdminPermission();
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        String dateFormat;
        
        switch (range.toLowerCase()) {
            case "week":
                startDate = endDate.minusWeeks(1);
                dateFormat = "%Y-%u"; // Năm-Tuần
                break;
            case "month":
                startDate = endDate.minusMonths(1);
                dateFormat = "%Y-%m"; // Năm-Tháng
                break;
            case "quarter":
                startDate = endDate.minusMonths(3);
                dateFormat = "%Y-Q%q"; // Năm-Quý
                break;
            case "year":
                startDate = endDate.minusYears(1);
                dateFormat = "%Y"; // Năm
                break;
            default:
                throw new RuntimeException("Range không hợp lệ. Chỉ chấp nhận: week, month, quarter, year");
        }

        List<Object[]> results = reportRepository.getSalesSummaryByPeriod(startDate, endDate, dateFormat);
        
        List<SalesSummaryResponseDTO.SalesDataPoint> dataPoints = results.stream()
                .map(result -> {
                    String period = (String) result[0];
                    BigDecimal revenue = (BigDecimal) result[1];
                    Integer orderCount = ((Number) result[2]).intValue();
                    
                    return new SalesSummaryResponseDTO.SalesDataPoint(period, revenue, orderCount);
                })
                .collect(Collectors.toList());

        BigDecimal totalRevenue = dataPoints.stream()
                .map(SalesSummaryResponseDTO.SalesDataPoint::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalOrders = dataPoints.stream()
                .mapToInt(SalesSummaryResponseDTO.SalesDataPoint::getOrderCount)
                .sum();

        return new SalesSummaryResponseDTO(range, totalRevenue, totalOrders, dataPoints.size(), dataPoints);
    }

    // Báo cáo top sản phẩm bán chạy
    public TopProductsResponseDTO getTopProducts() {
        checkAdminPermission();
        
        List<Object[]> results = reportRepository.getTopProductsByQuantity();
        
        List<TopProductsResponseDTO.TopProduct> products = results.stream()
                .limit(10) // Chỉ lấy top 10
                .map(result -> {
                    Integer productId = (Integer) result[0];
                    String productName = (String) result[1];
                    String categoryName = (String) result[2];
                    Integer totalQuantity = ((Number) result[3]).intValue();
                    BigDecimal totalRevenue = (BigDecimal) result[4];
                    Integer currentStock = (Integer) result[5];
                    
                    // Tính averagePrice từ totalRevenue và totalQuantity
                    BigDecimal averagePrice = totalQuantity > 0 ? 
                            totalRevenue.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP) : 
                            BigDecimal.ZERO;
                    
                    return new TopProductsResponseDTO.TopProduct(
                            productId, productName, categoryName, totalQuantity, 
                            totalRevenue, averagePrice, currentStock);
                })
                .collect(Collectors.toList());

        return new TopProductsResponseDTO(products);
    }

    // Báo cáo doanh thu theo khoảng thời gian
    public RevenueReportResponseDTO getRevenueReport(LocalDate fromDate, LocalDate toDate) {
        checkAdminPermission();
        
        if (fromDate == null || toDate == null) {
            throw new RuntimeException("Từ ngày và đến ngày không được để trống");
        }
        
        if (fromDate.isAfter(toDate)) {
            throw new RuntimeException("Từ ngày không được sau đến ngày");
        }

        BigDecimal totalRevenue = reportRepository.getTotalRevenueByDateRange(fromDate, toDate);
        Integer totalOrders = reportRepository.getTotalOrdersByDateRange(fromDate, toDate);
        
        BigDecimal averageOrderValue = totalOrders > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;

        List<Object[]> dailyResults = reportRepository.getDailyRevenue(fromDate, toDate);
        
        List<RevenueReportResponseDTO.DailyRevenue> dailyRevenues = dailyResults.stream()
                .map(result -> {
                    LocalDate date = (LocalDate) result[0];
                    BigDecimal revenue = (BigDecimal) result[1];
                    Integer orderCount = ((Number) result[2]).intValue();
                    Integer productCount = ((Number) result[3]).intValue();
                    
                    return new RevenueReportResponseDTO.DailyRevenue(date, revenue, orderCount, productCount);
                })
                .collect(Collectors.toList());

        return new RevenueReportResponseDTO(fromDate, toDate, totalRevenue, totalOrders, averageOrderValue, dailyRevenues);
    }

    // Báo cáo sản phẩm tồn kho thấp
    public LowStockResponseDTO getLowStockReport(Integer threshold) {
        checkAdminPermission();
        
        if (threshold == null) {
            threshold = 10; // Mặc định ngưỡng cảnh báo là 10
        }

        List<Object[]> results = reportRepository.getLowStockProducts(threshold);

        Integer finalThreshold = threshold;
        List<LowStockResponseDTO.LowStockProduct> products = results.stream()
                .map(result -> {
                    Integer productId = (Integer) result[0];
                    String productName = (String) result[1];
                    String categoryName = (String) result[2];
                    Integer currentStock = (Integer) result[3];
                    BigDecimal price = (BigDecimal) result[4];
                    
                    String status;
                    if (currentStock == 0) {
                        status = "CRITICAL";
                    } else if (currentStock <= finalThreshold * 0.3) {
                        status = "LOW";
                    } else {
                        status = "WARNING";
                    }
                    
                    return new LowStockResponseDTO.LowStockProduct(
                            productId, productName, categoryName, currentStock, price, status);
                })
                .collect(Collectors.toList());

        return new LowStockResponseDTO(threshold, products.size(), products);
    }
} 