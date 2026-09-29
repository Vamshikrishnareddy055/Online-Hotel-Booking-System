package com.booking.hotel.controller;

import com.booking.hotel.exception.InvalidCredentialsException;
import com.booking.hotel.model.User;
import com.booking.hotel.service.AuthService;
import com.booking.hotel.util.SessionContext;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Sits between Main and AuthService. A successful login is stored for the rest of this console run.
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    private final AuthService authService;
    private final SessionContext sessionContext;

    public AuthController(AuthService authService, SessionContext sessionContext) {
        this.authService = authService;
        this.sessionContext = sessionContext;
    }

    // Checks the email and password, then remembers that user until they log out.
    public User login(String email, String password) throws SQLException {
        try {
            User user = authService.login(email, password);
            sessionContext.login(user);
            return user;
        } catch (InvalidCredentialsException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }
}
