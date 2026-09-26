package com.booking.hotel.dao;

import com.booking.hotel.model.Location;

import java.sql.SQLException;

public interface LocationDAO {

    boolean create(Location location) throws SQLException;

    Location findById(long locationId) throws SQLException;

    boolean delete(long locationId) throws SQLException;

    String resolveFullPath(long locationId) throws SQLException;
}
