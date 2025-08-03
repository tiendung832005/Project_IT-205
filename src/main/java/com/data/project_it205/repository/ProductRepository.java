package com.data.project_it205.repository;

import com.data.project_it205.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Lấy sản phẩm chưa bị xóa mềm
    Page<Product> findByIsDeletedFalse(Pageable pageable);

    // Lấy sản phẩm theo category và chưa bị xóa mềm
    Page<Product> findByCategoryIdAndIsDeletedFalse(Integer categoryId, Pageable pageable);

    // Tìm kiếm sản phẩm theo tên và chưa bị xóa mềm
    Page<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    // Tìm kiếm sản phẩm theo category, tên và chưa bị xóa mềm
    Page<Product> findByCategoryIdAndNameContainingIgnoreCaseAndIsDeletedFalse(
            Integer categoryId, String name, Pageable pageable);

    // Lấy sản phẩm theo ID và chưa bị xóa mềm
    Optional<Product> findByIdAndIsDeletedFalse(Integer id);

    // Kiểm tra sản phẩm có trong order hay không
    @Query("SELECT COUNT(oi) > 0 FROM Product p JOIN p.orderItems oi WHERE p.id = :productId")
    boolean hasOrderItems(@Param("productId") Integer productId);

    // Lấy danh sách sản phẩm theo category (không phân trang)
    List<Product> findByCategoryIdAndIsDeletedFalse(Integer categoryId);

    // Tìm kiếm sản phẩm theo tên (không phân trang)
    List<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);
}