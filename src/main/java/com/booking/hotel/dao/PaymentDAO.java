package com.booking.hotel.dao;

import com.booking.hotel.model.Payment;

import java.sql.SQLException;

public interface PaymentDAO {

    boolean create(Payment payment) throws SQLException;

    Payment findById(long paymentId) throws SQLException;

    Payment findByBooking(long bookingId) throws SQLException;

    boolean delete(long paymentId) throws SQLException;
}
