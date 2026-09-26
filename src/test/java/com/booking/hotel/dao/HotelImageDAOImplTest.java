package com.booking.hotel.dao;

import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.HotelImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Integration test for HotelImageDAOImpl. It needs a running local MySQL database
// with the hotel_booking_system schema already applied, because it calls real JDBC code.
class HotelImageDAOImplTest {

    private HotelDAO hotelDAO;
    private HotelImageDAO imageDAO;

    private Hotel testHotel;

    private long createdHotelId;
    private final List<Long> createdImageIds = new ArrayList<>();

    // Inserts a throwaway hotel so an image can use a real hotel_id.
    @BeforeEach
    void setUp() throws SQLException {
        hotelDAO = new HotelDAOImpl();
        imageDAO = new HotelImageDAOImpl();
        createdHotelId = 0;
        createdImageIds.clear();

        testHotel = newTestHotel();
        hotelDAO.create(testHotel);
        createdHotelId = testHotel.getHotelId();
    }

    // Deletes every test image first, then the hotel. A missing row is ignored.
    @AfterEach
    void deleteTestRows() {
        for (Long imageId : createdImageIds) {
            try {
                if (imageId != null && imageId > 0) {
                    imageDAO.delete(imageId);
                }
            } catch (SQLException ignored) {
                // The image may already be gone. Still delete the remaining images and the hotel.
            }
        }
        try {
            if (createdHotelId > 0) {
                hotelDAO.delete(createdHotelId);
            }
        } catch (SQLException ignored) {
            // The hotel may already be gone.
        }
    }

    // Checks that create inserts an image and MySQL assigns an id.
    @Test
    void createInsertsImageAndSetsGeneratedId() throws SQLException {
        HotelImage image = newTestImage(0, false);

        boolean created = imageDAO.create(image);
        createdImageIds.add(image.getImageId());

        assertTrue(created);
        assertTrue(image.getImageId() > 0);
    }

    // Checks that findById returns the same values that were inserted.
    @Test
    void findByIdReturnsInsertedImage() throws SQLException {
        HotelImage image = insert(newTestImage(1, true));

        HotelImage found = imageDAO.findById(image.getImageId());

        assertNotNull(found);
        assertEquals(image.getImageId(), found.getImageId());
        assertEquals(testHotel.getHotelId(), found.getHotel().getHotelId());
        assertEquals(image.getImageUrl(), found.getImageUrl());
        assertEquals(image.getCaption(), found.getCaption());
        assertEquals(image.isPrimary(), found.isPrimary());
        assertEquals(image.getDisplayOrder(), found.getDisplayOrder());
    }

    // Checks that findByHotel includes the images and returns them in display_order, not insert order.
    @Test
    void findByHotelReturnsImagesOrderedByDisplayOrder() throws SQLException {
        HotelImage third = insert(newTestImage(2, false));
        HotelImage first = insert(newTestImage(0, true));
        HotelImage second = insert(newTestImage(1, false));

        List<HotelImage> images = imageDAO.findByHotel(testHotel.getHotelId());

        assertEquals(3, images.size());
        assertEquals(first.getImageId(), images.get(0).getImageId());
        assertEquals(second.getImageId(), images.get(1).getImageId());
        assertEquals(third.getImageId(), images.get(2).getImageId());
        assertEquals(0, images.get(0).getDisplayOrder());
        assertEquals(1, images.get(1).getDisplayOrder());
        assertEquals(2, images.get(2).getDisplayOrder());
    }

    private HotelImage insert(HotelImage image) throws SQLException {
        imageDAO.create(image);
        createdImageIds.add(image.getImageId());
        return image;
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

    // image_url must be unique so repeated test runs do not collide.
    private HotelImage newTestImage(int displayOrder, boolean primary) {
        HotelImage image = new HotelImage();
        image.setHotel(testHotel);
        image.setImageUrl("https://example.com/images/" + UUID.randomUUID());
        image.setCaption("Room photo " + displayOrder);
        image.setPrimary(primary);
        image.setDisplayOrder(displayOrder);
        return image;
    }
}
