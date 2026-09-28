package com.booking.hotel.exception;

// Thrown when login fails because the email or password is wrong, or the account is inactive
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
