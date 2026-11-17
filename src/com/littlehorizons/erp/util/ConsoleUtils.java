package com.littlehorizons.erp.util;

import com.littlehorizons.erp.validation.FormatValidators;
import com.littlehorizons.erp.validation.ValidationRules;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Console input helpers with full validation and retry loops.
 *
 * <p>Every method keeps asking until the user types a valid value, showing a
 * clear message for invalid input. {@code 0} always means "cancel / go back"
 * for numeric selections.</p>
 */
public final class ConsoleUtils {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private ConsoleUtils() {
        // Utility class must not be instantiated.
    }

    /** Reads a line of text, trimming spaces. Never returns null. */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    /**
     * Reads a text value accepted by the given rule, retrying until valid.
     * The rule must be a pure validation predicate (see
     * {@code com.littlehorizons.erp.validation.FormatValidators}).
     */
    public static String readValidatedText(String prompt,
                                           java.util.function.Predicate<String> rule,
                                           String errorMessage) {
        while (true) {
            String value = readLine(prompt);
            if (rule.test(value)) {
                return value;
            }
            System.out.println("  [!] " + errorMessage);
        }
    }

    /**
     * Reads a password without echo when the process has an attached console.
     * Without a console (e.g. piped stdin) it tries the POSIX {@code stty
     * -echo} trick before falling back to a visible read, so the password is
     * only shown when hiding it is technically impossible.
     */
    public static char[] readSecret(String prompt) {
        if (System.console() != null) {
            char[] value = System.console().readPassword(prompt);
            return value == null ? new char[0] : value;
        }
        if (tryEnableSttyEchoOff()) {
            try {
                System.out.print(prompt);
                return SCANNER.nextLine().trim().toCharArray();
            } catch (java.util.NoSuchElementException exception) {
                return new char[0];
            } finally {
                tryEnableSttyEchoOn();
            }
        }
        System.out.println("  [!] WARNING: no hidden input available; the password "
                + "will be visible on screen.");
        return readLine(prompt).toCharArray();
    }

    /**
     * Disables terminal echo through {@code stty} when stdin is a terminal.
     * Returns {@code false} when the trick is unavailable (not a terminal).
     */
    private static boolean tryEnableSttyEchoOff() {
        return runStty("stty -echo");
    }

    /** Re-enables terminal echo after a {@code stty -echo} read. */
    private static void tryEnableSttyEchoOn() {
        runStty("stty echo");
    }

