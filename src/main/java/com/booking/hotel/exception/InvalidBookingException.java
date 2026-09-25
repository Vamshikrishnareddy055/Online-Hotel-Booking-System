package com.booking.hotel.exception;

// Thrown when booking details fail a business rule check,
// e.g. check-out date is not after check-in date
public class InvalidBookingException extends RuntimeException {
    public InvalidBookingException(String message) {
        super(message);
    }
}