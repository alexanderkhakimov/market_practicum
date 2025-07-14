package com.example.payment_service.model;

import java.math.BigDecimal;

public record PaymentRequest(
        String orderId,
        BigDecimal amount,
        String paymentMethod
) {
}
