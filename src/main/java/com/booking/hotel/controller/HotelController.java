package com.booking.hotel.controller;

import com.booking.hotel.exception.HotelNotFoundException;
import com.booking.hotel.exception.InvalidBookingException;
import com.booking.hotel.exception.UnauthorizedException;
import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Roles;
import com.booking.hotel.service.HotelService;
import com.booking.hotel.util.SessionContext;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Sits between Main (console input and output) and HotelService (business rules).
// Adding a hotel is an admin action. Searching and viewing require a login.
public class HotelController {

    private static final Logger logger = Logger.getLogger(HotelController.class.getName());

    private final HotelService hotelService;
    private final SessionContext sessionContext;

    public HotelController(HotelService hotelService, SessionContext sessionContext) {
        this.hotelService = hotelService;
        this.sessionContext = sessionContext;
    }

    // Only an admin can save a new hotel.
    public Hotel addHotel(String name, String description, String address, String city,
                          String state, String country, BigDecimal starRating, String amenities)
            throws SQLException {
        try {
            sessionContext.requireRole(Roles.ADMIN);
            return hotelService.addHotel(name, description, address, city, state, country, starRating, amenities);
        } catch (UnauthorizedException | InvalidBookingException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    // Any logged-in user can search hotels by city.
    public List<Hotel> searchByCity(String city) throws SQLException {
        try {
            sessionContext.requireLogin();
            return hotelService.searchHotelsByCity(city);
        } catch (UnauthorizedException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    // Any logged-in user can open one hotel.
    public Hotel getHotel(long hotelId) throws SQLException {
        try {
            sessionContext.requireLogin();
            return hotelService.getHotelById(hotelId);
        } catch (UnauthorizedException | HotelNotFoundException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }
}
