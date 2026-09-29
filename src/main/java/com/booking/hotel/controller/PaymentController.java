package com.booking.hotel.controller;

import com.booking.hotel.exception.BookingNotFoundException;
import com.booking.hotel.exception.UnauthorizedException;
import com.booking.hotel.model.Booking;
import com.booking.hotel.model.Payment;
import com.booking.hotel.model.Roles;
import com.booking.hotel.model.User;
import com.booking.hotel.service.BookingService;
import com.booking.hotel.service.PaymentService;
import com.booking.hotel.util.SessionContext;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Sits between Main (console input and output) and PaymentService (business rules).
// A customer can pay only for a booking they made.
public class PaymentController {

    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final SessionContext sessionContext;

    public PaymentController(PaymentService paymentService, BookingService bookingService,
                             SessionContext sessionContext) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.sessionContext = sessionContext;
    }

    // Records a payment after checking that this customer owns the booking.
    public Payment pay(long bookingId, BigDecimal amount, String method) throws SQLException {
        try {
            User current = sessionContext.requireLogin();
            boolean isCustomer = Roles.CUSTOMER.equals(current.getRole());
            boolean isAdmin = Roles.ADMIN.equals(current.getRole());
            if (!isCustomer && !isAdmin) {
                throw new UnauthorizedException("Access denied");
            }
            Booking booking = bookingService.getBookingById(bookingId);
            // A customer pays only for their own stay. An admin may record a payment for any booking.
            if (isCustomer && booking.getUser().getUserId() != current.getUserId()) {
                throw new UnauthorizedException("You can only pay for your own bookings");
            }
            return paymentService.recordPayment(bookingId, amount, method);
        } catch (UnauthorizedException | BookingNotFoundException | SQLException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            throw e;
        }
    }
}
