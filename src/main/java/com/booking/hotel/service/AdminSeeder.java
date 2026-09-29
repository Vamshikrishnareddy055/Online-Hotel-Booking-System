package com.booking.hotel.service;

import com.booking.hotel.dao.UserDAO;
import com.booking.hotel.model.Roles;
import com.booking.hotel.model.User;
import com.booking.hotel.util.PasswordUtil;

import java.sql.SQLException;
import java.util.logging.Logger;

// Creates one admin account the first time the app starts, if that email is not already saved.
public class AdminSeeder {

    private static final Logger logger = Logger.getLogger(AdminSeeder.class.getName());

    private static final String ADMIN_EMAIL = "admin@bbj.com";
    private static final String ADMIN_NAME = "System Admin";

    private final UserDAO userDAO;

    // Receives the DAO from outside so this class can be tested with a fake DAO later.
    public AdminSeeder(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Inserts the default admin when the environment password is set and the email is new.
    public void seedDefaultAdmin() throws SQLException {
        // The password lives in the environment, not in this source file.
        String password = System.getenv("HOTEL_ADMIN_PASSWORD");
        if (password == null || password.isBlank()) {
            logger.warning("HOTEL_ADMIN_PASSWORD is not set - default admin was not created");
            return;
        }

        // Starting the app again must not insert a second admin with the same email.
        if (userDAO.findByEmail(ADMIN_EMAIL) != null) {
            logger.fine("Default admin already exists");
            return;
        }

        // Store the hash, not the password read from the environment.
        String passwordHash = PasswordUtil.hashPassword(password);
        User admin = new User(0, ADMIN_NAME, ADMIN_EMAIL, passwordHash, "", Roles.ADMIN, "ACTIVE");
        userDAO.create(admin);
        logger.info("Default admin created with id=" + admin.getUserId());
    }
}
