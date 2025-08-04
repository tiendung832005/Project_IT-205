package com.data.project_it205.service;

import com.data.project_it205.model.dto.request.CreatePaymentRequestDTO;
import com.data.project_it205.model.dto.response.PaymentResponseDTO;
import com.data.project_it205.model.entity.Invoice;
import com.data.project_it205.model.entity.Payment;
import com.data.project_it205.repository.InvoiceRepository;
import com.data.project_it205.repository.PaymentRepository;
import com.data.project_it205.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy thông tin user hiện tại từ JWT
    private com.data.project_it205.model.entity.User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // Tạo thanh toán mới cho hóa đơn
    @Transactional
    public PaymentResponseDTO createPayment(CreatePaymentRequestDTO requestDTO) {
        com.data.project_it205.model.entity.User currentUser = getCurrentUser();
        
        // Kiểm tra hóa đơn tồn tại
        Invoice invoice = invoiceRepository.findById(requestDTO.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + requestDTO.getInvoiceId()));

        // Kiểm tra quyền: CUSTOMER chỉ được thanh toán hóa đơn của mình, ADMIN/SALES có thể thanh toán tất cả
        String userRole = userRepository.findRoleNameByUsername(currentUser.getUsername());
        if ("CUSTOMER".equals(userRole) && !invoice.getOrder().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền thanh toán hóa đơn này");
        }

        // Kiểm tra hóa đơn chưa được thanh toán
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Hóa đơn đã được thanh toán");
        }

        // Kiểm tra hóa đơn không bị hủy
        if (invoice.getStatus() == Invoice.InvoiceStatus.FAILED) {
            throw new RuntimeException("Không thể thanh toán hóa đơn đã bị hủy");
        }

        // Tạo payment mới
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setMethod(requestDTO.getMethod());
        payment.setAmount(requestDTO.getAmount());
        payment.setStatus(Payment.PaymentStatus.SUCCESS); // Mặc định thành công
        payment.setTransactionId(requestDTO.getTransactionId());
        payment.setCreatedAt(LocalDate.now());
        payment.setUpdatedAt(LocalDate.now());

        // Lưu payment
        Payment savedPayment = paymentRepository.save(payment);

        // Cập nhật trạng thái hóa đơn thành PAID
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setUpdatedAt(LocalDate.now());
        invoiceRepository.save(invoice);

        return convertToDTO(savedPayment);
    }

    // Lấy thông tin chi tiết của 1 payment
    public PaymentResponseDTO getPaymentById(Integer paymentId) {
        com.data.project_it205.model.entity.User currentUser = getCurrentUser();
        
        // Kiểm tra quyền: CUSTOMER chỉ được xem payment của mình, ADMIN/SALES có thể xem tất cả
        String userRole = userRepository.findRoleNameByUsername(currentUser.getUsername());
        Payment payment;
        
        if ("CUSTOMER".equals(userRole)) {
            // CUSTOMER chỉ được xem payment của mình
            payment = paymentRepository.findByIdAndUserId(paymentId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với ID: " + paymentId));
        } else {
            // ADMIN/SALES có thể xem tất cả payment
            payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán với ID: " + paymentId));
        }

        return convertToDTO(payment);
    }

    // Chuyển đổi Entity sang DTO
    private PaymentResponseDTO convertToDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setInvoiceId(payment.getInvoice().getId());
        dto.setPaymentMethod(payment.getMethod().name());
        dto.setAmount(payment.getAmount());
        dto.setPaymentStatus(payment.getStatus().name());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        
        // Thông tin khách hàng
        dto.setCustomerName(payment.getInvoice().getOrder().getUser().getUsername());
        dto.setCustomerEmail(payment.getInvoice().getOrder().getUser().getEmail());
        
        return dto;
    }
} 