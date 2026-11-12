package com.littlehorizons.erp.util;

/** Small presentation helpers that keep sensitive identifiers out of output. */
public final class PrivacyUtils {

    private PrivacyUtils() {
        // Utility class must not be instantiated.
    }

    /** Keeps only the last four characters of a document-like identifier. */
    public static String maskIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return "*".repeat(trimmed.length() - 4) + trimmed.substring(trimmed.length() - 4);
    }
}
