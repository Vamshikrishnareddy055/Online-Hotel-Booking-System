package com.booking.hotel.dao;

import com.booking.hotel.model.Location;
import com.booking.hotel.util.JdbcUtil;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class LocationDAOImpl implements LocationDAO {

    // Records each location-table action. Messages are plain strings.
    private static final Logger logger = Logger.getLogger(LocationDAOImpl.class.getName());

    private static final String SQL_INSERT_LOCATION =
            "INSERT INTO location (name, type, parent_id) VALUES (?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM location WHERE location_id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM location WHERE location_id = ?";

    // Inserts one row into the location table and stores the new location_id on the Location object.
    @Override
    public boolean create(Location location) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_LOCATION, Statement.RETURN_GENERATED_KEYS)) {

            setLocationColumns(statement, location);

            logger.fine("Inserting location with name " + location.getName());
            int rows = statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    location.setLocationId(keys.getLong(1));
                }
            }

            if (rows > 0) {
                logger.info("Inserted location id=" + location.getLocationId());
            }
            return rows > 0;
        }
    }

    // Selects the one location row whose location_id matches the given id.
    @Override
    public Location findById(long locationId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {

            statement.setLong(1, locationId);

            logger.fine("Selecting location with id " + locationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                logger.warning("No location found for id " + locationId);
                return null;
            }
        }
    }

    // Deletes the location row whose location_id matches the given id.
    @Override
    public boolean delete(long locationId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setLong(1, locationId);
            logger.fine("Deleting location with id " + locationId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted location id=" + locationId);
            }
            return rows > 0;
        }
    }

    // Builds "Mumbai, Maharashtra, India" by loading each parent with findById. This is not one SQL query.
    @Override
    public String resolveFullPath(long locationId) throws SQLException {
        logger.fine("Resolving full path for location id " + locationId);

        Location current = findById(locationId);
        if (current == null) {
            return null;
        }

        StringBuilder path = new StringBuilder(current.getName());
        while (current.getParent() != null) {
            current = findById(current.getParent().getLocationId());
            if (current == null) {
                break;
            }
            path.append(", ").append(current.getName());
        }
        return path.toString();
    }

    // Copies one location-table row into a Location. A null parent_id leaves parent unset.
    private Location mapRow(ResultSet resultSet) throws SQLException {
        Location location = new Location();
        location.setLocationId(resultSet.getLong("location_id"));
        location.setName(resultSet.getString("name"));
        location.setType(resultSet.getString("type"));

        long parentId = resultSet.getLong("parent_id");
        if (!resultSet.wasNull()) {
            Location parent = new Location();
            parent.setLocationId(parentId);
            location.setParent(parent);
        }
        return location;
    }

    private void setLocationColumns(PreparedStatement statement, Location location) throws SQLException {
        statement.setString(1, location.getName());
        statement.setString(2, location.getType());
        if (location.getParent() == null) {
            statement.setNull(3, Types.BIGINT);
        } else {
            statement.setLong(3, location.getParent().getLocationId());
        }
    }
}
