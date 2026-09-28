package com.booking.hotel.model;

// Holds the allowed user roles so the rest of the code does not rely on raw strings that can be mistyped.
public final class Roles {

    public static final String ADMIN = "ADMIN";
    public static final String CUSTOMER = "CUSTOMER";

    private Roles() {
    }
}
