package com.booking.hotel.service;

import com.booking.hotel.dao.UserDAO;
import com.booking.hotel.exception.InvalidCredentialsException;
import com.booking.hotel.model.User;
import com.booking.hotel.util.PasswordUtil;

import java.sql.SQLException;
import java.util.logging.Logger;

public class AuthService {

    private static final Logger logger = Logger.getLogger(AuthService.class.getName());

    private final UserDAO userDAO;

    // Receives the DAO from outside so this class can be tested with a fake DAO later.
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Checks the email and password and returns the matching active user.
    public User login(String email, String password) throws SQLException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            logger.warning("Login failed");
            throw new InvalidCredentialsException("Email and password are required");
        }

        User user = userDAO.findByEmail(email);
        // A missing account and a wrong password share one message, so a stranger cannot tell which part failed.
        if (user == null || !PasswordUtil.verifyPassword(password, user.getPassword())) {
            logger.warning("Login failed");
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            logger.warning("Login failed");
            throw new InvalidCredentialsException("Account is not active");
        }

        logger.info("Login successful for user id=" + user.getUserId());
        return user;
    }
}
