package com.paypal.transaction_service.exception;

public class DepositProcessingException extends RuntimeException {

    public DepositProcessingException(String message) {
        super(message);
    }
}
