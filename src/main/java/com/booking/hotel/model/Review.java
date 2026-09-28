package com.booking.hotel.model;

public class Review {

    private long reviewId;
    private User user;   // using FK
    private Hotel hotel;  // using FK
    private int rating;
    private String title;
    private String comment;

    public Review() {
    }

    public Review(long reviewId, User user, Hotel hotel,
                  int rating, String title, String comment) {
        this.reviewId = reviewId;
        this.user = user;
        this.hotel = hotel;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
    }

    public long getReviewId() {
        return reviewId;
    }

    public void setReviewId(long reviewId) {
        this.reviewId = reviewId;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}