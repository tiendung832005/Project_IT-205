package com.data.project_it205.controller;

import com.data.project_it205.model.dto.request.CategoryRequestDTO;
import com.data.project_it205.model.dto.request.CategoryUpdateRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.CategoryResponseDTO;
import com.data.project_it205.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // GET /api/categories - Lấy danh sách tất cả danh mục
    @GetMapping
    public ResponseEntity<ApiResponseDTO> getAllCategories() {
        try {
            List<CategoryResponseDTO> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponseDTO(true, "Lấy danh sách danh mục thành công", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }


    // POST /api/categories - Tạo danh mục mới (chỉ admin)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        try {
            CategoryResponseDTO category = categoryService.createCategory(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDTO(true, "Tạo danh mục thành công", category));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    // PUT /api/categories/{id} - Cập nhật danh mục (chỉ admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryUpdateRequestDTO requestDTO) {
        try {
            CategoryResponseDTO category = categoryService.updateCategory(id, requestDTO);
            return ResponseEntity.ok(new ApiResponseDTO(true, "Cập nhật danh mục thành công", category));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    // DELETE /api/categories/{id} - Xóa danh mục (chỉ admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO> deleteCategory(@PathVariable Integer id) {
        try {
            ApiResponseDTO response = categoryService.deleteCategory(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO(false, "Lỗi: " + e.getMessage(), null));
        }
    }


}