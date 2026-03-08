package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNo = :orderNo")
    Optional<Order> findByOrderNo(@Param("orderNo") String orderNo);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNo = :orderNo AND o.user.id = :userId")
    Optional<Order> findByOrderNoAndUserId(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    @Query(value = "SELECT o FROM Order o WHERE o.user.id = :userId",
            countQuery = "SELECT count(o) FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status",
            countQuery = "SELECT count(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Page<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status, Pageable pageable);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime time);
}