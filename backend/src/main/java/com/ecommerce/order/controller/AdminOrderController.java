package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRepository orderRepository;

    @GetMapping
    public ApiResponse<Page<Order>> listAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(orderRepository.findAll(pageable));
    }

    @PutMapping("/{id}/ship")
    public ApiResponse<Void> ship(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Order"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Only paid orders can be shipped");
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setDeliveryTime(LocalDateTime.now());
        orderRepository.save(order);
        return ApiResponse.success();
    }
}
