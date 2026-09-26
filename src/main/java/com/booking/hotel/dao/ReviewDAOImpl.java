package com.booking.hotel.dao;

import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Review;
import com.booking.hotel.model.User;
import com.booking.hotel.util.JdbcUtil;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAOImpl implements ReviewDAO {

    // Records each review-table action. Messages are plain strings.
    private static final Logger logger = Logger.getLogger(ReviewDAOImpl.class.getName());

    private static final String SQL_INSERT_REVIEW =
            "INSERT INTO review (user_id, hotel_id, rating, title, comment) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM review WHERE review_id = ?";

    private static final String SQL_FIND_BY_HOTEL =
            "SELECT * FROM review WHERE hotel_id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM review WHERE review_id = ?";

    // Inserts one row into the review table and stores the new review_id on the Review object.
    @Override
    public boolean create(Review review) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS)) {

            setReviewColumns(statement, review);

            logger.fine("Inserting review for hotel id " + review.getHotel().getHotelId());
            int rows = statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    review.setReviewId(keys.getLong(1));
                }
            }

            if (rows > 0) {
                logger.info("Inserted review id=" + review.getReviewId());
            }
            return rows > 0;
        }
    }

    // Selects the one review row whose review_id matches the given id.
    @Override
    public Review findById(long reviewId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {

            statement.setLong(1, reviewId);

            logger.fine("Selecting review with id " + reviewId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                logger.warning("No review found for id " + reviewId);
                return null;
            }
        }
    }

    // Selects every review row that belongs to the given hotel_id.
    @Override
    public List<Review> findByHotel(long hotelId) throws SQLException {
        List<Review> reviews = new ArrayList<>();

        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_HOTEL)) {

            statement.setLong(1, hotelId);

            logger.fine("Selecting reviews for hotel id " + hotelId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reviews.add(mapRow(resultSet));
                }
            }
        }

        if (reviews.isEmpty()) {
            logger.warning("No reviews found for hotel id " + hotelId);
        }
        return reviews;
    }

    // Deletes the review row whose review_id matches the given id.
    @Override
    public boolean delete(long reviewId) throws SQLException {
        try (Connection connection = JdbcUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setLong(1, reviewId);
            logger.fine("Deleting review with id " + reviewId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                logger.info("Deleted review id=" + reviewId);
            }
            return rows > 0;
        }
    }

    // Copies one review-table row into a Review. user_id and hotel_id are stored on User and Hotel objects.
    private Review mapRow(ResultSet resultSet) throws SQLException {
        Review review = new Review();
        review.setReviewId(resultSet.getLong("review_id"));

        User user = new User();
        user.setUserId(resultSet.getLong("user_id"));
        review.setUser(user);

        Hotel hotel = new Hotel();
        hotel.setHotelId(resultSet.getLong("hotel_id"));
        review.setHotel(hotel);

        review.setRating(resultSet.getInt("rating"));
        review.setTitle(resultSet.getString("title"));
        review.setComment(resultSet.getString("comment"));
        return review;
    }

    private void setReviewColumns(PreparedStatement statement, Review review) throws SQLException {
        statement.setLong(1, review.getUser().getUserId());
        statement.setLong(2, review.getHotel().getHotelId());
        statement.setInt(3, review.getRating());
        statement.setString(4, review.getTitle());
        statement.setString(5, review.getComment());
    }
}
