package com.booking.hotel.exception;

// Thrown when registration uses an email that is already stored for another user
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
