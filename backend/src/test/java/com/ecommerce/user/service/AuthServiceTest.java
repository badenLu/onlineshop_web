package com.ecommerce.user.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.user.dto.AuthRequest;
import com.ecommerce.user.dto.AuthResponse;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.enums.UserRole;
import com.ecommerce.user.enums.UserStatus;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.impl.AuthServiceImpl;
import com.ecommerce.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .passwordHash("encoded_password")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Register")
    class Register {

        @Test
        @DisplayName("Should register successfully with valid input")
        void register_Success() {
            AuthRequest.Register request = new AuthRequest.Register();
            request.setUsername("newuser");
            request.setEmail("new@test.com");
            request.setPassword("password123");

            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(2L);
                return u;
            });
            when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                    .thenReturn("access_token");
            when(jwtTokenProvider.generateRefreshToken(anyLong()))
                    .thenReturn("refresh_token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400000L);

            AuthResponse response = authService.register(request);

            assertThat(response.getAccessToken()).isEqualTo("access_token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
            assertThat(response.getRole()).isEqualTo("USER");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_DuplicateEmail() {
            AuthRequest.Register request = new AuthRequest.Register();
            request.setEmail("existing@test.com");

            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Email already registered");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Test
        @DisplayName("Should login successfully with correct credentials")
        void login_Success() {
            AuthRequest.Login request = new AuthRequest.Login();
            request.setEmail("test@test.com");
            request.setPassword("password123");

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                    .thenReturn("access_token");
            when(jwtTokenProvider.generateRefreshToken(anyLong()))
                    .thenReturn("refresh_token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400000L);

            AuthResponse response = authService.login(request);

            assertThat(response.getAccessToken()).isNotNull();
            assertThat(response.getRole()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Should throw exception with wrong password")
        void login_WrongPassword() {
            AuthRequest.Login request = new AuthRequest.Login();
            request.setEmail("test@test.com");
            request.setPassword("wrong");

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void login_UserNotFound() {
            AuthRequest.Login request = new AuthRequest.Login();
            request.setEmail("nobody@test.com");
            request.setPassword("password");

            when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should throw exception when account is disabled")
        void login_DisabledAccount() {
            testUser.setStatus(UserStatus.DISABLED);

            AuthRequest.Login request = new AuthRequest.Login();
            request.setEmail("test@test.com");
            request.setPassword("password123");

            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("disabled");
        }
    }

    @Nested
    @DisplayName("Logout")
    class Logout {

        @Test
        @DisplayName("Should blacklist token on logout")
        void logout_Success() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(86400000L);

            authService.logout("some_token");

            verify(valueOperations).set(
                    eq("token:blacklist:some_token"),
                    eq("1"),
                    eq(86400000L),
                    eq(java.util.concurrent.TimeUnit.MILLISECONDS)
            );
        }
    }
}