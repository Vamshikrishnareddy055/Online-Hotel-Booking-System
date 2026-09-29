package com.booking.hotel.service;

import com.booking.hotel.dao.UserDAO;
import com.booking.hotel.exception.DuplicateEmailException;
import com.booking.hotel.exception.UserNotFoundException;
import com.booking.hotel.model.Roles;
import com.booking.hotel.model.User;
import com.booking.hotel.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    private final UserDAO userDAO;

    // Receives the DAO from outside so this class can be tested with a fake DAO later.
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Loads one user by id. Throws UserNotFoundException when that id is not in the database.
    public User getUserById(long userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            logger.warning("No user found with id " + userId);
            throw new UserNotFoundException("No user found with id " + userId);
        }
        return user;
    }

    // Saves a new customer with status ACTIVE and returns that user, including the generated id.
    public User registerUser(String fullName, String email, String password, String phone) throws SQLException {
        // One email can belong to only one account.
        User existing = userDAO.findByEmail(email);
        if (existing != null) {
            logger.warning("Registration failed: email already exists");
            throw new DuplicateEmailException("An account with this email already exists");
        }

        // The password column stores a hash, not the password the person typed.
        String passwordHash = PasswordUtil.hashPassword(password);
        User user = new User(0, fullName, email, passwordHash, phone, Roles.CUSTOMER, "ACTIVE");
        boolean created = userDAO.create(user);
        if (created) {
            logger.info("Registered user id=" + user.getUserId());
        }
        return user;
    }

    // Returns every user in the table, which the admin list shows.
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }
}
