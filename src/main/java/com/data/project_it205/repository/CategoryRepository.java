package com.data.project_it205.repository;

import com.data.project_it205.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // Tìm danh mục theo tên (để kiểm tra trùng lặp)
    Optional<Category> findByName(String name);

    // Tìm danh mục theo tên (không phân biệt hoa thường)
    Optional<Category> findByNameIgnoreCase(String name);

    // Lấy danh sách danh mục có sản phẩm
    @Query("SELECT c FROM Category c WHERE SIZE(c.products) > 0")
    List<Category> findCategoriesWithProducts();
    
    // Kiểm tra danh mục có sản phẩm hay không
    @Query("SELECT COUNT(p) > 0 FROM Category c JOIN c.products p WHERE c.id = :categoryId")
    boolean hasProducts(@Param("categoryId") Integer categoryId);

} 