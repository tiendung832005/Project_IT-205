package com.data.project_it205.controller;

import com.data.project_it205.model.dto.request.AddToCartRequestDTO;
import com.data.project_it205.model.dto.request.UpdateCartItemRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.CartItemResponseDTO;
import com.data.project_it205.model.dto.response.CartResponseDTO;
import com.data.project_it205.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    //1. GET /api/cart - Lấy tất cả items trong giỏ hàng
    @GetMapping
    public ResponseEntity<ApiResponseDTO> getCart() {
        try {
            CartResponseDTO cart = cartService.getCart();
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy giỏ hàng thành công", cart));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //2. POST /api/cart - Thêm sản phẩm vào giỏ hàng
    @PostMapping
    public ResponseEntity<ApiResponseDTO> addToCart(@Valid @RequestBody AddToCartRequestDTO requestDTO) {
        try {
            CartItemResponseDTO cartItem = cartService.addToCart(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDTO(true, "Thêm vào giỏ hàng thành công", cartItem));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //3. PUT /api/cart/{id} - Cập nhật số lượng item trong giỏ hàng
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> updateCartItem(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCartItemRequestDTO requestDTO) {
        try {
            CartItemResponseDTO cartItem = cartService.updateCartItem(id, requestDTO);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Cập nhật giỏ hàng thành công", cartItem));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //4. DELETE /api/cart/{id} - Xóa 1 item khỏi giỏ hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> removeCartItem(@PathVariable Integer id) {
        try {
            ApiResponseDTO response = cartService.removeCartItem(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //5. DELETE /api/cart - Xóa toàn bộ giỏ hàng
    @DeleteMapping
    public ResponseEntity<ApiResponseDTO> clearCart() {
        try {
            ApiResponseDTO response = cartService.clearCart();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }
}