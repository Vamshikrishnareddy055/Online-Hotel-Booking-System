package com.booking.hotel.exception;

// Thrown when a lookup by user ID finds no matching user in the database
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}