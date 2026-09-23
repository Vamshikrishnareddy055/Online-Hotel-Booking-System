CREATE DATABASE IF NOT EXISTS hotel_booking_system;
USE hotel_booking_system;

CREATE TABLE `user` (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE location (
    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    parent_id BIGINT,
    CONSTRAINT fk_location_parent
        FOREIGN KEY (parent_id) REFERENCES location (location_id)
);

CREATE TABLE hotel (
    hotel_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    location_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    star_rating DECIMAL(2, 1),
    amenities TEXT,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_hotel_location
        FOREIGN KEY (location_id) REFERENCES location (location_id)
);

CREATE TABLE room (
    room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    base_price DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_room_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotel (hotel_id)
);

CREATE TABLE booking (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guests_adults INT NOT NULL,
    guests_children INT DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,
    payment_option VARCHAR(20) NOT NULL,
    booking_status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    CONSTRAINT fk_booking_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotel (hotel_id),
    CONSTRAINT fk_booking_room
        FOREIGN KEY (room_id) REFERENCES room (room_id)
);

CREATE TABLE payment (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    transaction_ref VARCHAR(100) UNIQUE,
    paid_at TIMESTAMP NULL,
    CONSTRAINT fk_payment_booking
        FOREIGN KEY (booking_id) REFERENCES booking (booking_id)
);

CREATE TABLE review (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hotel_id BIGINT NOT NULL,
    rating INT NOT NULL,
    title VARCHAR(200),
    comment TEXT,
    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    CONSTRAINT fk_review_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotel (hotel_id)
);

CREATE TABLE hotel_image (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    caption VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    CONSTRAINT fk_hotel_image_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotel (hotel_id)
);
