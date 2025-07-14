package com.example.payment_service;

public class BalanceException extends RuntimeException {
    public BalanceException(String message) {
        super(message);
    }
}
