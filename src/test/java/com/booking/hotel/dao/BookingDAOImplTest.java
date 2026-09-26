package com.booking.hotel.dao;

import com.booking.hotel.model.Booking;
import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Room;
import com.booking.hotel.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Integration test for BookingDAOImpl. It needs a running local MySQL database
// with the hotel_booking_system schema already applied, because it calls real JDBC code.
class BookingDAOImplTest {

    private UserDAO userDAO;
    private HotelDAO hotelDAO;
    private RoomDAO roomDAO;
    private BookingDAO bookingDAO;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;

    private long createdUserId;
    private long createdHotelId;
    private long createdRoomId;
    private long createdBookingId;

    // Inserts a throwaway user, hotel, and room so a booking can use real foreign keys.
    @BeforeEach
    void setUp() throws SQLException {
        userDAO = new UserDAOImpl();
        hotelDAO = new HotelDAOImpl();
        roomDAO = new RoomDAOImpl();
        bookingDAO = new BookingDAOImpl();

        createdUserId = 0;
        createdHotelId = 0;
        createdRoomId = 0;
        createdBookingId = 0;

        testUser = newTestUser();
        userDAO.create(testUser);
        createdUserId = testUser.getUserId();

        testHotel = newTestHotel();
        hotelDAO.create(testHotel);
        createdHotelId = testHotel.getHotelId();

        testRoom = newTestRoom(testHotel);
        roomDAO.create(testRoom);
        createdRoomId = testRoom.getRoomId();
    }

    // Deletes the booking first, then the room, hotel, and user. A missing row is ignored.
    @AfterEach
    void deleteTestRows() {
        try {
            if (createdBookingId > 0) {
                bookingDAO.delete(createdBookingId);
            }
        } catch (SQLException ignored) {
            // The booking may already be gone. Still delete the room, hotel, and user below.
        }
        try {
            if (createdRoomId > 0) {
                roomDAO.delete(createdRoomId);
            }
        } catch (SQLException ignored) {
            // The room may already be gone. Still delete the hotel and user below.
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

    // Checks that create inserts a booking and MySQL assigns an id.
    @Test
    void createInsertsBookingAndSetsGeneratedId() throws SQLException {
        Booking booking = newTestBooking();

        boolean created = bookingDAO.create(booking);
        createdBookingId = booking.getBookingId();

        assertTrue(created);
        assertTrue(createdBookingId > 0);
    }

    // Checks that findById returns the inserted values, including the same check-in and check-out dates.
    @Test
    void findByIdReturnsInsertedBooking() throws SQLException {
        Booking booking = newTestBooking();
        bookingDAO.create(booking);
        createdBookingId = booking.getBookingId();

        Booking found = bookingDAO.findById(createdBookingId);

        assertNotNull(found);
        assertEquals(createdBookingId, found.getBookingId());
        assertEquals(testUser.getUserId(), found.getUser().getUserId());
        assertEquals(testHotel.getHotelId(), found.getHotel().getHotelId());
        assertEquals(testRoom.getRoomId(), found.getRoom().getRoomId());
        assertEquals(booking.getCheckInDate(), found.getCheckInDate());
        assertEquals(booking.getCheckOutDate(), found.getCheckOutDate());
        assertEquals(booking.getGuestsAdults(), found.getGuestsAdults());
        assertEquals(booking.getGuestsChildren(), found.getGuestsChildren());
        assertEquals(0, booking.getTotalAmount().compareTo(found.getTotalAmount()));
        assertEquals(booking.getPaymentOption(), found.getPaymentOption());
        assertEquals(booking.getBookingStatus(), found.getBookingStatus());
    }

    // Checks that findByUser includes the booking that was just inserted for this user.
    @Test
    void findByUserIncludesCreatedBooking() throws SQLException {
        Booking booking = newTestBooking();
        bookingDAO.create(booking);
        createdBookingId = booking.getBookingId();

        List<Booking> bookings = bookingDAO.findByUser(testUser.getUserId());

        boolean found = false;
        for (Booking candidate : bookings) {
            if (candidate.getBookingId() == createdBookingId) {
                found = true;
                assertEquals(testUser.getUserId(), candidate.getUser().getUserId());
            }
        }
        assertTrue(found);
    }

    // Checks that updateStatus changes booking_status and findById returns the new value.
    @Test
    void updateStatusChangesBookingStatus() throws SQLException {
        Booking booking = newTestBooking();
        bookingDAO.create(booking);
        createdBookingId = booking.getBookingId();

        assertTrue(bookingDAO.updateStatus(createdBookingId, "CANCELLED"));

        Booking found = bookingDAO.findById(createdBookingId);
        assertNotNull(found);
        assertEquals("CANCELLED", found.getBookingStatus());
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

    private Room newTestRoom(Hotel hotel) {
        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomNumber("R" + UUID.randomUUID().toString().substring(0, 8));
        room.setRoomType("DELUXE");
        room.setCapacity(2);
        room.setBasePrice(new BigDecimal("1500.00"));
        room.setStatus("AVAILABLE");
        return room;
    }

    // Check-out is three days after check-in so the two dates are different.
    private Booking newTestBooking() {
        Booking booking = new Booking();
        booking.setUser(testUser);
        booking.setHotel(testHotel);
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(4));
        booking.setGuestsAdults(2);
        booking.setGuestsChildren(1);
        booking.setTotalAmount(new BigDecimal("4500.00"));
        booking.setPaymentOption("CARD");
        booking.setBookingStatus("CONFIRMED");
        return booking;
    }
}
