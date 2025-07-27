package com.example.payment_service.model;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record PaymentRequest(
        String orderId,
        BigDecimal amount,
        String paymentMethod
) {
}
