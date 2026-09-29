package com.booking.hotel.util;

import com.booking.hotel.exception.UnauthorizedException;
import com.booking.hotel.model.User;

import java.util.Objects;
import java.util.logging.Logger;

// Remembers who is logged in for this console run. Each SessionContext has its own user.
public class SessionContext {

    private static final Logger logger = Logger.getLogger(SessionContext.class.getName());

    // Null means nobody is logged in yet.
    private User currentUser;

    // Stores the user who just logged in.
    public void login(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        this.currentUser = user;
    }

    // Forgets the logged-in user so the next action must log in again.
    public void logout() {
        if (currentUser != null) {
            logger.info("User logged out id=" + currentUser.getUserId());
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Returns the logged-in user, or stops the action when nobody has logged in.
    public User requireLogin() {
        if (currentUser == null) {
            logger.warning("Please log in first");
            throw new UnauthorizedException("Please log in first");
        }
        return currentUser;
    }

    // Stops the action when the logged-in user's role is not the one this action needs.
    public void requireRole(String role) {
        User user = requireLogin();
        if (!Objects.equals(user.getRole(), role)) {
            logger.warning("Access denied for user id=" + user.getUserId() + ", required role=" + role);
            throw new UnauthorizedException("Access denied: " + role + " role required");
        }
    }

    // True only when someone is logged in and their role is the one asked for. Does not throw.
    public boolean hasRole(String role) {
        return currentUser != null && Objects.equals(currentUser.getRole(), role);
    }
}
