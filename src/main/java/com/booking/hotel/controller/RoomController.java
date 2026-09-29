package com.booking.hotel.controller;

import com.booking.hotel.exception.HotelNotFoundException;
import com.booking.hotel.exception.InvalidBookingException;
import com.booking.hotel.exception.UnauthorizedException;
import com.booking.hotel.model.Roles;
import com.booking.hotel.model.Room;
import com.booking.hotel.service.RoomService;
import com.booking.hotel.util.SessionContext;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Sits between Main (console input and output) and RoomService (business rules).
// Adding a room is an admin action. Listing rooms requires a login.
public class RoomController {

    private static final Logger logger = Logger.getLogger(RoomController.class.getName());

    private final RoomService roomService;
    private final SessionContext sessionContext;

    public RoomController(RoomService roomService, SessionContext sessionContext) {
        this.roomService = roomService;
        this.sessionContext = sessionContext;
    }

    // Only an admin can save a new room.
    public Room addRoom(long hotelId, String roomNumber, String roomType, int capacity, BigDecimal basePrice)
            throws SQLException {
        try {
            sessionContext.requireRole(Roles.ADMIN);
            return roomService.addRoom(hotelId, roomNumber, roomType, capacity, basePrice);
        } catch (UnauthorizedException | HotelNotFoundException | InvalidBookingException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    // Any logged-in user can list the rooms in a hotel.
    public List<Room> getRoomsByHotel(long hotelId) throws SQLException {
        try {
            sessionContext.requireLogin();
            return roomService.getRoomsByHotel(hotelId);
        } catch (UnauthorizedException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }
}
