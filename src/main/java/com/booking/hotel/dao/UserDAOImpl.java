package com.booking.hotel.dao;

import com.booking.hotel.model.User;
import com.booking.hotel.util.JdbcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    // Records each user-table action. The {} placeholders are filled by the values passed after the message.
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    private static final String SQL_INSERT_USER =
            "INSERT INTO `user` (full_name, email, password_hash, phone, role, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM `user` WHERE user_id = ?";

    private static final String SQL_FIND_ALL =
            "SELECT * FROM `user`";

    private static final String SQL_UPDATE =
            "UPDATE `user` SET full_name = ?, email = ?, password_hash = ?, "
                    + "phone = ?, role = ?, status = ? WHERE user_id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM `user` WHERE user_id = ?";

    // Inserts one row into the user table and stores the new user_id on the User object.
    @Override
    public boolean create(User user) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getRole());
            statement.setString(6, user.getStatus());

            logger.debug("Inserting user with email {}", user.getEmail());
            int rows = statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getLong(1));
                }
            }

            if (rows > 0) {
                logger.info("Inserted user id={}", user.getUserId());
            }
            return rows > 0;
        }
    }

    // Selects the one user row whose user_id matches the given id.
    @Override
    public User findById(long userId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {

            statement.setLong(1, userId);

            logger.debug("Selecting user with id {}", userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                logger.warn("No user found for id {}", userId);
                return null;
            }
        }
    }

    // Selects every row from the user table.
    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_ALL)) {

            logger.debug("Selecting all users");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapRow(resultSet));
                }
            }
        }

        if (users.isEmpty()) {
            logger.warn("No users found");
        }
        return users;
    }

    // Updates full_name, email, password_hash, phone, role, and status for this user_id.
    @Override
    public boolean update(User user) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getRole());
            statement.setString(6, user.getStatus());
            statement.setLong(7, user.getUserId());

            logger.debug("Updating user with id {}", user.getUserId());
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Updated user id={}", user.getUserId());
            }
            return rows > 0;
        }
    }

    // Deletes the user row whose user_id matches the given id.
    @Override
    public boolean delete(long userId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setLong(1, userId);
            logger.debug("Deleting user with id {}", userId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted user id={}", userId);
            }
            return rows > 0;
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
