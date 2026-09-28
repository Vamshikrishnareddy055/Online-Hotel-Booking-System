package com.booking.hotel.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Turns a plain password into a stored "salt:hash" string, and checks a login attempt against it.
// We never store the password itself: if the database is copied, the real passwords are not readable.
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_BITS = 256;

    // A salt is random bytes mixed into the hash so the same password does not always look the same.
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    // Builds "salt:hash". Both parts are Base64, and the whole string is about 70 characters (under 255).
    public static String hashPassword(String plainPassword) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = deriveHash(plainPassword, salt);
        return Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    // Returns false when the stored value is missing or not "salt:hash", instead of throwing.
    public static boolean verifyPassword(String plainPassword, String storedValue) {
        if (storedValue == null) {
            return false;
        }

        String[] parts = storedValue.split(":", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return false;
        }

        byte[] salt;
        byte[] expectedHash;
        try {
            salt = Base64.getDecoder().decode(parts[0]);
            expectedHash = Base64.getDecoder().decode(parts[1]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (salt.length == 0 || expectedHash.length == 0) {
            return false;
        }

        byte[] actualHash = deriveHash(plainPassword, salt);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private static byte[] deriveHash(String plainPassword, byte[] salt) {
        char[] passwordChars = plainPassword.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Could not hash the password with PBKDF2", e);
        } finally {
            spec.clearPassword();
            Arrays.fill(passwordChars, '\0');
        }
    }
}
