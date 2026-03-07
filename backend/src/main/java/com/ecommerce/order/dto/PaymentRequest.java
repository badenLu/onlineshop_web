package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "Payment type is required")
    private String paymentType; // CREDIT_CARD, PAYPAL, BANK_TRANSFER
}