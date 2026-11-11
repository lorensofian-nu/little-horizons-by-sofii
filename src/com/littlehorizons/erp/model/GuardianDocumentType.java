package com.littlehorizons.erp.model;

import java.util.Arrays;
import java.util.List;

/**
 * Guardian document types. Enum values match the database ENUM column
 * {@code acudientes.tipo_documento} ('CC', 'CE', 'Pasaporte').
 */
public enum GuardianDocumentType {

    CC("CC"),
    CE("CE"),
    PASSPORT("Pasaporte");

    private final String displayValue;

    GuardianDocumentType(String displayValue) {
        this.displayValue = displayValue;
    }

    /** Value stored in the database column. */
    @Override
    public String toString() {
        return displayValue;
    }

    /** All database values, useful for console prompts. */
    public static List<String> displayValues() {
        return Arrays.stream(values()).map(GuardianDocumentType::toString).toList();
    }

    /** Maps a database value to the enum, defaulting to CC. */
    public static GuardianDocumentType fromDb(String value) {
        return Arrays.stream(values())
                .filter(type -> type.displayValue.equals(value))
                .findFirst()
                .orElse(CC);
    }

    /** Maps user input to the enum; returns null when the value is invalid. */
    public static GuardianDocumentType fromDisplayValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.displayValue.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}
