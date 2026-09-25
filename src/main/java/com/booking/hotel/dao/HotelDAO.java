package com.booking.hotel.dao;

import com.booking.hotel.model.Hotel;

import java.sql.SQLException;
import java.util.List;

public interface HotelDAO {

    boolean create(Hotel hotel) throws SQLException;

    Hotel findById(long hotelId) throws SQLException;

    List<Hotel> findByCity(String city) throws SQLException;

    List<Hotel> findAll() throws SQLException;

    boolean update(Hotel hotel) throws SQLException;

    boolean delete(long hotelId) throws SQLException;
}
