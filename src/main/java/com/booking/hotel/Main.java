package com.booking.hotel;

import com.booking.hotel.controller.AuthController;
import com.booking.hotel.controller.BookingController;
import com.booking.hotel.controller.HotelController;
import com.booking.hotel.controller.PaymentController;
import com.booking.hotel.controller.RoomController;
import com.booking.hotel.controller.UserController;
import com.booking.hotel.dao.BookingDAO;
import com.booking.hotel.dao.BookingDAOImpl;
import com.booking.hotel.dao.HotelDAO;
import com.booking.hotel.dao.HotelDAOImpl;
import com.booking.hotel.dao.PaymentDAO;
import com.booking.hotel.dao.PaymentDAOImpl;
import com.booking.hotel.dao.RoomDAO;
import com.booking.hotel.dao.RoomDAOImpl;
import com.booking.hotel.dao.UserDAO;
import com.booking.hotel.dao.UserDAOImpl;
import com.booking.hotel.model.Booking;
import com.booking.hotel.model.Hotel;
import com.booking.hotel.model.Payment;
import com.booking.hotel.model.Roles;
import com.booking.hotel.model.Room;
import com.booking.hotel.model.User;
import com.booking.hotel.service.AdminSeeder;
import com.booking.hotel.service.AuthService;
import com.booking.hotel.service.BookingService;
import com.booking.hotel.service.HotelService;
import com.booking.hotel.service.PaymentService;
import com.booking.hotel.service.RoomService;
import com.booking.hotel.service.UserService;
import com.booking.hotel.util.SessionContext;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

