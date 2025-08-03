package com.data.project_it205.controller;

import com.data.project_it205.model.dto.request.ProductRequestDTO;
import com.data.project_it205.model.dto.request.ProductSearchRequestDTO;
import com.data.project_it205.model.dto.request.ProductUpdateRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.ProductListResponseDTO;
import com.data.project_it205.model.dto.response.ProductResponseDTO;
import com.data.project_it205.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    //1. GET /api/products - Lấy danh sách sản phẩm với phân trang và filter
    @GetMapping
    public ResponseEntity<ApiResponseDTO> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            ProductSearchRequestDTO searchRequest = new ProductSearchRequestDTO(name, categoryId, page, size);
            ProductListResponseDTO products = productService.getProducts(searchRequest);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy danh sách sản phẩm thành công", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //2. GET /api/products/{id} - Lấy thông tin chi tiết sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> getProductById(@PathVariable Integer id) {
        try {
            ProductResponseDTO product = productService.getProductById(id);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy thông tin sản phẩm thành công", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //3. POST /api/products - Tạo sản phẩm mới (chỉ admin hoặc sales)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        try {
            ProductResponseDTO product = productService.createProduct(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDTO(true, "Tạo sản phẩm thành công", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //4. PUT /api/products/{id} - Cập nhật sản phẩm (chỉ admin hoặc sales)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponseDTO> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody ProductUpdateRequestDTO requestDTO) {
        try {
            ProductResponseDTO product = productService.updateProduct(id, requestDTO);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Cập nhật sản phẩm thành công", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    //5. DELETE /api/products/{id} - Xóa sản phẩm (chỉ admin hoặc sales)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponseDTO> deleteProduct(@PathVariable Integer id) {
        try {
            ApiResponseDTO response = productService.deleteProduct(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }
}