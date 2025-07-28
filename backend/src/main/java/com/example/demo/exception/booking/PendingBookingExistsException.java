package com.example.demo.exception.booking;

import lombok.Getter;

@Getter
public class PendingBookingExistsException extends RuntimeException {

    private final Integer existingBookingId;

    public PendingBookingExistsException(String message, Integer existingBookingId) {
        super(message);
        this.existingBookingId = existingBookingId;
    }
}