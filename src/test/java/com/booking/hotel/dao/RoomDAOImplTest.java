package com.booking.hotel.dao;

import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Room;
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

// Integration test for RoomDAOImpl. It needs a running local MySQL database
// with the hotel_booking_system schema already applied, because it calls real JDBC code.
class RoomDAOImplTest {

    private HotelDAO hotelDAO;
    private RoomDAO roomDAO;

    private Hotel testHotel;

    private long createdHotelId;
    private long createdRoomId;

    // Inserts a throwaway hotel so a room can use a real hotel_id.
    @BeforeEach
    void setUp() throws SQLException {
        hotelDAO = new HotelDAOImpl();
        roomDAO = new RoomDAOImpl();
        createdHotelId = 0;
        createdRoomId = 0;

        testHotel = newTestHotel();
        hotelDAO.create(testHotel);
        createdHotelId = testHotel.getHotelId();
    }

    // Deletes the room first, then the hotel. A missing row is ignored.
    @AfterEach
    void deleteTestRows() {
        try {
            if (createdRoomId > 0) {
                roomDAO.delete(createdRoomId);
            }
        } catch (SQLException ignored) {
            // The room may already be gone. Still delete the hotel below.
        }
        try {
            if (createdHotelId > 0) {
                hotelDAO.delete(createdHotelId);
            }
        } catch (SQLException ignored) {
            // The hotel may already be gone.
        }
    }

    // Checks that create inserts a room and MySQL assigns an id.
    @Test
    void createInsertsRoomAndSetsGeneratedId() throws SQLException {
        Room room = newTestRoom();

        boolean created = roomDAO.create(room);
        createdRoomId = room.getRoomId();

        assertTrue(created);
        assertTrue(createdRoomId > 0);
    }

    // Checks that findById returns the same values that were inserted.
    @Test
    void findByIdReturnsInsertedRoom() throws SQLException {
        Room room = newTestRoom();
        roomDAO.create(room);
        createdRoomId = room.getRoomId();

        Room found = roomDAO.findById(createdRoomId);

        assertNotNull(found);
        assertEquals(createdRoomId, found.getRoomId());
        assertEquals(testHotel.getHotelId(), found.getHotel().getHotelId());
        assertEquals(room.getRoomNumber(), found.getRoomNumber());
        assertEquals(room.getRoomType(), found.getRoomType());
        assertEquals(room.getCapacity(), found.getCapacity());
        assertEquals(0, room.getBasePrice().compareTo(found.getBasePrice()));
        assertEquals(room.getStatus(), found.getStatus());
    }

    // Checks that findByHotel includes the room that was just inserted for this hotel.
    @Test
    void findByHotelIncludesCreatedRoom() throws SQLException {
        Room room = newTestRoom();
        roomDAO.create(room);
        createdRoomId = room.getRoomId();

        List<Room> rooms = roomDAO.findByHotel(testHotel.getHotelId());

        boolean found = false;
        for (Room candidate : rooms) {
            if (candidate.getRoomId() == createdRoomId) {
                found = true;
                assertEquals(room.getRoomNumber(), candidate.getRoomNumber());
                assertEquals(testHotel.getHotelId(), candidate.getHotel().getHotelId());
            }
        }
        assertTrue(found);
    }

    // Checks that updateStatus changes the status and findById returns the new value.
    @Test
    void updateStatusChangesRoomStatus() throws SQLException {
        Room room = newTestRoom();
        roomDAO.create(room);
        createdRoomId = room.getRoomId();

        assertTrue(roomDAO.updateStatus(createdRoomId, "BOOKED"));

        Room found = roomDAO.findById(createdRoomId);
        assertNotNull(found);
        assertEquals("BOOKED", found.getStatus());
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

    // room_number is limited to 20 characters, so only part of the UUID is used.
    private Room newTestRoom() {
        Room room = new Room();
        room.setHotel(testHotel);
        room.setRoomNumber("R" + UUID.randomUUID().toString().substring(0, 8));
        room.setRoomType("DELUXE");
        room.setCapacity(2);
        room.setBasePrice(new BigDecimal("1500.00"));
        room.setStatus("AVAILABLE");
        return room;
    }
}
