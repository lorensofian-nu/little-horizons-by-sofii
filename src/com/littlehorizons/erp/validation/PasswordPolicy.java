package com.littlehorizons.erp.validation;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * NIST-style password policy: length over composition, rejection of
 * well-known passwords and of passwords containing the username.
 */
public final class PasswordPolicy {

    /** Small blacklist of the most reused passwords (checked lowercased). */
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "123456", "password", "123456789", "12345678", "1234567890", "qwerty",
            "abc123", "111111", "123123", "123321", "000000", "121212", "iloveyou",
            "qwerty123", "password1", "password123456", "contrasena", "contraseña", "admin", "admin123",
            "administrador", "letmein", "welcome", "monkey", "dragon", "sunshine",
            "sofi12345678", "12345", "1234567", "987654321", "555555", "654321");

    private PasswordPolicy() {
        // Utility class must not be instantiated.
    }

    /**
     * Returns a human-readable violation, or empty when the password is
     * acceptable. The username is forbidden inside the password, and the
     * password must mix at least two character categories.
     */
    public static Optional<String> violation(char[] password, String username) {
        if (password == null || password.length < ValidationRules.MIN_PASSWORD_LENGTH) {
            return Optional.of("the password must contain at least "
                    + ValidationRules.MIN_PASSWORD_LENGTH + " characters");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasOther = false;
        for (char character : password) {
            if (Character.isLetter(character)) {
                hasLetter = true;
            } else if (Character.isDigit(character)) {
                hasDigit = true;
            } else {
                hasOther = true;
            }
        }
        int categories = (hasLetter ? 1 : 0) + (hasDigit ? 1 : 0) + (hasOther ? 1 : 0);
        if (categories < 2) {
            return Optional.of("the password must mix letters with numbers or symbols");
        }
        String lowered = new String(password).toLowerCase(Locale.ROOT);
        if (username != null && !username.isBlank()
                && lowered.contains(username.toLowerCase(Locale.ROOT))) {
            return Optional.of("the password must not contain the username");
        }
        if (COMMON_PASSWORDS.contains(lowered)) {
            return Optional.of("the password is a well-known weak password");
        }
        return Optional.empty();
    }
}
