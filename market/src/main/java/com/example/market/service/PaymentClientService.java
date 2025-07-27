package com.example.market.service;

import com.example.market.client.PaymentApi;
import com.example.market.model.BalanceResponse;
import com.example.market.model.PaymentRequest;
import com.example.market.model.PaymentResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class PaymentClientService {

    private final PaymentApi paymentApi;

    public PaymentClientService(PaymentApi paymentApi) {
        this.paymentApi = paymentApi;
    }

    public Mono<BalanceResponse> getBalance() {
        return paymentApi.getBalance();
    }

    public Mono<PaymentResponse> processPayment(String orderId, BigDecimal amount, String paymentMethod) {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setAmount(String.valueOf(amount));
        request.setPaymentMethod(paymentMethod);

        return paymentApi.processPayment(request);
    }
}