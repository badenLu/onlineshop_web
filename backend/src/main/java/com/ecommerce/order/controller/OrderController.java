package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PaymentRequest;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody OrderRequest request,
                                             Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(orderService.createOrder(userId, request));
    }

    @GetMapping
    public ApiResponse<Page<OrderResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(orderService.listOrders(userId, status, page, size));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderResponse> detail(@PathVariable String orderNo,
                                             Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(orderService.getOrder(userId, orderNo));
    }

    @PutMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String orderNo,
                                    Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        orderService.cancelOrder(userId, orderNo);
        return ApiResponse.success();
    }

    @PostMapping("/{orderNo}/pay")
    public ApiResponse<Void> pay(@PathVariable String orderNo,
                                 @Valid @RequestBody PaymentRequest request,
                                 Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        orderService.payOrder(userId, orderNo, request);
        return ApiResponse.success();
    }

    @PutMapping("/{orderNo}/confirm")
    public ApiResponse<Void> confirm(@PathVariable String orderNo,
                                     Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        orderService.confirmReceipt(userId, orderNo);
        return ApiResponse.success();
    }
}