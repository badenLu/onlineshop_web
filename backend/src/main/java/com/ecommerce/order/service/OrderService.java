package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PaymentRequest;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse createOrder(Long userId, OrderRequest request);
    OrderResponse getOrder(Long userId, String orderNo);
    Page<OrderResponse> listOrders(Long userId, String status, int page, int size);
    void cancelOrder(Long userId, String orderNo);
    void payOrder(Long userId, String orderNo, PaymentRequest request);
    void confirmReceipt(Long userId, String orderNo);
}