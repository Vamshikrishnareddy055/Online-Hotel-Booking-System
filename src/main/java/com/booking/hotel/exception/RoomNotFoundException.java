package com.booking.hotel.exception;

// Thrown when a lookup by room ID finds no matching room in the database
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message);
    }
}