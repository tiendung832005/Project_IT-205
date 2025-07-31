package com.data.project_it205.service;

import com.data.project_it205.model.dto.request.CategoryRequestDTO;
import com.data.project_it205.model.dto.request.CategoryUpdateRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.CategoryResponseDTO;
import com.data.project_it205.model.entity.Category;
import com.data.project_it205.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy danh sách tất cả danh mục
    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tạo danh mục mới
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO) {
        // Kiểm tra tên danh mục đã tồn tại chưa
        if (categoryRepository.findByNameIgnoreCase(requestDTO.getName()).isPresent()) {
            throw new RuntimeException("Danh mục với tên '" + requestDTO.getName() + "' đã tồn tại");
        }

        Category category = new Category();
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setCreatedAt(LocalDate.now());

        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    // Cập nhật thông tin danh mục
    public CategoryResponseDTO updateCategory(Integer categoryId, CategoryUpdateRequestDTO requestDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));

        // Kiểm tra tên danh mục đã tồn tại chưa (trừ danh mục hiện tại)
        categoryRepository.findByNameIgnoreCase(requestDTO.getName())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(categoryId)) {
                        throw new RuntimeException("Danh mục với tên '" + requestDTO.getName() + "' đã tồn tại");
                    }
                });

        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setUpdatedAt(LocalDate.now());

        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    // Xóa danh mục (soft delete)
    public ApiResponseDTO deleteCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));

        // Kiểm tra danh mục có chứa sản phẩm không
        if (categoryRepository.hasProducts(categoryId)) {
            throw new RuntimeException("Không thể xóa danh mục vì nó chứa sản phẩm");
        }

        categoryRepository.delete(category);
        
        return new ApiResponseDTO(true, "Xóa danh mục thành công", null);
    }

    // Chuyển đổi Entity sang DTO
    private CategoryResponseDTO convertToDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setProductCount(category.getProducts() != null ? category.getProducts().size() : 0);
        return dto;
    }
} 