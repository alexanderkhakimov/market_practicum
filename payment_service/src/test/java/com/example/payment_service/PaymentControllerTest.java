package com.example.payment_service;

import com.example.payment_service.controller.PaymentController;
import com.example.payment_service.model.BalanceResponse;
import com.example.payment_service.model.PaymentRequest;
import com.example.payment_service.model.PaymentResponse;
import com.example.payment_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PaymentService paymentService;

    @Test
    void testGetBalance() {
        BalanceResponse response = new BalanceResponse(new BigDecimal("1000.0"));

        when(paymentService.getBalance()).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/payments/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .isEqualTo(response);
    }

    @Test
    void testProcessPaymentSuccess() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("order123");
        request.setAmount(new BigDecimal("100.0"));
        request.setPaymentMethod("CARD");

        PaymentResponse response =PaymentResponse.builder()
                .paymentId("payment123")
                .orderId("order123")
                .status("SUCCESS")
                .amount(new BigDecimal("100.0"))
                .balanceAfterPayment(new BigDecimal("900.0"))
                .build();

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .isEqualTo(response);
    }

    @Test
    void testProcessPaymentInsufficientBalance() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("order123");
        request.setAmount(new BigDecimal("2000.0"));
        request.setPaymentMethod("CARD");

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(Mono.error(new BalanceException("Отрицательный баланс: 1000.0 < 2000.0")));

        webTestClient.post()
                .uri("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Отрицательный баланс: 1000.0 < 2000.0");
    }
}