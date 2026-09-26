package com.booking.hotel.dao;

import com.booking.hotel.model.Booking;
import com.booking.hotel.model.Payment;
import com.booking.hotel.util.JdbcUtil;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

public class PaymentDAOImpl implements PaymentDAO {

    // Records each payment-table action. Messages are plain strings.
    private static final Logger logger = Logger.getLogger(PaymentDAOImpl.class.getName());

    private static final String SQL_INSERT_PAYMENT =
            "INSERT INTO payment (booking_id, amount, payment_method, payment_status, transaction_ref, paid_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM payment WHERE payment_id = ?";

    private static final String SQL_FIND_BY_BOOKING =
            "SELECT * FROM payment WHERE booking_id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM payment WHERE payment_id = ?";

    // Inserts one row into the payment table and stores the new payment_id on the Payment object.
    @Override
    public boolean create(Payment payment) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PAYMENT, Statement.RETURN_GENERATED_KEYS)) {

            setPaymentColumns(statement, payment);

            logger.fine("Inserting payment for booking id " + payment.getBooking().getBookingId());
            int rows = statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setPaymentId(keys.getLong(1));
                }
            }

            if (rows > 0) {
                logger.info("Inserted payment id=" + payment.getPaymentId());
            }
            return rows > 0;
        }
    }

    // Selects the one payment row whose payment_id matches the given id.
    @Override
    public Payment findById(long paymentId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {

            statement.setLong(1, paymentId);

            logger.fine("Selecting payment with id " + paymentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                logger.warning("No payment found for id " + paymentId);
                return null;
            }
        }
    }

    // Selects the payment row that belongs to the given booking_id.
    @Override
    public Payment findByBooking(long bookingId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_BOOKING)) {

            statement.setLong(1, bookingId);

            logger.fine("Selecting payment for booking id " + bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                logger.warning("No payment found for booking id " + bookingId);
                return null;
            }
        }
    }

    // Deletes the payment row whose payment_id matches the given id.
    @Override
    public boolean delete(long paymentId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setLong(1, paymentId);
            logger.fine("Deleting payment with id " + paymentId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted payment id=" + paymentId);
            }
            return rows > 0;
        }
    }

    // Copies one payment-table row into a Payment. booking_id is stored on a Booking object.
    private Payment mapRow(ResultSet resultSet) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(resultSet.getLong("payment_id"));

        Booking booking = new Booking();
        booking.setBookingId(resultSet.getLong("booking_id"));
        payment.setBooking(booking);

        payment.setAmount(resultSet.getBigDecimal("amount"));
        payment.setPaymentMethod(resultSet.getString("payment_method"));
        payment.setPaymentStatus(resultSet.getString("payment_status"));
        payment.setTransactionRef(resultSet.getString("transaction_ref"));

        Timestamp paidAt = resultSet.getTimestamp("paid_at");
        if (resultSet.wasNull()) {
            payment.setPaidAt(null);
        } else {
            payment.setPaidAt(paidAt.toLocalDateTime());
        }
        return payment;
    }

    private void setPaymentColumns(PreparedStatement statement, Payment payment) throws SQLException {
        statement.setLong(1, payment.getBooking().getBookingId());
        statement.setBigDecimal(2, payment.getAmount());
        statement.setString(3, payment.getPaymentMethod());
        statement.setString(4, payment.getPaymentStatus());
        statement.setString(5, payment.getTransactionRef());
        if (payment.getPaidAt() == null) {
            statement.setNull(6, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(6, Timestamp.valueOf(payment.getPaidAt()));
        }
    }
}
