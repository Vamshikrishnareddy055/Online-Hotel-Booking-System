package com.booking.hotel.dao;

import com.booking.hotel.model.Booking;
import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Payment;
import com.booking.hotel.model.Room;
import com.booking.hotel.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Integration test for PaymentDAOImpl. It needs a running local MySQL database
// with the hotel_booking_system schema already applied, because it calls real JDBC code.
class PaymentDAOImplTest {

    private UserDAO userDAO;
    private HotelDAO hotelDAO;
    private RoomDAO roomDAO;
    private BookingDAO bookingDAO;
    private PaymentDAO paymentDAO;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;
    private Booking testBooking;

    private long createdUserId;
    private long createdHotelId;
    private long createdRoomId;
    private long createdBookingId;
    private long createdPaymentId;

    // Inserts a throwaway user, hotel, room, and booking so a payment can use a real booking_id.
    @BeforeEach
    void setUp() throws SQLException {
        userDAO = new UserDAOImpl();
        hotelDAO = new HotelDAOImpl();
        roomDAO = new RoomDAOImpl();
        bookingDAO = new BookingDAOImpl();
        paymentDAO = new PaymentDAOImpl();

        createdUserId = 0;
        createdHotelId = 0;
        createdRoomId = 0;
        createdBookingId = 0;
        createdPaymentId = 0;

        testUser = newTestUser();
        userDAO.create(testUser);
        createdUserId = testUser.getUserId();

        testHotel = newTestHotel();
        hotelDAO.create(testHotel);
        createdHotelId = testHotel.getHotelId();

        testRoom = newTestRoom(testHotel);
        roomDAO.create(testRoom);
        createdRoomId = testRoom.getRoomId();

        testBooking = newTestBooking();
        bookingDAO.create(testBooking);
        createdBookingId = testBooking.getBookingId();
    }

    // Deletes the payment first, then the booking, room, hotel, and user. A missing row is ignored.
    @AfterEach
    void deleteTestRows() {
        try {
            if (createdPaymentId > 0) {
                paymentDAO.delete(createdPaymentId);
            }
        } catch (SQLException ignored) {
            // The payment may already be gone. Still delete the booking and the rows it depends on.
        }
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

    // Checks that create inserts a payment and MySQL assigns an id.
    @Test
    void createInsertsPaymentAndSetsGeneratedId() throws SQLException {
        Payment payment = newTestPayment();

        boolean created = paymentDAO.create(payment);
        createdPaymentId = payment.getPaymentId();

        assertTrue(created);
        assertTrue(createdPaymentId > 0);
    }

    // Checks that findById returns the same values that were inserted.
    @Test
    void findByIdReturnsInsertedPayment() throws SQLException {
        Payment payment = newTestPayment();
        paymentDAO.create(payment);
        createdPaymentId = payment.getPaymentId();

        Payment found = paymentDAO.findById(createdPaymentId);

        assertNotNull(found);
        assertEquals(createdPaymentId, found.getPaymentId());
        assertEquals(testBooking.getBookingId(), found.getBooking().getBookingId());
        assertEquals(0, payment.getAmount().compareTo(found.getAmount()));
        assertEquals(payment.getPaymentMethod(), found.getPaymentMethod());
        assertEquals(payment.getPaymentStatus(), found.getPaymentStatus());
        assertEquals(payment.getTransactionRef(), found.getTransactionRef());
        assertEquals(payment.getPaidAt(), found.getPaidAt());
    }

    // Checks that findByBooking returns the payment attached to this booking.
    @Test
    void findByBookingReturnsCreatedPayment() throws SQLException {
        Payment payment = newTestPayment();
        paymentDAO.create(payment);
        createdPaymentId = payment.getPaymentId();

        Payment found = paymentDAO.findByBooking(testBooking.getBookingId());

        assertNotNull(found);
        assertEquals(createdPaymentId, found.getPaymentId());
        assertEquals(payment.getTransactionRef(), found.getTransactionRef());
        assertEquals(testBooking.getBookingId(), found.getBooking().getBookingId());
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

    // transaction_ref must be unique. paidAt has no fractional seconds because the column is TIMESTAMP.
    private Payment newTestPayment() {
        Payment payment = new Payment();
        payment.setBooking(testBooking);
        payment.setAmount(new BigDecimal("4500.00"));
        payment.setPaymentMethod("CARD");
        payment.setPaymentStatus("PAID");
        payment.setTransactionRef("txn-" + UUID.randomUUID());
        payment.setPaidAt(LocalDateTime.now().withNano(0));
        return payment;
    }
}
