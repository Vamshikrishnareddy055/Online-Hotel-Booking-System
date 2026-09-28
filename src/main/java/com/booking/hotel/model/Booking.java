package com.booking.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Booking {

    private long bookingId;
    private User user;  // using Fk
    private Hotel hotel;  // using FK
    private Room room; // USING FK
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int guestsAdults;
    private int guestsChildren;
    private BigDecimal totalAmount;
    private String paymentOption;
    private String bookingStatus;

    public Booking() {
    }

    public Booking(long bookingId, User user, Hotel hotel, Room room,
                   LocalDate checkInDate, LocalDate checkOutDate,
                   int guestsAdults, int guestsChildren,
                   BigDecimal totalAmount, String paymentOption,
                   String bookingStatus) {
        this.bookingId = bookingId;
        this.user = user;
        this.hotel = hotel;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestsAdults = guestsAdults;
        this.guestsChildren = guestsChildren;
        this.totalAmount = totalAmount;
        this.paymentOption = paymentOption;
        this.bookingStatus = bookingStatus;
    }

    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getGuestsAdults() {
        return guestsAdults;
    }

    public void setGuestsAdults(int guestsAdults) {
        this.guestsAdults = guestsAdults;
    }

    public int getGuestsChildren() {
        return guestsChildren;
    }

    public void setGuestsChildren(int guestsChildren) {
        this.guestsChildren = guestsChildren;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(String paymentOption) {
        this.paymentOption = paymentOption;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}
