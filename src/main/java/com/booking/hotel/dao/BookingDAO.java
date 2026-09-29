package com.booking.hotel.dao;

import com.booking.hotel.model.Booking;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface BookingDAO {

    boolean create(Booking booking) throws SQLException;

    Booking findById(long bookingId) throws SQLException;

    List<Booking> findByUser(long userId) throws SQLException;

    List<Booking> findAll() throws SQLException;

    // Confirmed bookings for this room whose nights overlap the given stay.
    List<Booking> findOverlapping(long roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException;

    boolean updateStatus(long bookingId, String status) throws SQLException;

    boolean delete(long bookingId) throws SQLException;
}
