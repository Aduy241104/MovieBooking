package com.example.demo.exception;

public class UnpaidBookingException extends RuntimeException {
    public UnpaidBookingException(String message) {
        super(message);
    }
}
