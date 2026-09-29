package com.booking.hotel.dao;

import com.booking.hotel.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDAO {

    boolean create(User user) throws SQLException;

    User findById(long userId) throws SQLException;

    User findByEmail(String email) throws SQLException;

    List<User> findAll() throws SQLException;

    boolean update(User user) throws SQLException;

    boolean delete(long userId) throws SQLException;
}