package com.data.project_it205.controller;

import com.data.project_it205.model.dto.response.*;
import com.data.project_it205.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    //1. GET /api/reports/sales-summary?range=month - Báo cáo tổng quan doanh số
    @GetMapping("/sales-summary")
    public ResponseEntity<ApiResponseDTO> getSalesSummary(@RequestParam(defaultValue = "month") String range) {
        try {
            SalesSummaryResponseDTO salesSummary = reportService.getSalesSummary(range);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy báo cáo doanh số thành công", salesSummary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //2. GET /api/reports/top-products - Báo cáo top sản phẩm bán chạy
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponseDTO> getTopProducts() {
        try {
            TopProductsResponseDTO topProducts = reportService.getTopProducts();
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy báo cáo top sản phẩm thành công", topProducts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //3. GET /api/reports/revenue?from=2025-07-01&to=2025-07-30 - Báo cáo doanh thu theo khoảng thời gian
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponseDTO> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            RevenueReportResponseDTO revenueReport = reportService.getRevenueReport(from, to);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy báo cáo doanh thu thành công", revenueReport));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //4. GET /api/reports/low-stock - Báo cáo sản phẩm tồn kho thấp
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponseDTO> getLowStockReport(@RequestParam(defaultValue = "10") Integer threshold) {
        try {
            LowStockResponseDTO lowStockReport = reportService.getLowStockReport(threshold);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy báo cáo tồn kho thành công", lowStockReport));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }
} 