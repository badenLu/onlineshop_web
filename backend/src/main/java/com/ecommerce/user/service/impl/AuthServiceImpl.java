package com.ecommerce.user.service.impl;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.user.dto.AuthRequest;
import com.ecommerce.user.dto.AuthResponse;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.enums.UserRole;
import com.ecommerce.user.enums.UserStatus;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.infrastructure.security.JwtTokenProvider;
import com.ecommerce.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    @Transactional
    public AuthResponse register(AuthRequest.Register request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.duplicateEmail();
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(AuthRequest.Login request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(BusinessException::unauthorized);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw BusinessException.unauthorized();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(403, "Account is disabled");
        }

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(401, "Invalid refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User"));

        return buildAuthResponse(user);
    }

    @Override
    public void logout(String accessToken) {
        long expiration = jwtTokenProvider.getAccessTokenExpiration();
        redisTemplate.opsForValue().set(
                TOKEN_BLACKLIST_PREFIX + accessToken, "1",
                expiration, TimeUnit.MILLISECONDS
        );
        log.info("Token blacklisted");
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
                .role(user.getRole().name())
                .build();
    }
}