package com.littlehorizons.erp.model;

import java.util.Arrays;
import java.util.List;

/**
 * Guardian document types. Enum values match the database ENUM column
 * {@code acudientes.tipo_documento} ('CC', 'CE', 'Pasaporte', 'PPT').
 */
public enum GuardianDocumentType {

    CC("CC", "Cédula de Ciudadanía"),
    CE("CE", "Cédula de Extranjería"),
    PASSPORT("Pasaporte", "Pasaporte"),
    PPT("PPT", "PPT");

    private final String databaseValue;
    private final String displayValue;

    GuardianDocumentType(String databaseValue, String displayValue) {
        this.databaseValue = databaseValue;
        this.displayValue = displayValue;
    }

    /** Value stored in the database column. */
    public String getDatabaseValue() {
        return databaseValue;
    }

    /** Human-readable value used by the console. */
    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }

    /** All human-readable values, useful for console prompts. */
    public static List<String> displayValues() {
        return Arrays.stream(values()).map(GuardianDocumentType::getDisplayValue).toList();
    }

    /** Maps a database value to the enum, defaulting to CC. */
    public static GuardianDocumentType fromDb(String value) {
        return Arrays.stream(values())
                .filter(type -> type.databaseValue.equalsIgnoreCase(value)
                        || type.displayValue.equalsIgnoreCase(value))
                .findFirst()
                .orElse(CC);
    }

    /** Maps user input to the enum; returns null when the value is invalid. */
    public static GuardianDocumentType fromDisplayValue(String value) {
        String normalized = normalize(value);
        return Arrays.stream(values())
                .filter(type -> normalize(type.displayValue).equals(normalized)
                        || normalize(type.databaseValue).equals(normalized))
                .findFirst()
                .orElse(null);
    }

    private static String normalize(String value) {
        return java.text.Normalizer.normalize(value == null ? "" : value.trim(),
                        java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(java.util.Locale.ROOT);
    }
}
