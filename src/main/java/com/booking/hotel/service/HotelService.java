package com.booking.hotel.service;

import com.booking.hotel.dao.HotelDAO;
import com.booking.hotel.exception.HotelNotFoundException;
import com.booking.hotel.exception.InvalidBookingException;
import com.booking.hotel.model.Hotel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class HotelService {

    private static final Logger logger = Logger.getLogger(HotelService.class.getName());

    private final HotelDAO hotelDAO;

    // Receives the DAO from outside so this class can be tested with a fake DAO later.
    public HotelService(HotelDAO hotelDAO) {
        this.hotelDAO = hotelDAO;
    }

    // Saves a new active hotel. Name and city are required. Location is left empty for now.
    public Hotel addHotel(String name, String description, String address, String city,
                          String state, String country, BigDecimal starRating, String amenities)
            throws SQLException {
        if (name == null || name.isBlank() || city == null || city.isBlank()) {
            throw new InvalidBookingException("Hotel name and city are required");
        }

        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setDescription(description);
        hotel.setAddress(address);
        hotel.setCity(city);
        hotel.setState(state);
        hotel.setCountry(country);
        hotel.setStarRating(starRating);
        hotel.setAmenities(amenities);
        hotel.setStatus("ACTIVE");

        boolean created = hotelDAO.create(hotel);
        if (created) {
            logger.info("Added hotel id=" + hotel.getHotelId());
        }
        return hotel;
    }

    // Loads one hotel by id. Throws HotelNotFoundException when that id is not in the database.
    public Hotel getHotelById(long hotelId) throws SQLException {
        Hotel hotel = hotelDAO.findById(hotelId);
        if (hotel == null) {
            logger.warning("No hotel found with id " + hotelId);
            throw new HotelNotFoundException("No hotel found with id " + hotelId);
        }
        return hotel;
    }

    // Returns every hotel in the given city. An empty list means none were found.
    public List<Hotel> searchHotelsByCity(String city) throws SQLException {
        return hotelDAO.findByCity(city);
    }
}
