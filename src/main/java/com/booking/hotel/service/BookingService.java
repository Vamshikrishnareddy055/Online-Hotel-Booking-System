package com.booking.hotel.service;

import com.booking.hotel.dao.BookingDAO;
import com.booking.hotel.dao.RoomDAO;
import com.booking.hotel.exception.BookingNotFoundException;
import com.booking.hotel.exception.InvalidBookingException;
import com.booking.hotel.exception.RoomNotAvailableException;
import com.booking.hotel.exception.RoomNotFoundException;
import com.booking.hotel.model.Booking;
import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Room;
import com.booking.hotel.model.User;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

public class BookingService {

    private static final Logger logger = Logger.getLogger(BookingService.class.getName());

    private final RoomDAO roomDAO;
    private final BookingDAO bookingDAO;

    // Receives the DAOs from outside so this class can be tested with fake DAOs later.
    public BookingService(RoomDAO roomDAO, BookingDAO bookingDAO) {
        this.roomDAO = roomDAO;
        this.bookingDAO = bookingDAO;
    }

    // Books a room when the dates are valid and no other guest already has those nights.
    public Booking createBooking(long userId, long hotelId, long roomId, LocalDate checkIn,
                                 LocalDate checkOut, int adults, int children, String paymentOption)
            throws SQLException {
        // A booking can only refer to a room that exists.
        Room room = roomDAO.findById(roomId);
        if (room == null) {
            logger.warning("No room found with id " + roomId);
            throw new RoomNotFoundException("No room found with id " + roomId);
        }

        // Check-out must be a later date than check-in, so the stay is at least one night.
        if (checkOut == null || checkIn == null || !checkOut.isAfter(checkIn)) {
            logger.warning("Check-out is not after check-in for room id " + roomId);
            throw new InvalidBookingException("Check-out date must be after check-in date");
        }

        // The same room can be booked by someone else on different dates, but not on the same nights.
        if (!bookingDAO.findOverlapping(roomId, checkIn, checkOut).isEmpty()) {
            logger.warning("Room is already booked for those dates, room id " + roomId);
            throw new RoomNotAvailableException("This room is already booked for those dates");
        }

        // Price is the nightly rate multiplied by the number of nights.
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal totalAmount = room.getBasePrice().multiply(BigDecimal.valueOf(nights));

        User user = new User();
        user.setUserId(userId);
        Hotel hotel = new Hotel();
        hotel.setHotelId(hotelId);

        Booking booking = new Booking(
                0,
                user,
                hotel,
                room,
                checkIn,
                checkOut,
                adults,
                children,
                totalAmount,
                paymentOption,
                "CONFIRMED"
        );

        // Saving the booking stores the generated booking id on the object.
        boolean created = bookingDAO.create(booking);
        if (created) {
            logger.info("Created booking id=" + booking.getBookingId());
        }
        return booking;
    }

    // Cancels a confirmed booking. Other dates for this room stay bookable.
    public void cancelBooking(long bookingId) throws SQLException {
        // The booking must exist before its status can change.
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            logger.warning("No booking found with id " + bookingId);
            throw new BookingNotFoundException("No booking found with id " + bookingId);
        }

        // Cancelling twice is not allowed.
        if ("CANCELLED".equals(booking.getBookingStatus())) {
            logger.warning("Booking is already cancelled with id " + bookingId);
            throw new InvalidBookingException("Booking is already cancelled");
        }

        bookingDAO.updateStatus(bookingId, "CANCELLED");
        logger.info("Cancelled booking id=" + bookingId);
    }

    // Loads one booking by id. Throws BookingNotFoundException when that id is not in the database.
    public Booking getBookingById(long bookingId) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            logger.warning("No booking found with id " + bookingId);
            throw new BookingNotFoundException("No booking found with id " + bookingId);
        }
        return booking;
    }

    // Returns the bookings saved for this user. The list is empty when they have none.
    public List<Booking> getBookingsByUser(long userId) throws SQLException {
        return bookingDAO.findByUser(userId);
    }

    // Returns every booking in the table, which the admin view lists.
    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.findAll();
    }
}
