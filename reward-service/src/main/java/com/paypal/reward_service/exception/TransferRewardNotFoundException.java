package com.paypal.reward_service.exception;

public class TransferRewardNotFoundException extends RuntimeException {

    public TransferRewardNotFoundException(String message) {
        super(message);
    }
}
