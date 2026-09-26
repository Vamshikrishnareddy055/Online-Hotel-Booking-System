package com.booking.hotel.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcUtil {

    // Records whether opening a database connection succeeded or failed.
    private static final Logger logger = Logger.getLogger(JdbcUtil.class.getName());

    private static final String URL =
            "jdbc:mysql://localhost:3306/hotel_booking_system";

    private static final String USER = "root";

    private static final String PASSWORD =
            System.getenv("HOTEL_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.fine("Opened database connection");
            return connection;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection failed for user=" + USER, e);
            throw e;
        }
    }

    public static void main(String[] args) {

        try (Connection connection = getConnection()) {

            System.out.println("Database connected successfully!");

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
}