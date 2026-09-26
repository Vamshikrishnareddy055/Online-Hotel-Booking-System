package com.booking.hotel.dao;

import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Review;
import com.booking.hotel.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Integration test for ReviewDAOImpl. It needs a running local MySQL database
// with the hotel_booking_system schema already applied, because it calls real JDBC code.
class ReviewDAOImplTest {

    private UserDAO userDAO;
    private HotelDAO hotelDAO;
    private ReviewDAO reviewDAO;

    private User testUser;
    private Hotel testHotel;

    private long createdUserId;
    private long createdHotelId;
    private long createdReviewId;

    // Inserts a throwaway user and hotel so a review can use real foreign keys.
    @BeforeEach
    void setUp() throws SQLException {
        userDAO = new UserDAOImpl();
        hotelDAO = new HotelDAOImpl();
        reviewDAO = new ReviewDAOImpl();
        createdUserId = 0;
        createdHotelId = 0;
        createdReviewId = 0;

        testUser = newTestUser();
        userDAO.create(testUser);
        createdUserId = testUser.getUserId();

        testHotel = newTestHotel();
        hotelDAO.create(testHotel);
        createdHotelId = testHotel.getHotelId();
    }

    // Deletes the review first, then the hotel and user. A missing row is ignored.
    @AfterEach
    void deleteTestRows() {
        try {
            if (createdReviewId > 0) {
                reviewDAO.delete(createdReviewId);
            }
        } catch (SQLException ignored) {
            // The review may already be gone. Still delete the hotel and user below.
        }
        try {
            if (createdHotelId > 0) {
                hotelDAO.delete(createdHotelId);
            }
        } catch (SQLException ignored) {
            // The hotel may already be gone. Still delete the user below.
        }
        try {
            if (createdUserId > 0) {
                userDAO.delete(createdUserId);
            }
        } catch (SQLException ignored) {
            // The user may already be gone.
        }
    }

    // Checks that create inserts a review and MySQL assigns an id.
    @Test
    void createInsertsReviewAndSetsGeneratedId() throws SQLException {
        Review review = newTestReview();

        boolean created = reviewDAO.create(review);
        createdReviewId = review.getReviewId();

        assertTrue(created);
        assertTrue(createdReviewId > 0);
    }

    // Checks that findById returns the same values that were inserted.
    @Test
    void findByIdReturnsInsertedReview() throws SQLException {
        Review review = newTestReview();
        reviewDAO.create(review);
        createdReviewId = review.getReviewId();

        Review found = reviewDAO.findById(createdReviewId);

        assertNotNull(found);
        assertEquals(createdReviewId, found.getReviewId());
        assertEquals(testUser.getUserId(), found.getUser().getUserId());
        assertEquals(testHotel.getHotelId(), found.getHotel().getHotelId());
        assertEquals(review.getRating(), found.getRating());
        assertEquals(review.getTitle(), found.getTitle());
        assertEquals(review.getComment(), found.getComment());
    }

    // Checks that findByHotel includes the review that was just inserted for this hotel.
    @Test
    void findByHotelIncludesCreatedReview() throws SQLException {
        Review review = newTestReview();
        reviewDAO.create(review);
        createdReviewId = review.getReviewId();

        List<Review> reviews = reviewDAO.findByHotel(testHotel.getHotelId());

        boolean found = false;
        for (Review candidate : reviews) {
            if (candidate.getReviewId() == createdReviewId) {
                found = true;
                assertEquals(review.getRating(), candidate.getRating());
                assertEquals(review.getComment(), candidate.getComment());
                assertEquals(testHotel.getHotelId(), candidate.getHotel().getHotelId());
            }
        }
        assertTrue(found);
    }

    private User newTestUser() {
        String uniqueEmail = "user-" + UUID.randomUUID() + "@example.com";
        return new User(0, "Test User", uniqueEmail, "test123", "9999999999", "CUSTOMER", "ACTIVE");
    }

    private Hotel newTestHotel() {
        String unique = UUID.randomUUID().toString();
        Hotel hotel = new Hotel();
        hotel.setName("Hotel " + unique);
        hotel.setDescription("Test hotel");
        hotel.setAddress("1 Test Street");
        hotel.setCity("City-" + unique);
        hotel.setState("Test State");
        hotel.setCountry("India");
        hotel.setStarRating(new BigDecimal("4.5"));
        hotel.setAmenities("WiFi");
        hotel.setStatus("ACTIVE");
        return hotel;
    }

    private Review newTestReview() {
        Review review = new Review();
        review.setUser(testUser);
        review.setHotel(testHotel);
        review.setRating(5);
        review.setTitle("Great stay");
        review.setComment("Clean room and helpful staff");
        return review;
    }
}
