package com.booking.hotel.dao;

import com.booking.hotel.model.User;
import com.booking.hotel.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    // Inserts one row into the user table and stores the new user_id on the User object.
    @Override
    public boolean create(User user) throws SQLException {
        String sql = "INSERT INTO `user` (full_name, email, password_hash, phone, role, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getRole());
            statement.setString(6, user.getStatus());

            int rows = statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getLong(1));
                }
            }

            return rows > 0;
        }
    }

    // Selects the one user row whose user_id matches the given id.
    @Override
    public User findById(long userId) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE user_id = ?";

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                return null;
            }
        }
    }

    // Selects every row from the user table.
    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM `user`";
        List<User> users = new ArrayList<>();

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
        }

        return users;
    }

    // Updates full_name, email, password_hash, phone, role, and status for this user_id.
    @Override
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE `user` SET full_name = ?, email = ?, password_hash = ?, "
                + "phone = ?, role = ?, status = ? WHERE user_id = ?";

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getRole());
            statement.setString(6, user.getStatus());
            statement.setLong(7, user.getUserId());

            return statement.executeUpdate() > 0;
        }
    }

    // Deletes the user row whose user_id matches the given id.
    @Override
    public boolean delete(long userId) throws SQLException {
        String sql = "DELETE FROM `user` WHERE user_id = ?";

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    // Copies one user-table row into a User. The password field comes from the password_hash column.
    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getLong("user_id"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password_hash"));
        user.setPhone(resultSet.getString("phone"));
        user.setRole(resultSet.getString("role"));
        user.setStatus(resultSet.getString("status"));
        return user;
    }
}
