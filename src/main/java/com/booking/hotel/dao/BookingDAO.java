package com.booking.hotel.dao;

import com.booking.hotel.model.Booking;

import java.sql.SQLException;
import java.util.List;

public interface BookingDAO {

    boolean create(Booking booking) throws SQLException;

    Booking findById(long bookingId) throws SQLException;

    List<Booking> findByUser(long userId) throws SQLException;

    boolean updateStatus(long bookingId, String status) throws SQLException;

    boolean delete(long bookingId) throws SQLException;
}
