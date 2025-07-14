package com.example.payment_service.controller;

import com.example.payment_service.model.BalanceResponse;
import com.example.payment_service.model.PaymentRequest;
import com.example.payment_service.model.PaymentResponse;
import com.example.payment_service.service.PaymentService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")

public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/balance")
    public Mono<BalanceResponse> getBalance() {
        return paymentService.getBalance();
    }

    @PostMapping
    public Mono<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(request);
    }
}
