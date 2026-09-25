package com.booking.hotel.dao;

import com.booking.hotel.model.Hotel;
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

// Integration tests for HotelDAOImpl. They need a running local MySQL database
// with the hotel_booking_system schema already applied, because they call real JDBC code.
class HotelDAOImplTest {

    private HotelDAO hotelDAO;
    private long createdHotelId;

    // Builds a fresh DAO before each test. The id starts at 0 until a test inserts a row.
    @BeforeEach
    void setUp() {
        hotelDAO = new HotelDAOImpl();
        createdHotelId = 0;
    }

    // Removes the row this test inserted. A missing row is ignored so cleanup does not hide a real failure.
    @AfterEach
    void deleteTestHotel() {
        if (createdHotelId <= 0) {
            return;
        }
        try {
            hotelDAO.delete(createdHotelId);
        } catch (SQLException ignored) {
            // The row may already have been deleted by the test.
        }
    }

    // Checks that create inserts a row and MySQL assigns an id.
    @Test
    void createInsertsHotelAndSetsGeneratedId() throws SQLException {
        Hotel hotel = newTestHotel();

        boolean created = hotelDAO.create(hotel);
        createdHotelId = hotel.getHotelId();

        assertTrue(created);
        assertTrue(createdHotelId > 0);
    }

    // Checks that findById returns the same values that were inserted.
    @Test
    void findByIdReturnsInsertedHotel() throws SQLException {
        Hotel hotel = newTestHotel();
        hotelDAO.create(hotel);
        createdHotelId = hotel.getHotelId();

        Hotel found = hotelDAO.findById(createdHotelId);

        assertNotNull(found);
        assertEquals(createdHotelId, found.getHotelId());
        assertEquals(hotel.getName(), found.getName());
        assertEquals(hotel.getDescription(), found.getDescription());
        assertEquals(hotel.getAddress(), found.getAddress());
        assertEquals(hotel.getCity(), found.getCity());
        assertEquals(hotel.getState(), found.getState());
        assertEquals(hotel.getCountry(), found.getCountry());
        assertEquals(0, hotel.getStarRating().compareTo(found.getStarRating()));
        assertEquals(hotel.getAmenities(), found.getAmenities());
        assertEquals(hotel.getStatus(), found.getStatus());
    }

    // Checks that findByCity includes the hotel that was just inserted in that city.
    @Test
    void findByCityIncludesCreatedHotel() throws SQLException {
        Hotel hotel = newTestHotel();
        hotelDAO.create(hotel);
        createdHotelId = hotel.getHotelId();

        List<Hotel> hotels = hotelDAO.findByCity(hotel.getCity());

        boolean found = false;
        for (Hotel candidate : hotels) {
            if (candidate.getHotelId() == createdHotelId) {
                found = true;
                assertEquals(hotel.getName(), candidate.getName());
                assertEquals(hotel.getCity(), candidate.getCity());
            }
        }
        assertTrue(found);
    }

    // Builds a hotel whose name and city cannot collide with rows already in the table.
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
}
