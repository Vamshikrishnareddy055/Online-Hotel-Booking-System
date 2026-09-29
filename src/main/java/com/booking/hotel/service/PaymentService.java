package com.booking.hotel.service;

import com.booking.hotel.dao.BookingDAO;
import com.booking.hotel.dao.PaymentDAO;
import com.booking.hotel.exception.BookingNotFoundException;
import com.booking.hotel.model.Booking;
import com.booking.hotel.model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

public class PaymentService {

    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    private final PaymentDAO paymentDAO;
    private final BookingDAO bookingDAO;

    // Receives the DAOs from outside so this class can be tested with fake DAOs later.
    public PaymentService(PaymentDAO paymentDAO, BookingDAO bookingDAO) {
        this.paymentDAO = paymentDAO;
        this.bookingDAO = bookingDAO;
    }

    // Records a successful payment for an existing booking and returns it with the generated id.
    public Payment recordPayment(long bookingId, BigDecimal amount, String method) throws SQLException {
        // A payment can only be recorded for a booking that exists.
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null) {
            logger.warning("No booking found with id " + bookingId);
            throw new BookingNotFoundException("No booking found with id " + bookingId);
        }

        // The reference is short and uppercase so it is easy to read on a receipt.
        String transactionRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionRef(transactionRef);
        payment.setPaidAt(LocalDateTime.now());

        boolean created = paymentDAO.create(payment);
        if (created) {
            logger.info("Recorded payment id=" + payment.getPaymentId() + " for booking id=" + bookingId);
        }
        return payment;
    }
}
