package com.paypal.transaction_service.exception;

public class DepositNotFoundException extends RuntimeException {

    public DepositNotFoundException(String message) {
        super(message);
    }
}
