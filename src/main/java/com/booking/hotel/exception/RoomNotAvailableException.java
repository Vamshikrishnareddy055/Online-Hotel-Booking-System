package com.booking.hotel.exception;

// Thrown when a customer tries to book a room that exists but is already booked
public class RoomNotAvailableException extends RuntimeException {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}