// Wiring: a DAO talks to MySQL, a service applies the business rules, and a controller calls that service.
// Main only reads the keyboard and prints results. The same DAO object is shared where two services need it.
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final UserDAO userDAO = new UserDAOImpl();
    private static final HotelDAO hotelDAO = new HotelDAOImpl();
    private static final RoomDAO roomDAO = new RoomDAOImpl();
    private static final BookingDAO bookingDAO = new BookingDAOImpl();
    private static final PaymentDAO paymentDAO = new PaymentDAOImpl();

    private static final UserService userService = new UserService(userDAO);
    private static final AuthService authService = new AuthService(userDAO);
    private static final HotelService hotelService = new HotelService(hotelDAO);
    private static final RoomService roomService = new RoomService(roomDAO, hotelDAO);
    private static final BookingService bookingService = new BookingService(roomDAO, bookingDAO);
    private static final PaymentService paymentService = new PaymentService(paymentDAO, bookingDAO);

    // One session for this console run. The same object is shared by every controller.
    private static final SessionContext sessionContext = new SessionContext();

    private static final AuthController authController = new AuthController(authService, sessionContext);
    private static final UserController userController = new UserController(userService, sessionContext);
    private static final HotelController hotelController = new HotelController(hotelService, sessionContext);
    private static final RoomController roomController = new RoomController(roomService, sessionContext);
    private static final BookingController bookingController = new BookingController(bookingService, sessionContext);
    private static final PaymentController paymentController =
            new PaymentController(paymentService, bookingService, sessionContext);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        // Make sure the default admin exists, then open the menu even if that database step fails.
        try {
            new AdminSeeder(userDAO).seedDefaultAdmin();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Default admin was not created", e);
        }

        while (running) {
            try {
                if (!sessionContext.isLoggedIn()) {
                    running = guestMenu(scanner);
                } else if (Roles.ADMIN.equals(sessionContext.getCurrentUser().getRole())) {
                    adminMenu(scanner);
                } else {
                    customerMenu(scanner);
                }
            } catch (RuntimeException | SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Goodbye.");
    }

    // Shown before anyone logs in. Register stays here so a new customer can create an account.
    private static boolean guestMenu(Scanner scanner) throws SQLException {
        System.out.println();
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> login(scanner);
            case "2" -> registerUser(scanner);
            case "0" -> {
                return false;
            }
            default -> System.out.println("Choose a number from the menu.");
        }
        return true;
    }

    // A customer can search, book, pay, and cancel their own stay.
    private static void customerMenu(Scanner scanner) throws SQLException {
        User current = sessionContext.getCurrentUser();
        System.out.println();
        System.out.println("Logged in as " + current.getFullName() + " (" + current.getRole() + ")");
        System.out.println("1. Search hotels");
        System.out.println("2. Book a room");
        System.out.println("3. Record a payment");
        System.out.println("4. My bookings");
        System.out.println("5. Cancel a booking");
        System.out.println("0. Logout");
        System.out.print("Choose an option: ");
        switch (scanner.nextLine().trim()) {
            case "1" -> searchHotels(scanner);
            case "2" -> bookRoom(scanner);
            case "3" -> recordPayment(scanner);
            case "4" -> viewMyBookings();
            case "5" -> cancelBooking(scanner);
            case "0" -> logout();
            default -> System.out.println("Choose a number from the menu.");
        }
    }

    // An admin can do the customer actions plus hotel, room, user, and all-booking management.
    private static void adminMenu(Scanner scanner) throws SQLException {
        User current = sessionContext.getCurrentUser();
        System.out.println();
        System.out.println("Logged in as " + current.getFullName() + " (" + current.getRole() + ")");
        System.out.println("1. Search hotels");
        System.out.println("2. Book a room");
        System.out.println("3. Record a payment");
        System.out.println("4. Cancel a booking");
        System.out.println("5. Add hotel");
        System.out.println("6. Add room");
        System.out.println("7. All bookings");
        System.out.println("8. All users");
        System.out.println("0. Logout");
        System.out.print("Choose an option: ");
        switch (scanner.nextLine().trim()) {
            case "1" -> searchHotels(scanner);
            case "2" -> bookRoom(scanner);
            case "3" -> recordPayment(scanner);
            case "4" -> cancelBooking(scanner);
            case "5" -> addHotel(scanner);
            case "6" -> addRoom(scanner);
            case "7" -> viewAllBookings();
            case "8" -> listUsers();
            case "0" -> logout();
            default -> System.out.println("Choose a number from the menu.");
        }
    }

    private static void login(Scanner scanner) throws SQLException {
        String email = readLine(scanner, "Email: ");
        String password = readLine(scanner, "Password: ");
        User user = authController.login(email, password);
        System.out.println("Welcome, " + user.getFullName() + ".");
    }

    private static void logout() {
        sessionContext.logout();
        System.out.println("Logged out.");
    }

    private static void registerUser(Scanner scanner) throws SQLException {
        String fullName = readLine(scanner, "Full name: ");
        String email = readLine(scanner, "Email: ");
        String password = readLine(scanner, "Password: ");
        String phone = readLine(scanner, "Phone: ");

        User user = userController.register(fullName, email, password, phone);
        System.out.println("Registered user id=" + user.getUserId()
                + ", name=" + user.getFullName()
                + ", email=" + user.getEmail()
                + ", role=" + user.getRole()
                + ", status=" + user.getStatus());
    }

    private static void addHotel(Scanner scanner) throws SQLException {
        String name = readLine(scanner, "Hotel name: ");
        String description = readLine(scanner, "Description: ");
        String address = readLine(scanner, "Address: ");
        String city = readLine(scanner, "City: ");
        String state = readLine(scanner, "State: ");
        String country = readLine(scanner, "Country: ");
        BigDecimal starRating = new BigDecimal(readLine(scanner, "Star rating: "));
        String amenities = readLine(scanner, "Amenities: ");

        Hotel hotel = hotelController.addHotel(name, description, address, city, state, country, starRating, amenities);
        System.out.println("Added hotel id=" + hotel.getHotelId()
                + ", name=" + hotel.getName()
                + ", city=" + hotel.getCity()
                + ", status=" + hotel.getStatus());
    }

    private static void addRoom(Scanner scanner) throws SQLException {
        long hotelId = Long.parseLong(readLine(scanner, "Hotel ID: "));
        String roomNumber = readLine(scanner, "Room number: ");
        String roomType = readLine(scanner, "Room type: ");
        int capacity = Integer.parseInt(readLine(scanner, "Capacity: "));
        BigDecimal basePrice = new BigDecimal(readLine(scanner, "Base price: "));

        Room room = roomController.addRoom(hotelId, roomNumber, roomType, capacity, basePrice);
        System.out.println("Added room id=" + room.getRoomId()
                + ", number=" + room.getRoomNumber()
                + ", type=" + room.getRoomType()
                + ", price=" + room.getBasePrice()
                + ", status=" + room.getStatus());
    }

    private static void searchHotels(Scanner scanner) throws SQLException {
        String city = readLine(scanner, "City: ");
        List<Hotel> hotels = hotelController.searchByCity(city);
        if (hotels.isEmpty()) {
            System.out.println("No hotels found in " + city + ".");
            return;
        }
        for (Hotel hotel : hotels) {
            System.out.println("Hotel id=" + hotel.getHotelId()
                    + ", name=" + hotel.getName()
                    + ", city=" + hotel.getCity()
                    + ", stars=" + hotel.getStarRating()
                    + ", status=" + hotel.getStatus());
            List<Room> rooms = roomController.getRoomsByHotel(hotel.getHotelId());
            if (rooms.isEmpty()) {
                System.out.println("  No rooms.");
                continue;
            }
            for (Room room : rooms) {
                System.out.println("  Room id=" + room.getRoomId()
                        + ", number=" + room.getRoomNumber()
                        + ", type=" + room.getRoomType()
                        + ", price=" + room.getBasePrice()
                        + ", status=" + room.getStatus());
            }
        }
    }

    private static void viewMyBookings() throws SQLException {
        List<Booking> bookings = bookingController.viewMyBookings();
        printBookings(bookings);
    }

    private static void viewAllBookings() throws SQLException {
        List<Booking> bookings = bookingController.viewAllBookings();
        printBookings(bookings);
    }

    private static void printBookings(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        for (Booking booking : bookings) {
            System.out.println("Booking id=" + booking.getBookingId()
                    + ", user id=" + booking.getUser().getUserId()
                    + ", hotel id=" + booking.getHotel().getHotelId()
                    + ", room id=" + booking.getRoom().getRoomId()
                    + ", check-in=" + booking.getCheckInDate()
                    + ", check-out=" + booking.getCheckOutDate()
                    + ", total=" + booking.getTotalAmount()
                    + ", status=" + booking.getBookingStatus());
        }
    }

    private static void listUsers() throws SQLException {
        List<User> users = userController.listUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        for (User user : users) {
            System.out.println("User id=" + user.getUserId()
                    + ", name=" + user.getFullName()
                    + ", email=" + user.getEmail()
                    + ", role=" + user.getRole()
                    + ", status=" + user.getStatus());
        }
    }

    private static void bookRoom(Scanner scanner) throws SQLException {
        long hotelId = Long.parseLong(readLine(scanner, "Hotel ID: "));
        long roomId = Long.parseLong(readLine(scanner, "Room ID: "));
        LocalDate checkIn = LocalDate.parse(readLine(scanner, "Check-in date (YYYY-MM-DD): "));
        LocalDate checkOut = LocalDate.parse(readLine(scanner, "Check-out date (YYYY-MM-DD): "));
        int adults = Integer.parseInt(readLine(scanner, "Adults: "));
        int children = Integer.parseInt(readLine(scanner, "Children: "));
        String paymentOption = readLine(scanner, "Payment option: ");

        Booking booking = bookingController.bookRoom(
                hotelId, roomId, checkIn, checkOut, adults, children, paymentOption);
        System.out.println("Booked id=" + booking.getBookingId()
                + ", check-in=" + booking.getCheckInDate()
                + ", check-out=" + booking.getCheckOutDate()
                + ", total=" + booking.getTotalAmount()
                + ", status=" + booking.getBookingStatus());
    }

    private static void recordPayment(Scanner scanner) throws SQLException {
        long bookingId = Long.parseLong(readLine(scanner, "Booking ID: "));
        BigDecimal amount = new BigDecimal(readLine(scanner, "Amount: "));
        String method = readLine(scanner, "Payment method: ");

        Payment payment = paymentController.pay(bookingId, amount, method);
        System.out.println("Payment id=" + payment.getPaymentId()
                + ", booking id=" + payment.getBooking().getBookingId()
                + ", amount=" + payment.getAmount()
                + ", method=" + payment.getPaymentMethod()
                + ", status=" + payment.getPaymentStatus()
                + ", ref=" + payment.getTransactionRef());
    }

    private static void cancelBooking(Scanner scanner) throws SQLException {
        long bookingId = Long.parseLong(readLine(scanner, "Booking ID: "));
        bookingController.cancelBooking(bookingId);
        System.out.println("Booking " + bookingId + " cancelle1d.");
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
