package com.ecommerce.product.repository;

import com.ecommerce.product.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {

    List<ProductSku> findByProductId(Long productId);

    @Query("SELECT s FROM ProductSku s JOIN FETCH s.product WHERE s.id = :id")
    Optional<ProductSku> findByIdWithProduct(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ProductSku s SET s.stock = s.stock - :quantity WHERE s.id = :skuId AND s.stock >= :quantity")
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductSku s SET s.stock = s.stock + :quantity WHERE s.id = :skuId")
    int restoreStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
}