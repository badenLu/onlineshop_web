package com.ecommerce.user.service;

import com.ecommerce.user.dto.AuthRequest;
import com.ecommerce.user.dto.AuthResponse;

public interface AuthService {
    AuthResponse register(AuthRequest.Register request);
    AuthResponse login(AuthRequest.Login request);
    AuthResponse refresh(String refreshToken);
    void logout(String accessToken);
}