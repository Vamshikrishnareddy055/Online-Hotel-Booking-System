package com.booking.hotel.service;

import com.booking.hotel.dao.HotelDAO;
import com.booking.hotel.dao.RoomDAO;
import com.booking.hotel.exception.HotelNotFoundException;
import com.booking.hotel.exception.InvalidBookingException;
import com.booking.hotel.exception.RoomNotFoundException;
import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Room;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class RoomService {

    private static final Logger logger = Logger.getLogger(RoomService.class.getName());

    private final RoomDAO roomDAO;
    private final HotelDAO hotelDAO;

    // Receives the DAOs from outside so this class can be tested with fake DAOs later.
    public RoomService(RoomDAO roomDAO, HotelDAO hotelDAO) {
        this.roomDAO = roomDAO;
        this.hotelDAO = hotelDAO;
    }

    // Saves a new available room for an existing hotel. The price must be greater than zero.
    public Room addRoom(long hotelId, String roomNumber, String roomType, int capacity, BigDecimal basePrice)
            throws SQLException {
        Hotel hotel = hotelDAO.findById(hotelId);
        if (hotel == null) {
            logger.warning("No hotel found with id " + hotelId);
            throw new HotelNotFoundException("No hotel found with id " + hotelId);
        }

        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBookingException("Room price must be greater than zero");
        }

        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setBasePrice(basePrice);
        room.setStatus("AVAILABLE");

        boolean created = roomDAO.create(room);
        if (created) {
            logger.info("Added room id=" + room.getRoomId() + " for hotel id=" + hotelId);
        }
        return room;
    }

    // Returns every room that belongs to this hotel. An empty list means none were found.
    public List<Room> getRoomsByHotel(long hotelId) throws SQLException {
        return roomDAO.findByHotel(hotelId);
    }

    // Loads one room by id. Throws RoomNotFoundException when that id is not in the database.
    public Room getRoomById(long roomId) throws SQLException {
        Room room = roomDAO.findById(roomId);
        if (room == null) {
            logger.warning("No room found with id " + roomId);
            throw new RoomNotFoundException("No room found with id " + roomId);
        }
        return room;
    }
}
