package com.data.project_it205.service;

import com.data.project_it205.model.dto.request.CancelOrderRequestDTO;
import com.data.project_it205.model.dto.request.CreateOrderRequestDTO;
import com.data.project_it205.model.dto.request.UpdateOrderRequestDTO;
import com.data.project_it205.model.dto.request.UpdateOrderStatusRequestDTO;
import com.data.project_it205.model.dto.response.*;
import com.data.project_it205.model.entity.*;
import com.data.project_it205.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
public class OrderService {

    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderLogRepository orderLogRepository;

    // Lấy thông tin user hiện tại từ JWT
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // Lấy danh sách đơn hàng (phân quyền theo role)
    public OrderListResponseDTO getOrders(Integer page, Integer size, Order.OrderStatus status) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;

        // Phân quyền theo role
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        if ("CUSTOMER".equals(roleName)) {
            // Customer chỉ thấy đơn hàng của mình
            if (status != null) {
                orderPage = orderRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable);
            } else {
                orderPage = orderRepository.findByUserId(currentUser.getId(), pageable);
            }
        } else {
            // ADMIN/SALES thấy tất cả đơn hàng
            if (status != null) {
                orderPage = orderRepository.findByStatus(status, pageable);
            } else {
                orderPage = orderRepository.findAll(pageable);
            }
        }

        List<OrderResponseDTO> orders = orderPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new OrderListResponseDTO(
                orders,
                orderPage.getNumber(),
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                orderPage.getSize()
        );
    }

    // Lấy chi tiết đơn hàng
    public OrderResponseDTO getOrderById(Integer orderId) {
        User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        Order order;
        if ("CUSTOMER".equals(roleName)) {
            // Customer chỉ thấy đơn hàng của mình
            order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        } else {
            // ADMIN/SALES có thể xem tất cả đơn hàng
            order = orderRepository.findByIdWithOrderItems(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        }

        return convertToDTO(order);
    }

    // Tạo đơn hàng mới từ giỏ hàng
    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequestDTO requestDTO) {
        User currentUser = getCurrentUser();

        // Lấy giỏ hàng của user
        List<CartItem> cartItems = cartItemRepository.findByUserId(currentUser.getId());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể tạo đơn hàng");
        }

        // Tính tổng tiền
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tạo đơn hàng mới
        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(requestDTO.getShippingAddress());
        order.setInternalNotes(requestDTO.getInternalNotes());
        order.setTotalPrice(totalPrice);
        order.setCreatedAt(LocalDate.now());

        Order savedOrder = orderRepository.save(order);

        // Tạo order items và trừ stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Kiểm tra stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ hàng. Chỉ còn " + product.getStock() + " sản phẩm");
            }

            // Trừ stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // Tạo order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItemRepository.save(orderItem);
        }

        // Xóa toàn bộ giỏ hàng
        cartItemRepository.deleteAll(cartItems);

        // Log việc tạo đơn hàng
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(savedOrder);
        orderLog.setUser(currentUser);
        orderLog.setAction(OrderLog.LogAction.ORDER_CREATED);
        orderLog.setReason("Tạo đơn hàng mới");
        orderLogRepository.save(orderLog);

        return convertToDTO(savedOrder);
    }

    // Cập nhật trạng thái đơn hàng
    @Transactional
    public OrderResponseDTO updateOrderStatus(Integer orderId, UpdateOrderStatusRequestDTO requestDTO) {
        User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        Order order;
        if ("CUSTOMER".equals(roleName)) {
            // Customer chỉ có thể hủy đơn hàng của mình
            if (requestDTO.getStatus() != Order.OrderStatus.CANCELLED) {
                throw new RuntimeException("Bạn chỉ có thể hủy đơn hàng");
            }
            order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        } else {
            // ADMIN/SALES có thể cập nhật trạng thái tất cả đơn hàng
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        }

        // Kiểm tra trạng thái hiện tại
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Không thể cập nhật đơn hàng đã bị hủy");
        }

        // Nếu hủy đơn hàng, hoàn trả stock
        if (requestDTO.getStatus() == Order.OrderStatus.CANCELLED && order.getStatus() != Order.OrderStatus.CANCELLED) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            for (OrderItem orderItem : orderItems) {
                Product product = orderItem.getProduct();
                product.setStock(product.getStock() + orderItem.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(requestDTO.getStatus());
        order.setUpdatedAt(LocalDate.now());

        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    // Chuyển đổi Entity sang DTO
    private OrderResponseDTO convertToDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setInternalNotes(order.getInternalNotes());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Convert order items
        List<OrderItemResponseDTO> orderItems = order.getOrderItems().stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());
        dto.setOrderItems(orderItems);

        return dto;
    }

    private OrderItemResponseDTO convertOrderItemToDTO(OrderItem orderItem) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setProductDescription(orderItem.getProduct().getDescription());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setTotalPrice(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        return dto;
    }

    // Cập nhật thông tin đơn hàng (address, notes) - chỉ khi status = PENDING
    @Transactional
    public OrderResponseDTO updateOrder(Integer orderId, UpdateOrderRequestDTO requestDTO) {
        User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        Order order;
        if ("CUSTOMER".equals(roleName)) {
            // Customer chỉ có thể cập nhật đơn hàng của mình
            order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        } else {
            // ADMIN/SALES có thể cập nhật tất cả đơn hàng
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        }

        // Kiểm tra trạng thái đơn hàng - chỉ cho phép cập nhật khi PENDING
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể cập nhật đơn hàng đang ở trạng thái PENDING");
        }

        // Cập nhật thông tin
        order.setShippingAddress(requestDTO.getShippingAddress());
        order.setInternalNotes(requestDTO.getInternalNotes());
        order.setUpdatedAt(LocalDate.now());

        Order updatedOrder = orderRepository.save(order);

        // Log việc cập nhật đơn hàng
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(updatedOrder);
        orderLog.setUser(currentUser);
        orderLog.setAction(OrderLog.LogAction.ORDER_UPDATED);
        orderLog.setReason("Cập nhật thông tin đơn hàng");
        orderLogRepository.save(orderLog);

        return convertToDTO(updatedOrder);
    }

    // Hủy đơn hàng
    @Transactional
    public OrderResponseDTO cancelOrder(Integer orderId, CancelOrderRequestDTO requestDTO) {
        User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        Order order;
        if ("CUSTOMER".equals(roleName)) {
            // Customer chỉ có thể hủy đơn hàng của mình
            order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        } else {
            // ADMIN/SALES có thể hủy tất cả đơn hàng
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        }

        // Kiểm tra trạng thái đơn hàng - chỉ cho phép hủy khi PENDING
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang ở trạng thái PENDING");
        }

        // Log lý do hủy
        logger.info("Đơn hàng ID: " + orderId + " bị hủy bởi user: " + currentUser.getUsername() + 
                   " - Lý do: " + requestDTO.getCancelReason());

        // Hoàn trả stock của các sản phẩm
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
            productRepository.save(product);
        }

        // Cập nhật trạng thái đơn hàng
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDate.now());

        Order cancelledOrder = orderRepository.save(order);

        // Log việc hủy đơn hàng
        OrderLog orderLog = new OrderLog();
        orderLog.setOrder(cancelledOrder);
        orderLog.setUser(currentUser);
        orderLog.setAction(OrderLog.LogAction.ORDER_CANCELLED);
        orderLog.setReason(requestDTO.getCancelReason());
        orderLogRepository.save(orderLog);

        return convertToDTO(cancelledOrder);
    }

    // Lấy danh sách items của một đơn hàng
    public OrderItemListResponseDTO getOrderItems(Integer orderId) {
        User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        Order order;
        if ("CUSTOMER".equals(roleName)) {
            // Customer chỉ có thể xem items của đơn hàng của mình
            order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        } else {
            // ADMIN/SALES có thể xem items của tất cả đơn hàng
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<OrderItemResponseDTO> orderItemDTOs = orderItems.stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());

        return new OrderItemListResponseDTO(
                orderId,
                order.getStatus().toString(),
                orderItemDTOs,
                orderItemDTOs.size()
        );
    }

    // Lấy logs của đơn hàng (chỉ admin/sales)
    public List<OrderLogResponseDTO> getOrderLogs(Integer orderId) {
        User currentUser = getCurrentUser();
        String roleName = userRepository.findRoleNameByUsername(currentUser.getUsername());

        // Chỉ admin/sales mới có thể xem logs
        if ("CUSTOMER".equals(roleName)) {
            throw new RuntimeException("Bạn không có quyền xem logs của đơn hàng");
        }

        // Kiểm tra đơn hàng tồn tại
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        List<OrderLog> orderLogs = orderLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        
        return orderLogs.stream()
                .map(this::convertOrderLogToDTO)
                .collect(Collectors.toList());
    }

    private OrderLogResponseDTO convertOrderLogToDTO(OrderLog orderLog) {
        OrderLogResponseDTO dto = new OrderLogResponseDTO();
        dto.setId(orderLog.getId());
        dto.setOrderId(orderLog.getOrder().getId());
        dto.setUsername(orderLog.getUser().getUsername());
        dto.setAction(orderLog.getAction().toString());
        dto.setReason(orderLog.getReason());
        dto.setCreatedAt(orderLog.getCreatedAt());
        return dto;
    }
}