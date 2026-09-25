package com.booking.hotel.dao;

import com.booking.hotel.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Integration tests for UserDAOImpl. They need a running local MySQL database
// with the hotel_booking_system schema already applied, because they call real JDBC code.
class UserDAOImplTest {

    private UserDAO userDAO;
    private long createdUserId;

    // Builds a fresh DAO before each test. The id starts at 0 until a test inserts a row.
    @BeforeEach
    void setUp() {
        userDAO = new UserDAOImpl();
        createdUserId = 0;
    }

    // Removes the row this test inserted. A missing row is ignored so cleanup does not hide a real failure.
    @AfterEach
    void deleteTestUser() {
        if (createdUserId <= 0) {
            return;
        }
        try {
            userDAO.delete(createdUserId);
        } catch (SQLException ignored) {
            // The row may already have been deleted by the test.
        }
    }

    // Checks that create inserts a row and MySQL assigns an id.
    @Test
    void createInsertsUserAndSetsGeneratedId() throws SQLException {
        User user = newTestUser();

        boolean created = userDAO.create(user);
        createdUserId = user.getUserId();

        assertTrue(created);
        assertTrue(createdUserId > 0);
    }

    // Checks that findById returns the same values that were inserted.
    @Test
    void findByIdReturnsInsertedUser() throws SQLException {
        User user = newTestUser();
        userDAO.create(user);
        createdUserId = user.getUserId();

        User found = userDAO.findById(createdUserId);

        assertNotNull(found);
        assertEquals(createdUserId, found.getUserId());
        assertEquals(user.getFullName(), found.getFullName());
        assertEquals(user.getEmail(), found.getEmail());
        assertEquals(user.getPassword(), found.getPassword());
        assertEquals(user.getPhone(), found.getPhone());
        assertEquals(user.getRole(), found.getRole());
        assertEquals(user.getStatus(), found.getStatus());
    }

    // Checks that update changes a saved field, and delete makes findById return null.
    @Test
    void updateChangesPhoneAndDeleteRemovesUser() throws SQLException {
        User user = newTestUser();
        userDAO.create(user);
        createdUserId = user.getUserId();

        user.setPhone("8888888888");
        assertTrue(userDAO.update(user));

        User updated = userDAO.findById(createdUserId);
        assertNotNull(updated);
        assertEquals("8888888888", updated.getPhone());

        assertTrue(userDAO.delete(createdUserId));
        assertNull(userDAO.findById(createdUserId));
    }

    // Builds a user whose email cannot collide with rows already in the table.
    private User newTestUser() {
        String uniqueEmail = "user-" + UUID.randomUUID() + "@example.com";
        return new User(0, "Test User", uniqueEmail, "test123", "9999999999", "CUSTOMER", "ACTIVE");
    }
}
