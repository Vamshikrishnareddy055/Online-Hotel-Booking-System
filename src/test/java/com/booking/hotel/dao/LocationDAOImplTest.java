package com.booking.hotel.dao;

import com.booking.hotel.model.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Integration test for LocationDAOImpl. It needs a running local MySQL database
// with the hotel_booking_system schema already applied, because it calls real JDBC code.
class LocationDAOImplTest {

    private LocationDAO locationDAO;

    private long createdCountryId;
    private long createdStateId;
    private long createdCityId;

    // Builds a fresh DAO before each test. Ids stay 0 until a test inserts a row.
    @BeforeEach
    void setUp() {
        locationDAO = new LocationDAOImpl();
        createdCountryId = 0;
        createdStateId = 0;
        createdCityId = 0;
    }

    // Deletes the city first, then the state, then the country, so parent rows are removed last.
    @AfterEach
    void deleteTestLocations() {
        deleteQuietly(createdCityId);
        deleteQuietly(createdStateId);
        deleteQuietly(createdCountryId);
    }

    // Checks that a country with no parent is inserted and MySQL assigns an id.
    @Test
    void createCountryWithNoParentSetsGeneratedId() throws SQLException {
        Location country = newLocation("Country", "COUNTRY", null);

        boolean created = locationDAO.create(country);
        createdCountryId = country.getLocationId();

        assertTrue(created);
        assertTrue(createdCountryId > 0);
    }

    // Checks that a state can point at a country and a city can point at that state.
    @Test
    void createStateAndCityWithParentIds() throws SQLException {
        Location country = insert(newLocation("Country", "COUNTRY", null));
        Location state = insert(newLocation("State", "STATE", country));
        Location city = insert(newLocation("City", "CITY", state));

        assertTrue(state.getLocationId() > 0);
        assertTrue(city.getLocationId() > 0);
        assertEquals(country.getLocationId(), state.getParent().getLocationId());
        assertEquals(state.getLocationId(), city.getParent().getLocationId());
    }

    // Checks that the city path is "City, State, Country", walking parents from the city upward.
    @Test
    void resolveFullPathJoinsCityStateAndCountry() throws SQLException {
        String suffix = UUID.randomUUID().toString();
        String countryName = "Country-" + suffix;
        String stateName = "State-" + suffix;
        String cityName = "City-" + suffix;

        Location country = insert(newLocation(countryName, "COUNTRY", null));
        Location state = insert(newLocation(stateName, "STATE", country));
        Location city = insert(newLocation(cityName, "CITY", state));

        String path = locationDAO.resolveFullPath(city.getLocationId());

        assertEquals(cityName + ", " + stateName + ", " + countryName, path);
    }

    // Checks that findById returns the name, type, and parent id saved for each level.
    @Test
    void findByIdReturnsEachLevel() throws SQLException {
        Location country = insert(newLocation("Country", "COUNTRY", null));
        Location state = insert(newLocation("State", "STATE", country));
        Location city = insert(newLocation("City", "CITY", state));

        Location foundCountry = locationDAO.findById(country.getLocationId());
        Location foundState = locationDAO.findById(state.getLocationId());
        Location foundCity = locationDAO.findById(city.getLocationId());

        assertNotNull(foundCountry);
        assertEquals(country.getName(), foundCountry.getName());
        assertEquals("COUNTRY", foundCountry.getType());
        assertNull(foundCountry.getParent());

        assertNotNull(foundState);
        assertEquals(state.getName(), foundState.getName());
        assertEquals("STATE", foundState.getType());
        assertEquals(country.getLocationId(), foundState.getParent().getLocationId());

        assertNotNull(foundCity);
        assertEquals(city.getName(), foundCity.getName());
        assertEquals("CITY", foundCity.getType());
        assertEquals(state.getLocationId(), foundCity.getParent().getLocationId());
    }

    private void deleteQuietly(long locationId) {
        if (locationId <= 0) {
            return;
        }
        try {
            locationDAO.delete(locationId);
        } catch (SQLException ignored) {
            // The row may already be gone. Still delete the parent locations after this one.
        }
    }

    // Records the new id so @AfterEach can delete this row even if a later assertion fails.
    private Location insert(Location location) throws SQLException {
        locationDAO.create(location);
        if ("CITY".equals(location.getType())) {
            createdCityId = location.getLocationId();
        } else if ("STATE".equals(location.getType())) {
            createdStateId = location.getLocationId();
        } else if ("COUNTRY".equals(location.getType())) {
            createdCountryId = location.getLocationId();
        }
        return location;
    }

    private Location newLocation(String namePrefix, String type, Location parent) {
        String name = namePrefix.contains("-") ? namePrefix : namePrefix + "-" + UUID.randomUUID();
        return new Location(0, name, type, parent);
    }
}
