package com.booking.hotel.controller;

import com.booking.hotel.exception.BookingNotFoundException;
import com.booking.hotel.exception.InvalidBookingException;
import com.booking.hotel.exception.RoomNotAvailableException;
import com.booking.hotel.exception.RoomNotFoundException;
import com.booking.hotel.exception.UnauthorizedException;
import com.booking.hotel.model.Booking;
import com.booking.hotel.model.Roles;
import com.booking.hotel.model.User;
import com.booking.hotel.service.BookingService;
import com.booking.hotel.util.SessionContext;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Sits between Main (console input and output) and BookingService (business rules).
// Booking is for customers. Cancelling depends on who owns the booking.
public class BookingController {

    private static final Logger logger = Logger.getLogger(BookingController.class.getName());

    private final BookingService bookingService;
    private final SessionContext sessionContext;

    public BookingController(BookingService bookingService, SessionContext sessionContext) {
        this.bookingService = bookingService;
        this.sessionContext = sessionContext;
    }

    // The booking belongs to the person who is logged in.
    public Booking bookRoom(long hotelId, long roomId, LocalDate checkIn,
                            LocalDate checkOut, int adults, int children, String paymentOption)
            throws SQLException {
        try {
            User current = sessionContext.requireLogin();
            if (!Roles.CUSTOMER.equals(current.getRole()) && !Roles.ADMIN.equals(current.getRole())) {
                throw new UnauthorizedException("Access denied");
            }
            long userId = current.getUserId();
            return bookingService.createBooking(
                    userId, hotelId, roomId, checkIn, checkOut, adults, children, paymentOption);
        } catch (UnauthorizedException | RoomNotFoundException | RoomNotAvailableException
                | InvalidBookingException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    // A customer can cancel only a booking they made. An admin can cancel any booking.
    public void cancelBooking(long bookingId) throws SQLException {
        try {
            User current = sessionContext.requireLogin();
            Booking booking = bookingService.getBookingById(bookingId);
            if (Roles.CUSTOMER.equals(current.getRole())
                    && booking.getUser().getUserId() != current.getUserId()) {
                throw new UnauthorizedException("You can only cancel your own bookings");
            }
            bookingService.cancelBooking(bookingId);
        } catch (UnauthorizedException | BookingNotFoundException | InvalidBookingException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    // Returns the bookings that belong to the logged-in customer.
    public List<Booking> viewMyBookings() throws SQLException {
        try {
            sessionContext.requireRole(Roles.CUSTOMER);
            long userId = sessionContext.getCurrentUser().getUserId();
            return bookingService.getBookingsByUser(userId);
        } catch (UnauthorizedException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    // Returns every booking, which only an admin can open.
    public List<Booking> viewAllBookings() throws SQLException {
        try {
            sessionContext.requireRole(Roles.ADMIN);
            return bookingService.getAllBookings();
        } catch (UnauthorizedException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }
}
