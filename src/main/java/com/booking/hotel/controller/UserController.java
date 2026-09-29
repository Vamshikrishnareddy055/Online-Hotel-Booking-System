package com.booking.hotel.controller;

import com.booking.hotel.exception.UnauthorizedException;
import com.booking.hotel.exception.UserNotFoundException;
import com.booking.hotel.model.Roles;
import com.booking.hotel.model.User;
import com.booking.hotel.service.UserService;
import com.booking.hotel.util.SessionContext;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Sits between Main (console input and output) and UserService (business rules).
// Register is open. Viewing a user or the full list depends on who is logged in.
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    private final UserService userService;
    private final SessionContext sessionContext;

    public UserController(UserService userService, SessionContext sessionContext) {
        this.userService = userService;
        this.sessionContext = sessionContext;
    }

    // Anyone can create an account. No login is required.
    public User register(String fullName, String email, String password, String phone) throws SQLException {
        try {
            return userService.registerUser(fullName, email, password, phone);
        } catch (SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    // A customer can open only their own account. An admin can open any account.
    public User getUser(long userId) throws SQLException {
        try {
            User current = sessionContext.requireLogin();
            boolean isAdmin = Roles.ADMIN.equals(current.getRole());
            boolean isOwnAccount = Roles.CUSTOMER.equals(current.getRole()) && current.getUserId() == userId;
            if (!isAdmin && !isOwnAccount) {
                throw new UnauthorizedException("Access denied");
            }
            return userService.getUserById(userId);
        } catch (UnauthorizedException | UserNotFoundException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }

    // Only an admin can list every user.
    public List<User> listUsers() throws SQLException {
        try {
            sessionContext.requireRole(Roles.ADMIN);
            return userService.getAllUsers();
        } catch (UnauthorizedException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }
}
