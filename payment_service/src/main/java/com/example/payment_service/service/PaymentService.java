package com.example.payment_service.service;

import com.example.payment_service.BalanceException;
import com.example.payment_service.model.BalanceResponse;
import com.example.payment_service.model.PaymentRequest;
import com.example.payment_service.model.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PaymentService {
    private final AtomicReference<BigDecimal> balance;

    public PaymentService(@Value("${payment.balance}") String initialBalance) {
        this.balance = new AtomicReference<>(new BigDecimal(initialBalance));
    }

    public Mono<BalanceResponse> getBalance(){
        return Mono.just(new BalanceResponse(balance.get()));
    }


    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
      return Mono.fromCallable(()->{
          BigDecimal currentBalance = balance.get();
          BigDecimal paymentAmount = request.getAmount();

          if(currentBalance.compareTo(paymentAmount) < 0){
              throw  new BalanceException( "Отрицательный баланс: " + currentBalance + " < " + paymentAmount);
          }

          BigDecimal newBalance = currentBalance.subtract(paymentAmount);
          balance.set(newBalance);

          PaymentResponse response = PaymentResponse.builder()
                  .paymentId(UUID.randomUUID().toString())
                  .orderId(request.getOrderId())
                  .amount(paymentAmount)
                  .status("SUCCESS")
                  .balanceAfterPayment(newBalance)
                  .build();
          return response;
      });
    }
}
