package com.delivery.habsida.exception;

public class MinOrderPriceNotReachedException extends RuntimeException {
    public MinOrderPriceNotReachedException(String message) {
        super(message);
    }
}
