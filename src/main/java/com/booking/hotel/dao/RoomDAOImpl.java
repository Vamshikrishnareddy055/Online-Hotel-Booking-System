package com.booking.hotel.dao;

import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Room;
import com.booking.hotel.util.JdbcUtil;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoomDAOImpl implements RoomDAO {

    // Records each room-table action. Messages are plain strings.
    private static final Logger logger = Logger.getLogger(RoomDAOImpl.class.getName());

    private static final String SQL_INSERT_ROOM =
            "INSERT INTO room (hotel_id, room_number, room_type, capacity, base_price, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM room WHERE room_id = ?";

    private static final String SQL_FIND_BY_HOTEL =
            "SELECT * FROM room WHERE hotel_id = ?";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE room SET status = ? WHERE room_id = ?";

    private static final String SQL_UPDATE =
            "UPDATE room SET hotel_id = ?, room_number = ?, room_type = ?, "
                    + "capacity = ?, base_price = ?, status = ? WHERE room_id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM room WHERE room_id = ?";

    // Inserts one row into the room table and stores the new room_id on the Room object.
    @Override
    public boolean create(Room room) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_ROOM, Statement.RETURN_GENERATED_KEYS)) {

            setRoomColumns(statement, room);

            logger.fine("Inserting room with number " + room.getRoomNumber());
            int rows = statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    room.setRoomId(keys.getLong(1));
                }
            }

            if (rows > 0) {
                logger.info("Inserted room id=" + room.getRoomId());
            }
            return rows > 0;
        }
    }

    // Selects the one room row whose room_id matches the given id.
    @Override
    public Room findById(long roomId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {

            statement.setLong(1, roomId);

            logger.fine("Selecting room with id " + roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                logger.warning("No room found for id " + roomId);
                return null;
            }
        }
    }

    // Selects every room row that belongs to the given hotel_id.
    @Override
    public List<Room> findByHotel(long hotelId) throws SQLException {
        List<Room> rooms = new ArrayList<>();

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_HOTEL)) {

            statement.setLong(1, hotelId);

            logger.fine("Selecting rooms for hotel id " + hotelId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rooms.add(mapRow(resultSet));
                }
            }
        }

        if (rooms.isEmpty()) {
            logger.warning("No rooms found for hotel id " + hotelId);
        }
        return rooms;
    }

    // Updates only the status column for this room_id.
    @Override
    public boolean updateStatus(long roomId, String status) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_STATUS)) {

            statement.setString(1, status);
            statement.setLong(2, roomId);

            logger.fine("Updating status for room id " + roomId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Updated room id=" + roomId);
            }
            return rows > 0;
        }
    }

    // Updates hotel, room number, type, capacity, price, and status for this room_id.
    @Override
    public boolean update(Room room) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {

            setRoomColumns(statement, room);
            statement.setLong(7, room.getRoomId());

            logger.fine("Updating room with id " + room.getRoomId());
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Updated room id=" + room.getRoomId());
            }
            return rows > 0;
        }
    }

    // Deletes the room row whose room_id matches the given id.
    @Override
    public boolean delete(long roomId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setLong(1, roomId);
            logger.fine("Deleting room with id " + roomId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted room id=" + roomId);
            }
            return rows > 0;
        }
    }

    // Copies one room-table row into a Room. hotel_id is stored on a Hotel object.
    private Room mapRow(ResultSet resultSet) throws SQLException {
        Room room = new Room();
        room.setRoomId(resultSet.getLong("room_id"));

        Hotel hotel = new Hotel();
        hotel.setHotelId(resultSet.getLong("hotel_id"));
        room.setHotel(hotel);

        room.setRoomNumber(resultSet.getString("room_number"));
        room.setRoomType(resultSet.getString("room_type"));
        room.setCapacity(resultSet.getInt("capacity"));
        room.setBasePrice(resultSet.getBigDecimal("base_price"));
        room.setStatus(resultSet.getString("status"));
        return room;
    }

    private void setRoomColumns(PreparedStatement statement, Room room) throws SQLException {
        statement.setLong(1, room.getHotel().getHotelId());
        statement.setString(2, room.getRoomNumber());
        statement.setString(3, room.getRoomType());
        statement.setInt(4, room.getCapacity());
        statement.setBigDecimal(5, room.getBasePrice());
        statement.setString(6, room.getStatus());
    }
}
