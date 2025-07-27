package com.example.payment_service.model;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record PaymentResponse(
        String paymentId,
        String orderId,
        String status,
        BigDecimal amount,
        BigDecimal balanceAfterPayment
) {
}
