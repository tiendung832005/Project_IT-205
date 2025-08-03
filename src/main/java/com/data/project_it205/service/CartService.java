package com.data.project_it205.service;

import com.data.project_it205.model.dto.request.AddToCartRequestDTO;
import com.data.project_it205.model.dto.request.UpdateCartItemRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.CartItemResponseDTO;
import com.data.project_it205.model.dto.response.CartResponseDTO;
import com.data.project_it205.model.entity.CartItem;
import com.data.project_it205.model.entity.Product;
import com.data.project_it205.model.entity.User;
import com.data.project_it205.repository.CartItemRepository;
import com.data.project_it205.repository.ProductRepository;
import com.data.project_it205.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy thông tin user hiện tại từ JWT
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // Lấy tất cả items trong giỏ hàng của user hiện tại
    public CartResponseDTO getCart() {
        User currentUser = getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findByUserId(currentUser.getId());

        List<CartItemResponseDTO> items = cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponseDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(items, items.size(), totalAmount);
    }

    // Thêm sản phẩm vào giỏ hàng
    public CartItemResponseDTO addToCart(AddToCartRequestDTO requestDTO) {
        User currentUser = getCurrentUser();

        // Kiểm tra sản phẩm có tồn tại và chưa bị xóa không
        Product product = productRepository.findByIdAndIsDeletedFalse(requestDTO.getProductId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + requestDTO.getProductId()));

        // Kiểm tra stock
        if (product.getStock() == 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng");
        }

        // Kiểm tra số lượng yêu cầu có vượt quá stock không
        if (requestDTO.getQuantity() > product.getStock()) {
            throw new RuntimeException(
                    "Số lượng yêu cầu vượt quá hàng tồn kho. Chỉ còn " + product.getStock() + " sản phẩm");
        }

        // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
        CartItem existingCartItem = cartItemRepository
                .findByUserIdAndProductId(currentUser.getId(), requestDTO.getProductId())
                .orElse(null);

        if (existingCartItem != null) {
            // Cộng dồn số lượng
            int newQuantity = existingCartItem.getQuantity() + requestDTO.getQuantity();

            // Kiểm tra tổng số lượng có vượt quá stock không
            if (newQuantity > product.getStock()) {
                throw new RuntimeException(
                        "Tổng số lượng vượt quá hàng tồn kho. Chỉ còn " + product.getStock() + " sản phẩm");
            }

            existingCartItem.setQuantity(newQuantity);
            existingCartItem.setUpdatedAt(LocalDate.now());
            CartItem savedCartItem = cartItemRepository.save(existingCartItem);
            return convertToDTO(savedCartItem);
        } else {
            // Tạo cart item mới
            CartItem newCartItem = new CartItem();
            newCartItem.setUser(currentUser);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(requestDTO.getQuantity());
            newCartItem.setCreatedAt(LocalDate.now());

            CartItem savedCartItem = cartItemRepository.save(newCartItem);
            return convertToDTO(savedCartItem);
        }
    }

    // Cập nhật số lượng item trong giỏ hàng
    public CartItemResponseDTO updateCartItem(Integer cartItemId, UpdateCartItemRequestDTO requestDTO) {
        User currentUser = getCurrentUser();

        // Kiểm tra cart item có thuộc về user hiện tại không
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item trong giỏ hàng"));

        // Kiểm tra stock
        Product product = cartItem.getProduct();
        if (requestDTO.getQuantity() > product.getStock()) {
            throw new RuntimeException(
                    "Số lượng yêu cầu vượt quá hàng tồn kho. Chỉ còn " + product.getStock() + " sản phẩm");
        }

        cartItem.setQuantity(requestDTO.getQuantity());
        cartItem.setUpdatedAt(LocalDate.now());

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return convertToDTO(savedCartItem);
    }

    // Xóa 1 item khỏi giỏ hàng
    public ApiResponseDTO removeCartItem(Integer cartItemId) {
        User currentUser = getCurrentUser();

        // Kiểm tra cart item có thuộc về user hiện tại không
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item trong giỏ hàng"));

        cartItemRepository.delete(cartItem);

        return new ApiResponseDTO(true, "Xóa item khỏi giỏ hàng thành công", null);
    }

    // Xóa toàn bộ giỏ hàng
    @Transactional
    public ApiResponseDTO clearCart() {
        User currentUser = getCurrentUser();

        List<CartItem> cartItems = cartItemRepository.findByUserId(currentUser.getId());
        cartItemRepository.deleteAll(cartItems);

        return new ApiResponseDTO(true, "Xóa toàn bộ giỏ hàng thành công", null);
    }

    // Chuyển đổi Entity sang DTO
    private CartItemResponseDTO convertToDTO(CartItem cartItem) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductDescription(cartItem.getProduct().getDescription());
        dto.setProductPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setTotalPrice(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());
        return dto;
    }
}