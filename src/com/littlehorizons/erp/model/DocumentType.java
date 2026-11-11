package com.littlehorizons.erp.model;

import java.util.Arrays;
import java.util.List;

/**
 * Student document types. Enum values match the database ENUM column
 * {@code estudiantes.tipo_documento} ('TI', 'CC', 'CE', 'Pasaporte').
 */
public enum DocumentType {

    TI("TI"),
    CC("CC"),
    CE("CE"),
    PASSPORT("Pasaporte");

    private final String displayValue;

    DocumentType(String displayValue) {
        this.displayValue = displayValue;
    }

    /** Value stored in the database column. */
    @Override
    public String toString() {
        return displayValue;
    }

    /** All database values, useful for console prompts. */
    public static List<String> displayValues() {
        return Arrays.stream(values()).map(DocumentType::toString).toList();
    }

    /** Maps a database value to the enum, defaulting to TI. */
    public static DocumentType fromDb(String value) {
        return Arrays.stream(values())
                .filter(type -> type.displayValue.equals(value))
                .findFirst()
                .orElse(TI);
    }

    /** Maps user input to the enum; returns null when the value is invalid. */
    public static DocumentType fromDisplayValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.displayValue.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}
