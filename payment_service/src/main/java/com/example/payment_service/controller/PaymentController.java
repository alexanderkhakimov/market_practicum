package com.example.payment_service.controller;

import com.example.payment_service.model.BalanceResponse;
import com.example.payment_service.model.PaymentRequest;
import com.example.payment_service.model.PaymentResponse;
import com.example.payment_service.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('SCOPE_payment:read')")
    public Mono<BalanceResponse> getBalance() {
        return paymentService.getBalance();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_payment:write')")
    public Mono<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(request);
    }
}