    private static boolean runStty(String command) {
        try {
            Process process = new ProcessBuilder("/bin/sh", "-c", command)
                    .inheritIO()
                    .start();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException | RuntimeException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    /** Reads a non-empty text value, retrying until valid. */
    public static String readRequiredText(String prompt) {
        return readRequiredText(prompt, 255);
    }

    /** Reads a non-empty text value with a database-safe maximum length. */
    public static String readRequiredText(String prompt, int maxLength) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty() && value.length() <= maxLength) {
                return value;
            }
            System.out.println(value.isEmpty()
                    ? "  [!] This value is required. Please try again."
                    : "  [!] Maximum length is " + maxLength + " characters.");
        }
    }

    /** Reads an optional text value: empty input is accepted and returned as the fallback. */
    public static String readOptionalText(String prompt, String fallback) {
        String value = readLine(prompt);
        return value.isEmpty() ? fallback : value;
    }

    public static String readRequiredText(String prompt, int minLength, int maxLength,
                                          String regex, String error) {
        while (true) {
            String value = readLine(prompt);
            if (value.length() >= minLength && value.length() <= maxLength
                    && value.matches(regex)) {
                return value;
            }
            System.out.println("  [!] " + error);
        }
    }

    /** Reads an optional email with a basic syntax and length check. */
    public static String readOptionalEmail(String prompt, String fallback) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                return FormatValidators.normalizeEmail(fallback);
            }
            String normalized = FormatValidators.normalizeEmail(value);
            if (FormatValidators.isValidEmail(normalized)) {
                return normalized;
            }
            System.out.println("  [!] Enter a valid email address or leave it empty.");
        }
    }

    /** Reads an integer between min and max (inclusive); {@code cancelValue} returns early. */
    public static int readInt(String prompt, int min, int max, int cancelValue) {
        while (true) {
            String raw = readLine(prompt);
            try {
                int value = Integer.parseInt(raw);
                if (value == cancelValue) {
                    return value;
                }
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("  [!] Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException exception) {
                System.out.println("  [!] Invalid number. Please try again.");
            }
        }
    }

    /** Reads a menu option (1..maxOptions), 0 goes back. */
    public static int readMenuOption(String prompt, int maxOptions) {
        return readInt(prompt, 1, maxOptions, 0);
    }

    /** Reads a decimal between min and max (inclusive). */
    public static double readDouble(String prompt, double min, double max) {
        while (true) {
            String raw = readLine(prompt);
            try {
                double value = Double.parseDouble(raw.replace(',', '.'));
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("  [!] Enter a value between " + min + " and " + max + ".");
            } catch (NumberFormatException exception) {
                System.out.println("  [!] Invalid decimal number. Please try again.");
            }
        }
    }

    /** Reads a monetary amount (>= 0) in Colombian pesos. */
    public static double readMoney(String prompt) {
        return readDouble(prompt, 0.0, Double.MAX_VALUE);
    }

    /**
     * Reads a monetary amount as an exact {@link BigDecimal} with two
     * decimals, avoiding the precision loss of intermediate {@code double}
     * values. Range: 0 .. 99,999,999.99.
     */
    public static BigDecimal readBigDecimalAmount(String prompt) {
        return parseBigDecimalAmount(prompt, null);
    }

    /**
     * Reads an optional monetary amount as an exact {@link BigDecimal};
     * empty input returns the supplied fallback value unchanged.
     */
    public static BigDecimal readOptionalBigDecimalAmount(String prompt, BigDecimal fallback) {
        return parseBigDecimalAmount(prompt, fallback);
    }

    /** Shared parser for the exact-amount readers; never returns null. */
    private static BigDecimal parseBigDecimalAmount(String prompt, BigDecimal fallback) {
        while (true) {
            String raw = readLine(prompt);
            if (raw.isEmpty() && fallback != null) {
                return fallback;
            }
            // Strict pattern: digits with an optional single two-decimal
            // separator, no scientific notation and no signs.
            if (FormatValidators.isAmountText(raw)) {
                BigDecimal value = new BigDecimal(raw.replace(',', '.'))
                        .setScale(2, RoundingMode.HALF_UP);
                if (value.compareTo(BigDecimal.ZERO) >= 0
                        && value.compareTo(ValidationRules.MAX_MONEY) <= 0) {
                    return value;
                }
            }
            System.out.println("  [!] Enter a valid amount between 0 and 99,999,999.99 "
                    + "(plain digits, optional 2 decimals).");
        }
    }

    /** Reads a monetary value; empty input returns the supplied fallback. */
    public static double readOptionalMoney(String prompt, double fallback) {
        while (true) {
            String raw = readLine(prompt);
            if (raw.isEmpty()) {
                return fallback;
            }
            try {
                double value = Double.parseDouble(raw.replace(',', '.'));
                if (Double.isFinite(value) && value >= 0.0 && value <= 99_999_999.99) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // The retry message below is intentionally generic.
            }
            System.out.println("  [!] Enter a valid amount between 0 and 99,999,999.99.");
        }
    }

    /** Reads a date in ISO format (yyyy-MM-dd). */
    public static LocalDate readDate(String prompt) {
        while (true) {
            String raw = readLine(prompt + " (yyyy-MM-dd): ");
            try {
                return LocalDate.parse(raw, DATE_FORMAT);
            } catch (DateTimeParseException exception) {
                System.out.println("  [!] Invalid date. Use the format yyyy-MM-dd (e.g. 2026-03-15).");
            }
        }
    }

    /** Reads a date in ISO format; empty input returns the fallback date. */
    public static LocalDate readOptionalDate(String prompt, LocalDate fallback) {
        while (true) {
            String raw = readLine(prompt + " (yyyy-MM-dd, empty = keep current): ");
            if (raw.isEmpty()) {
                return fallback;
            }
            try {
                return LocalDate.parse(raw, DATE_FORMAT);
            } catch (DateTimeParseException exception) {
                System.out.println("  [!] Invalid date. Please try again.");
            }
        }
    }

    /** Reads one of the allowed values (case-insensitive); shows them in the prompt. */
    public static String readOption(String prompt, List<String> options) {
        while (true) {
            String raw = readLine(prompt + " " + options + ": ");
            String match = options.stream()
                    .filter(option -> option.equalsIgnoreCase(raw))
                    .findFirst()
                    .orElse(null);
            if (match != null) {
                return match;
            }
            System.out.println("  [!] Invalid option. Choose one of: " + options);
        }
    }

    /** Reads a yes/no confirmation. */
    public static boolean confirm(String prompt) {
        while (true) {
            String raw = readLine(prompt + " (y/n): ").toLowerCase();
            if (raw.equals("y") || raw.equals("yes")) {
                return true;
            }
            if (raw.equals("n") || raw.equals("no")) {
                return false;
            }
            System.out.println("  [!] Please answer y (yes) or n (no).");
        }
    }

    /** Prints a boxed section header. */
    public static void printHeader(String title) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.printf("║ %-52s ║%n", title);
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    /** Prints a success message. */
    public static void printSuccess(String message) {
        System.out.println("  [OK] " + message);
    }

    /** Prints an error message. */
    public static void printError(String message) {
        System.out.println("  [X] " + message);
    }

    /** Pauses until the user presses Enter. */
    public static void pause() {
        readLine("  Press ENTER to continue...");
    }
}
