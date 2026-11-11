package com.littlehorizons.erp.util;

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

    /** Reads a non-empty text value, retrying until valid. */
    public static String readRequiredText(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  [!] This value is required. Please try again.");
        }
    }

    /** Reads an optional text value: empty input is accepted and returned as the fallback. */
    public static String readOptionalText(String prompt, String fallback) {
        String value = readLine(prompt);
        return value.isEmpty() ? fallback : value;
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
        String raw = readLine(prompt + " (yyyy-MM-dd, empty = " + fallback + "): ");
        if (raw.isEmpty()) {
            return fallback;
        }
        try {
            return LocalDate.parse(raw, DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            System.out.println("  [!] Invalid date, using " + fallback + ".");
            return fallback;
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
