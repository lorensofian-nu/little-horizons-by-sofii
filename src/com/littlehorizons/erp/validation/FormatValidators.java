package com.littlehorizons.erp.validation;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Pure format validators and normalizers for domain data.
 *
 * <p>Every method is static, side-effect free and console-independent, so the
 * rules can be unit tested and reused by any front-end. Colombian identity
 * document and phone formats are validated by document type.</p>
 */
public final class FormatValidators {

    /**
     * Amounts: digits only, optional single separator with at most two
     * decimals. Rejects scientific notation, multiple separators and
     * signs, so {@code BigDecimal} never parses a surprise value.
     */
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("\\d{1,9}([.,]\\d{1,2})?");

    /** Automatic receipt format produced by {@code DocumentSequenceUtil}. */
    private static final Pattern RECEIPT_PATTERN = Pattern.compile("REC-\\d{4}-\\d{6}");

    /** Colombian mobile numbers: 10 digits starting with 3. */
    private static final Pattern MOBILE_PHONE_PATTERN = Pattern.compile("3\\d{9}");
    /** Colombian landline numbers: 10 digits starting with the 60x area code. */
    private static final Pattern LANDLINE_PHONE_PATTERN = Pattern.compile("60\\d{8}");

    /** Stricter than the old rule: no consecutive dots and real domain labels. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-z0-9]+(?:[._%+-][a-z0-9]+)*@"
                    + "[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?"
                    + "(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)*"
                    + "\\.[a-z]{2,63}$");

    /** Lowercase name particles kept in lower case when capitalizing. */
    private static final Set<String> NAME_PARTICLES = Set.of(
            "de", "del", "la", "las", "los", "y", "da", "di", "san", "santa", "van", "von");

    private FormatValidators() {
        // Utility class must not be instantiated.
    }

    /** Whether the raw console text is a valid amount for the money readers. */
    public static boolean isAmountText(String raw) {
        return raw != null && AMOUNT_PATTERN.matcher(raw).matches();
    }

    /** Whether the manual receipt number follows the automatic format. */
    public static boolean isValidReceiptNumber(String receiptNumber) {
        return receiptNumber != null && RECEIPT_PATTERN.matcher(receiptNumber).matches();
    }

    /**
     * Validates an identity document number by its type:
     * TI (10-11 digits), CC (6-10 digits), CE / Pasaporte / PPT
     * (5-8 / 5-12 alphanumerics). Repeated digits (1111111111) are rejected.
     */
    public static boolean isValidDocumentNumber(String documentType, String number) {
        if (documentType == null || number == null) {
            return false;
        }
        return switch (documentType) {
            case "TI" -> number.matches("\\d{10,11}") && !allSameCharacter(number);
            case "CC" -> number.matches("\\d{6,10}") && !allSameCharacter(number);
            case "CE" -> number.matches("[a-zA-Z0-9]{5,8}");
            case "Pasaporte", "PPT" -> number.matches("[a-zA-Z0-9]{5,12}");
            default -> false;
        };
    }

    /** Human-readable rule for the console error messages. */
    public static String documentRuleDescription(String documentType) {
        return switch (documentType) {
            case "TI" -> "a TI number has 10 or 11 digits (NUIP)";
            case "CC" -> "a CC number has 6 to 10 digits";
            case "CE" -> "a CE number has 5 to 8 letters or digits";
            case "Pasaporte" -> "a passport number has 5 to 12 letters or digits";
            case "PPT" -> "a PPT number has 5 to 12 letters or digits";
            default -> "the document type is not supported";
        };
    }

    /** Colombian phone: mobile (3xx xxx xxxx) or new-format landline (60x xxx xxxx). */
    public static boolean isValidPhone(String phone) {
        return phone != null
                && (MOBILE_PHONE_PATTERN.matcher(phone).matches()
                || LANDLINE_PHONE_PATTERN.matcher(phone).matches());
    }

    /** Same rule, but null/blank passes because the field is optional. */
    public static boolean isValidOptionalPhone(String phone) {
        return phone == null || phone.isBlank() || isValidPhone(phone);
    }

    /** Strict e-mail rule applied to the normalized (lowercase) value. */
    public static boolean isValidEmail(String email) {
        return email != null && email.length() <= 100
                && EMAIL_PATTERN.matcher(email.toLowerCase(Locale.ROOT)).matches();
    }

    /** Normalizes e-mails: trimmed and lowercase (the DB collation is CI). */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes names: collapses repeated whitespace and capitalizes each
     * word, keeping particles ('de', 'del', 'la', ...) in lower case.
     */
    public static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String collapsed = name.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] words = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i > 0) {
                normalized.append(' ');
            }
            boolean particle = i > 0
                    && NAME_PARTICLES.contains(word.toLowerCase(Locale.ROOT));
            if (particle) {
                normalized.append(word.toLowerCase(Locale.ROOT));
            } else {
                normalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return normalized.toString();
    }

    private static boolean allSameCharacter(String value) {
        for (int i = 1; i < value.length(); i++) {
            if (value.charAt(i) != value.charAt(0)) {
                return false;
            }
        }
        return true;
    }
}
