package com.ecommerce.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequest {

    @Data
    public static class Register {
        @NotBlank(message = "Username is required")
        @Size(min = 2, max = 32)
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 64, message = "Password must be 6-64 characters")
        private String password;

        private String phone;
    }

    @Data
    public static class Login {
        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }
}
