package com.littlehorizons.erp.util;

/**
 * Utility for building safe LIKE search patterns.
 *
 * <p>User-provided search terms may contain the SQL LIKE wildcards
 * {@code %} and {@code _}; they are escaped so the user input is always
 * matched literally instead of widening the search unexpectedly.</p>
 */
public final class LikePatternUtil {

    private LikePatternUtil() {
        // Utility class must not be instantiated.
    }

    /**
     * Escapes the LIKE wildcards of the term and wraps it with {@code %} so
     * it can be bound directly to a {@code LIKE ?} parameter. The default
     * MySQL escape character (backslash) applies.
     */
    public static String contains(String term) {
        String safe = term == null ? "" : term;
        safe = safe.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + safe + "%";
    }
}
