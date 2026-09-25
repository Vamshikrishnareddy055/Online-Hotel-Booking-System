package com.booking.hotel.exception;

// Thrown when a lookup by booking ID finds no matching booking in the database
public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}