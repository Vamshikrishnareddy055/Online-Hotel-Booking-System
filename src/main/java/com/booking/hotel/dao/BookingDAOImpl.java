package com.booking.hotel.dao;

import com.booking.hotel.model.Booking;
import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Room;
import com.booking.hotel.model.User;
import com.booking.hotel.util.JdbcUtil;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookingDAOImpl implements BookingDAO {

    // Records each booking-table action. Messages are plain strings.
    private static final Logger logger = Logger.getLogger(BookingDAOImpl.class.getName());

    private static final String SQL_INSERT_BOOKING =
            "INSERT INTO booking (user_id, hotel_id, room_id, check_in_date, check_out_date, "
                    + "guests_adults, guests_children, total_amount, payment_option, booking_status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM booking WHERE booking_id = ?";

    private static final String SQL_FIND_BY_USER =
            "SELECT * FROM booking WHERE user_id = ?";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE booking SET booking_status = ? WHERE booking_id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM booking WHERE booking_id = ?";

    // Inserts one row into the booking table and stores the new booking_id on the Booking object.
    @Override
    public boolean create(Booking booking) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_BOOKING, Statement.RETURN_GENERATED_KEYS)) {

            setBookingColumns(statement, booking);

            logger.fine("Inserting booking for user id " + booking.getUser().getUserId());
            int rows = statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    booking.setBookingId(keys.getLong(1));
                }
            }

            if (rows > 0) {
                logger.info("Inserted booking id=" + booking.getBookingId());
            }
            return rows > 0;
        }
    }

    // Selects the one booking row whose booking_id matches the given id.
    @Override
    public Booking findById(long bookingId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {

            statement.setLong(1, bookingId);

            logger.fine("Selecting booking with id " + bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                logger.warning("No booking found for id " + bookingId);
                return null;
            }
        }
    }

    // Selects every booking row that belongs to the given user_id.
    @Override
    public List<Booking> findByUser(long userId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_USER)) {

            statement.setLong(1, userId);

            logger.fine("Selecting bookings for user id " + userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(mapRow(resultSet));
                }
            }
        }

        if (bookings.isEmpty()) {
            logger.warning("No bookings found for user id " + userId);
        }
        return bookings;
    }

    // Updates only the booking_status column for this booking_id.
    @Override
    public boolean updateStatus(long bookingId, String status) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_STATUS)) {

            statement.setString(1, status);
            statement.setLong(2, bookingId);

            logger.fine("Updating status for booking id " + bookingId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Updated booking id=" + bookingId);
            }
            return rows > 0;
        }
    }

    // Deletes the booking row whose booking_id matches the given id.
    @Override
    public boolean delete(long bookingId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setLong(1, bookingId);
            logger.fine("Deleting booking with id " + bookingId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted booking id=" + bookingId);
            }
            return rows > 0;
        }
    }

    // Copies one booking-table row into a Booking. Foreign keys are stored as ids on User, Hotel, and Room.
    private Booking mapRow(ResultSet resultSet) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(resultSet.getLong("booking_id"));

        User user = new User();
        user.setUserId(resultSet.getLong("user_id"));
        booking.setUser(user);

        Hotel hotel = new Hotel();
        hotel.setHotelId(resultSet.getLong("hotel_id"));
        booking.setHotel(hotel);

        Room room = new Room();
        room.setRoomId(resultSet.getLong("room_id"));
        booking.setRoom(room);

        booking.setCheckInDate(resultSet.getDate("check_in_date").toLocalDate());
        booking.setCheckOutDate(resultSet.getDate("check_out_date").toLocalDate());
        booking.setGuestsAdults(resultSet.getInt("guests_adults"));
        booking.setGuestsChildren(resultSet.getInt("guests_children"));
        booking.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        booking.setPaymentOption(resultSet.getString("payment_option"));
        booking.setBookingStatus(resultSet.getString("booking_status"));
        return booking;
    }

    private void setBookingColumns(PreparedStatement statement, Booking booking) throws SQLException {
        statement.setLong(1, booking.getUser().getUserId());
        statement.setLong(2, booking.getHotel().getHotelId());
        statement.setLong(3, booking.getRoom().getRoomId());
        statement.setDate(4, Date.valueOf(booking.getCheckInDate()));
        statement.setDate(5, Date.valueOf(booking.getCheckOutDate()));
        statement.setInt(6, booking.getGuestsAdults());
        statement.setInt(7, booking.getGuestsChildren());
        statement.setBigDecimal(8, booking.getTotalAmount());
        statement.setString(9, booking.getPaymentOption());
        statement.setString(10, booking.getBookingStatus());
    }
}
