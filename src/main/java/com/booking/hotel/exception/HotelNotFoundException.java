package com.booking.hotel.exception;

// Thrown when a lookup by hotel ID finds no matching hotel in the database
public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException(String message) {
        super(message);
    }
}