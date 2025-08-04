package com.data.project_it205.service;

import com.data.project_it205.model.dto.request.CreateInvoiceRequestDTO;
import com.data.project_it205.model.dto.request.UpdateInvoiceStatusRequestDTO;
import com.data.project_it205.model.dto.response.InvoiceListResponseDTO;
import com.data.project_it205.model.dto.response.InvoiceResponseDTO;
import com.data.project_it205.model.entity.Invoice;
import com.data.project_it205.model.entity.Order;
import com.data.project_it205.repository.InvoiceRepository;
import com.data.project_it205.repository.OrderRepository;
import com.data.project_it205.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy thông tin user hiện tại từ JWT
    private com.data.project_it205.model.entity.User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // Lấy danh sách hóa đơn (có phân trang, filter theo trạng thái, thời gian)
    public InvoiceListResponseDTO getInvoices(Integer page, Integer size, Invoice.InvoiceStatus status,
            LocalDate startDate, LocalDate endDate) {
        com.data.project_it205.model.entity.User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoicePage;

        // Phân quyền theo role
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        if ("CUSTOMER".equals(roleName)) {
            // Customer chỉ thấy hóa đơn của mình
            if (status != null && startDate != null && endDate != null) {
                invoicePage = invoiceRepository.findByUserIdAndStatusAndCreatedAtBetween(
                        currentUser.getId(), status, startDate, endDate, pageable);
            } else if (status != null) {
                invoicePage = invoiceRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable);
            } else if (startDate != null && endDate != null) {
                invoicePage = invoiceRepository.findByUserIdAndCreatedAtBetween(
                        currentUser.getId(), startDate, endDate, pageable);
            } else {
                invoicePage = invoiceRepository.findByUserId(currentUser.getId(), pageable);
            }
        } else {
            // ADMIN/SALES thấy tất cả hóa đơn
            if (status != null && startDate != null && endDate != null) {
                invoicePage = invoiceRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate, pageable);
            } else if (status != null) {
                invoicePage = invoiceRepository.findByStatus(status, pageable);
            } else if (startDate != null && endDate != null) {
                invoicePage = invoiceRepository.findByCreatedAtBetween(startDate, endDate, pageable);
            } else {
                invoicePage = invoiceRepository.findAll(pageable);
            }
        }

        List<InvoiceResponseDTO> invoices = invoicePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new InvoiceListResponseDTO(
                invoices,
                invoicePage.getNumber(),
                invoicePage.getTotalPages(),
                invoicePage.getTotalElements(),
                invoicePage.getSize());
    }

    // Lấy chi tiết hóa đơn theo ID
    public InvoiceResponseDTO getInvoiceById(Integer invoiceId) {
        com.data.project_it205.model.entity.User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        Invoice invoice;
        if ("CUSTOMER".equals(roleName)) {
            // Customer chỉ thấy hóa đơn của mình
            invoice = invoiceRepository.findById(invoiceId)
                    .filter(inv -> inv.getOrder().getUser().getId().equals(currentUser.getId()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
        } else {
            // ADMIN/SALES có thể xem tất cả hóa đơn
            invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));
        }

        return convertToDTO(invoice);
    }

    // Tạo hóa đơn mới từ đơn hàng
    @Transactional
    public InvoiceResponseDTO createInvoice(CreateInvoiceRequestDTO requestDTO) {
        com.data.project_it205.model.entity.User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        // Chỉ ADMIN/SALES mới có thể tạo hóa đơn
        if ("CUSTOMER".equals(roleName)) {
            throw new RuntimeException("Bạn không có quyền tạo hóa đơn");
        }

        // Kiểm tra đơn hàng tồn tại
        Order order = orderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + requestDTO.getOrderId()));

        // Kiểm tra đã có hóa đơn cho đơn hàng này chưa
        if (invoiceRepository.findByOrderId(requestDTO.getOrderId()).isPresent()) {
            throw new RuntimeException("Đã có hóa đơn cho đơn hàng này");
        }

        // Tạo hóa đơn mới
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setTotalAmount(order.getTotalPrice());
        invoice.setCreatedAt(LocalDate.now());

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return convertToDTO(savedInvoice);
    }

    // Cập nhật trạng thái hóa đơn
    @Transactional
    public InvoiceResponseDTO updateInvoiceStatus(Integer invoiceId, UpdateInvoiceStatusRequestDTO requestDTO) {
        com.data.project_it205.model.entity.User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        // Chỉ ADMIN/SALES mới có thể cập nhật trạng thái
        if ("CUSTOMER".equals(roleName)) {
            throw new RuntimeException("Bạn không có quyền cập nhật trạng thái hóa đơn");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        invoice.setStatus(requestDTO.getStatus());
        invoice.setUpdatedAt(LocalDate.now());

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return convertToDTO(updatedInvoice);
    }

    // Lấy hóa đơn theo order_id
    public InvoiceResponseDTO getInvoiceByOrderId(Integer orderId) {
        com.data.project_it205.model.entity.User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        Invoice invoice;
        if ("CUSTOMER".equals(roleName)) {
            // Customer chỉ thấy hóa đơn của mình
            invoice = invoiceRepository.findByUserIdAndOrderId(currentUser.getId(), orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn cho đơn hàng ID: " + orderId));
        } else {
            // ADMIN/SALES có thể xem tất cả hóa đơn
            invoice = invoiceRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn cho đơn hàng ID: " + orderId));
        }

        return convertToDTO(invoice);
    }

    // Chuyển đổi Entity sang DTO
    private InvoiceResponseDTO convertToDTO(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setOrderId(invoice.getOrder().getId());
        dto.setOrderStatus(invoice.getOrder().getStatus().toString());
        dto.setInvoiceStatus(invoice.getStatus().toString());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        dto.setCustomerName(invoice.getOrder().getUser().getUsername());
        dto.setCustomerEmail(invoice.getOrder().getUser().getEmail());
        return dto;
    }
}