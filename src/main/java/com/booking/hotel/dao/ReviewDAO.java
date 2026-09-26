package com.booking.hotel.dao;

import com.booking.hotel.model.Review;

import java.sql.SQLException;
import java.util.List;

public interface ReviewDAO {

    boolean create(Review review) throws SQLException;

    Review findById(long reviewId) throws SQLException;

    List<Review> findByHotel(long hotelId) throws SQLException;

    boolean delete(long reviewId) throws SQLException;
}
