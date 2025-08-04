package com.data.project_it205.controller;

import com.data.project_it205.model.dto.request.CreateInvoiceRequestDTO;
import com.data.project_it205.model.dto.request.UpdateInvoiceStatusRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.InvoiceListResponseDTO;
import com.data.project_it205.model.dto.response.InvoiceResponseDTO;
import com.data.project_it205.model.entity.Invoice;
import com.data.project_it205.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    //1. GET /api/invoices - Lấy danh sách hóa đơn (có phân trang, filter theo trạng thái, thời gian)
    @GetMapping
    public ResponseEntity<ApiResponseDTO> getInvoices(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Invoice.InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            InvoiceListResponseDTO invoices = invoiceService.getInvoices(page, size, status, startDate, endDate);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy danh sách hóa đơn thành công", invoices));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //2. GET /api/invoices/{id} - Lấy chi tiết hóa đơn theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> getInvoiceById(@PathVariable Integer id) {
        try {
            InvoiceResponseDTO invoice = invoiceService.getInvoiceById(id);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy chi tiết hóa đơn thành công", invoice));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //3. POST /api/invoices - Tạo hóa đơn mới từ đơn hàng
    @PostMapping
    public ResponseEntity<ApiResponseDTO> createInvoice(@Valid @RequestBody CreateInvoiceRequestDTO requestDTO) {
        try {
            InvoiceResponseDTO invoice = invoiceService.createInvoice(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDTO(true, "Tạo hóa đơn thành công", invoice));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //4. PUT /api/invoices/{id}/status - Cập nhật trạng thái hóa đơn
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponseDTO> updateInvoiceStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateInvoiceStatusRequestDTO requestDTO) {
        try {
            InvoiceResponseDTO invoice = invoiceService.updateInvoiceStatus(id, requestDTO);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Cập nhật trạng thái hóa đơn thành công", invoice));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //5. GET /api/invoices/order/{orderId} - Lấy hóa đơn theo order_id
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponseDTO> getInvoiceByOrderId(@PathVariable Integer orderId) {
        try {
            InvoiceResponseDTO invoice = invoiceService.getInvoiceByOrderId(orderId);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy hóa đơn theo order ID thành công", invoice));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }
} 