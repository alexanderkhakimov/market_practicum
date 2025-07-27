package com.example.payment_service.controller;

import com.example.payment_service.BalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleInsufficientBalanceException(BalanceException ex) {
        return Mono.just(new ErrorResponse(ex.getMessage()));
    }

    public record ErrorResponse(String message) {}
}