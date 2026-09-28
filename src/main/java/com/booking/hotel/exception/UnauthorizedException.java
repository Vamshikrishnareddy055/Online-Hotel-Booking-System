package com.booking.hotel.exception;

// Thrown when a logged-in user tries an action their role is not allowed to perform
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
