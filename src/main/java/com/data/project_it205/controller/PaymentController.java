package com.data.project_it205.controller;

import com.data.project_it205.model.dto.request.CreatePaymentRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.PaymentResponseDTO;
import com.data.project_it205.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    //1. POST /api/payments - Tạo thanh toán mới cho hóa đơn
    @PostMapping
    public ResponseEntity<ApiResponseDTO> createPayment(@Valid @RequestBody CreatePaymentRequestDTO requestDTO) {
        try {
            PaymentResponseDTO payment = paymentService.createPayment(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDTO(true, "Tạo thanh toán thành công", payment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //2. GET /api/payments/{id} - Lấy thông tin chi tiết của 1 payment
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> getPaymentById(@PathVariable Integer id) {
        try {
            PaymentResponseDTO payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy thông tin thanh toán thành công", payment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }
} 