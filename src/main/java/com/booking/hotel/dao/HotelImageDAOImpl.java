package com.booking.hotel.dao;

import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.HotelImage;
import com.booking.hotel.util.JdbcUtil;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HotelImageDAOImpl implements HotelImageDAO {

    // Records each hotel_image-table action. Messages are plain strings.
    private static final Logger logger = Logger.getLogger(HotelImageDAOImpl.class.getName());

    private static final String SQL_INSERT_HOTEL_IMAGE =
            "INSERT INTO hotel_image (hotel_id, image_url, caption, is_primary, display_order) "
                    + "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM hotel_image WHERE image_id = ?";

    private static final String SQL_FIND_BY_HOTEL =
            "SELECT * FROM hotel_image WHERE hotel_id = ? ORDER BY display_order";

    private static final String SQL_DELETE =
            "DELETE FROM hotel_image WHERE image_id = ?";

    // Inserts one row into the hotel_image table and stores the new image_id on the HotelImage object.
    @Override
    public boolean create(HotelImage image) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_HOTEL_IMAGE, Statement.RETURN_GENERATED_KEYS)) {

            setImageColumns(statement, image);

            logger.fine("Inserting hotel image for hotel id " + image.getHotel().getHotelId());
            int rows = statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    image.setImageId(keys.getLong(1));
                }
            }

            if (rows > 0) {
                logger.info("Inserted hotel image id=" + image.getImageId());
            }
            return rows > 0;
        }
    }

    // Selects the one hotel_image row whose image_id matches the given id.
    @Override
    public HotelImage findById(long imageId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {

            statement.setLong(1, imageId);

            logger.fine("Selecting hotel image with id " + imageId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                logger.warning("No hotel image found for id " + imageId);
                return null;
            }
        }
    }

    // Selects every hotel_image row for this hotel_id, ordered by display_order.
    @Override
    public List<HotelImage> findByHotel(long hotelId) throws SQLException {
        List<HotelImage> images = new ArrayList<>();

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_HOTEL)) {

            statement.setLong(1, hotelId);

            logger.fine("Selecting hotel images for hotel id " + hotelId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    images.add(mapRow(resultSet));
                }
            }
        }

        if (images.isEmpty()) {
            logger.warning("No hotel images found for hotel id " + hotelId);
        }
        return images;
    }

    // Deletes the hotel_image row whose image_id matches the given id.
    @Override
    public boolean delete(long imageId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setLong(1, imageId);
            logger.fine("Deleting hotel image with id " + imageId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted hotel image id=" + imageId);
            }
            return rows > 0;
        }
    }

    // Copies one hotel_image row into a HotelImage. hotel_id is stored on a Hotel object.
    private HotelImage mapRow(ResultSet resultSet) throws SQLException {
        HotelImage image = new HotelImage();
        image.setImageId(resultSet.getLong("image_id"));

        Hotel hotel = new Hotel();
        hotel.setHotelId(resultSet.getLong("hotel_id"));
        image.setHotel(hotel);

        image.setImageUrl(resultSet.getString("image_url"));
        image.setCaption(resultSet.getString("caption"));
        image.setPrimary(resultSet.getBoolean("is_primary"));
        image.setDisplayOrder(resultSet.getInt("display_order"));
        return image;
    }

    private void setImageColumns(PreparedStatement statement, HotelImage image) throws SQLException {
        statement.setLong(1, image.getHotel().getHotelId());
        statement.setString(2, image.getImageUrl());
        statement.setString(3, image.getCaption());
        statement.setBoolean(4, image.isPrimary());
        statement.setInt(5, image.getDisplayOrder());
    }
}
