package com.booking.hotel.dao;

import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Location;
import com.booking.hotel.util.JdbcUtil;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class HotelDAOImpl implements HotelDAO {

    // Records each hotel-table action. Messages are plain strings.
    private static final Logger logger = Logger.getLogger(HotelDAOImpl.class.getName());

    private static final String SQL_INSERT_HOTEL =
            "INSERT INTO hotel (location_id, name, description, address, city, state, "
                    + "country, star_rating, amenities, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM hotel WHERE hotel_id = ?";

    private static final String SQL_FIND_BY_CITY =
            "SELECT * FROM hotel WHERE city = ?";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM hotel";

    private static final String SQL_UPDATE =
            "UPDATE hotel SET location_id = ?, name = ?, description = ?, address = ?, "
                    + "city = ?, state = ?, country = ?, star_rating = ?, amenities = ?, status = ? "
                    + "WHERE hotel_id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM hotel WHERE hotel_id = ?";

    // Inserts one row into the hotel table and stores the new hotel_id on the Hotel object.
    @Override
    public boolean create(Hotel hotel) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_HOTEL, Statement.RETURN_GENERATED_KEYS)) {

            setHotelColumns(statement, hotel);

            logger.fine("Inserting hotel with name " + hotel.getName());
            int rows = statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    hotel.setHotelId(keys.getLong(1));
                }
            }

            if (rows > 0) {
                logger.info("Inserted hotel id=" + hotel.getHotelId());
            }
            return rows > 0;
        }
    }

    // Selects the one hotel row whose hotel_id matches the given id.
    @Override
    public Hotel findById(long hotelId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {

            statement.setLong(1, hotelId);

            logger.fine("Selecting hotel with id " + hotelId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                logger.warning("No hotel found for id " + hotelId);
                return null;
            }
        }
    }

    // Selects every hotel row whose city matches the given city.
    @Override
    public List<Hotel> findByCity(String city) throws SQLException {
        List<Hotel> hotels = new ArrayList<>();

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_CITY)) {

            statement.setString(1, city);

            logger.fine("Selecting hotels in city " + city);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    hotels.add(mapRow(resultSet));
                }
            }
        }

        if (hotels.isEmpty()) {
            logger.warning("No hotels found in city " + city);
        }
        return hotels;
    }

    // Selects every row from the hotel table.
    @Override
    public List<Hotel> findAll() throws SQLException {
        List<Hotel> hotels = new ArrayList<>();

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_ALL)) {

            logger.fine("Selecting all hotels");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    hotels.add(mapRow(resultSet));
                }
            }
        }

        if (hotels.isEmpty()) {
            logger.warning("No hotels found");
        }
        return hotels;
    }

    // Updates location, name, description, address, city, state, country, rating, amenities, and status for this hotel_id.
    @Override
    public boolean update(Hotel hotel) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {

            setHotelColumns(statement, hotel);
            statement.setLong(11, hotel.getHotelId());

            logger.fine("Updating hotel with id " + hotel.getHotelId());
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Updated hotel id=" + hotel.getHotelId());
            }
            return rows > 0;
        }
    }

    // Deletes the hotel row whose hotel_id matches the given id.
    @Override
    public boolean delete(long hotelId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setLong(1, hotelId);
            logger.fine("Deleting hotel with id " + hotelId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted hotel id=" + hotelId);
            }
            return rows > 0;
        }
    }

    // Copies one hotel-table row into a Hotel. location_id is stored on a Location object.
    private Hotel mapRow(ResultSet resultSet) throws SQLException {
        Hotel hotel = new Hotel();
        hotel.setHotelId(resultSet.getLong("hotel_id"));

        long locationId = resultSet.getLong("location_id");
        if (!resultSet.wasNull()) {
            Location location = new Location();
            location.setLocationId(locationId);
            hotel.setLocation(location);
        }

        hotel.setName(resultSet.getString("name"));
        hotel.setDescription(resultSet.getString("description"));
        hotel.setAddress(resultSet.getString("address"));
        hotel.setCity(resultSet.getString("city"));
        hotel.setState(resultSet.getString("state"));
        hotel.setCountry(resultSet.getString("country"));
        hotel.setStarRating(resultSet.getBigDecimal("star_rating"));
        hotel.setAmenities(resultSet.getString("amenities"));
        hotel.setStatus(resultSet.getString("status"));
        return hotel;
    }

    private void setHotelColumns(PreparedStatement statement, Hotel hotel) throws SQLException {
        if (hotel.getLocation() == null) {
            statement.setNull(1, Types.BIGINT);
        } else {
            statement.setLong(1, hotel.getLocation().getLocationId());
        }
        statement.setString(2, hotel.getName());
        statement.setString(3, hotel.getDescription());
        statement.setString(4, hotel.getAddress());
        statement.setString(5, hotel.getCity());
        statement.setString(6, hotel.getState());
        statement.setString(7, hotel.getCountry());
        statement.setBigDecimal(8, hotel.getStarRating());
        statement.setString(9, hotel.getAmenities());
        statement.setString(10, hotel.getStatus());
    }
}
