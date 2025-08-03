package com.data.project_it205.controller;

import com.data.project_it205.model.dto.request.CreateOrderRequestDTO;
import com.data.project_it205.model.dto.request.UpdateOrderStatusRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.OrderListResponseDTO;
import com.data.project_it205.model.dto.response.OrderResponseDTO;
import com.data.project_it205.model.entity.Order;
import com.data.project_it205.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //1. GET /api/orders - Lấy danh sách đơn hàng
    @GetMapping
    public ResponseEntity<ApiResponseDTO> getOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Order.OrderStatus status) {
        try {
            OrderListResponseDTO orders = orderService.getOrders(page, size, status);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy danh sách đơn hàng thành công", orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //2. GET /api/orders/{id} - Lấy chi tiết đơn hàng
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> getOrderById(@PathVariable Integer id) {
        try {
            OrderResponseDTO order = orderService.getOrderById(id);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy chi tiết đơn hàng thành công", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //3. POST /api/orders - Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<ApiResponseDTO> createOrder(@Valid @RequestBody CreateOrderRequestDTO requestDTO) {
        try {
            OrderResponseDTO order = orderService.createOrder(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDTO(true, "Tạo đơn hàng thành công", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //4. PUT /api/orders/{id}/status - Cập nhật trạng thái đơn hàng
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponseDTO> updateOrderStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateOrderStatusRequestDTO requestDTO) {
        try {
            OrderResponseDTO order = orderService.updateOrderStatus(id, requestDTO);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Cập nhật trạng thái đơn hàng thành công", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }
}