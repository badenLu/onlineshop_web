package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.CartRequest;
import com.ecommerce.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemResponse>> getCart(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.success(cartService.getCart(userId));
    }

    @PostMapping
    public ApiResponse<Void> add(@Valid @RequestBody CartRequest.Add request,
                                 Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        cartService.addToCart(userId, request.getSkuId(), request.getQuantity());
        return ApiResponse.success();
    }

    @PutMapping("/{skuId}")
    public ApiResponse<Void> updateQuantity(@PathVariable Long skuId,
                                            @Valid @RequestBody CartRequest.Update request,
                                            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        cartService.updateQuantity(userId, skuId, request.getQuantity());
        return ApiResponse.success();
    }

    @DeleteMapping("/{skuId}")
    public ApiResponse<Void> remove(@PathVariable Long skuId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        cartService.removeFromCart(userId, skuId);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> clear(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        cartService.clearCart(userId);
        return ApiResponse.success();
    }
}