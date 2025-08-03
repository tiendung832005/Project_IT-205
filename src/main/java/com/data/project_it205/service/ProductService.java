package com.data.project_it205.service;

import com.data.project_it205.model.dto.request.ProductRequestDTO;
import com.data.project_it205.model.dto.request.ProductSearchRequestDTO;
import com.data.project_it205.model.dto.request.ProductUpdateRequestDTO;
import com.data.project_it205.model.dto.response.ApiResponseDTO;
import com.data.project_it205.model.dto.response.ProductListResponseDTO;
import com.data.project_it205.model.dto.response.ProductResponseDTO;
import com.data.project_it205.model.entity.Category;
import com.data.project_it205.model.entity.Product;
import com.data.project_it205.repository.CategoryRepository;
import com.data.project_it205.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy danh sách sản phẩm với phân trang và filter
    public ProductListResponseDTO getProducts(ProductSearchRequestDTO searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        Page<Product> productPage;

        if (searchRequest.getCategoryId() != null && searchRequest.getName() != null && !searchRequest.getName().trim().isEmpty()) {
            // Tìm theo cả category và tên
            productPage = productRepository.findByCategoryIdAndNameContainingIgnoreCaseAndIsDeletedFalse(
                    searchRequest.getCategoryId(), searchRequest.getName().trim(), pageable);
        } else if (searchRequest.getCategoryId() != null) {
            // Tìm theo category
            productPage = productRepository.findByCategoryIdAndIsDeletedFalse(searchRequest.getCategoryId(), pageable);
        } else if (searchRequest.getName() != null && !searchRequest.getName().trim().isEmpty()) {
            // Tìm theo tên
            productPage = productRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(
                    searchRequest.getName().trim(), pageable);
        } else {
            // Lấy tất cả
            productPage = productRepository.findByIsDeletedFalse(pageable);
        }

        List<ProductResponseDTO> products = productPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new ProductListResponseDTO(
                products,
                productPage.getNumber(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getSize()
        );
    }

    // Lấy thông tin chi tiết sản phẩm theo ID
    public ProductResponseDTO getProductById(Integer productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        return convertToDTO(product);
    }

    // Tạo sản phẩm mới
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        // Kiểm tra category có tồn tại không
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + requestDTO.getCategoryId()));

        Product product = new Product();
        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setPrice(requestDTO.getPrice());
        product.setStock(requestDTO.getStock());
        product.setCategory(category);
        product.setCreatedAt(LocalDate.now());

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    // Cập nhật thông tin sản phẩm (không được sửa stock)
    public ProductResponseDTO updateProduct(Integer productId, ProductUpdateRequestDTO requestDTO) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        // Kiểm tra category có tồn tại không
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + requestDTO.getCategoryId()));

        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setPrice(requestDTO.getPrice());
        product.setCategory(category);
        product.setUpdatedAt(LocalDate.now());

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    // Xóa mềm sản phẩm
    public ApiResponseDTO deleteProduct(Integer productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        // Kiểm tra sản phẩm có trong order không
        if (productRepository.hasOrderItems(productId)) {
            throw new RuntimeException("Không thể xóa sản phẩm vì nó đã được đặt hàng");
        }

        product.setIsDeleted(true);
        product.setDeletedAt(LocalDate.now());
        productRepository.save(product);

        return new ApiResponseDTO(true, "Xóa sản phẩm thành công", null);
    }

    // Chuyển đổi Entity sang DTO
    private ProductResponseDTO convertToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}