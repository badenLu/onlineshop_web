package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByStatus(ProductStatus productStatus, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus productStatus, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = :status AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "or LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword,
                                  @Param("status") ProductStatus productStatus,
                                  Pageable pageable);
}
