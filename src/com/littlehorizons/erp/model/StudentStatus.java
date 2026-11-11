package com.littlehorizons.erp.model;

import java.util.Arrays;
import java.util.List;

/**
 * Student lifecycle statuses. Enum values match the database ENUM column
 * {@code estudiantes.estado} ('Activo', 'Graduado', 'Retirado', 'Suspendido').
 */
public enum StudentStatus {

    ACTIVE("Activo"),
    GRADUATED("Graduado"),
    WITHDRAWN("Retirado"),
    SUSPENDED("Suspendido");

    private final String displayValue;

    StudentStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    /** Value stored in the database column. */
    @Override
    public String toString() {
        return displayValue;
    }

    /** All database values, useful for console prompts. */
    public static List<String> displayValues() {
        return Arrays.stream(values()).map(StudentStatus::toString).toList();
    }

    /** Maps a database value to the enum, defaulting to Activo. */
    public static StudentStatus fromDb(String value) {
        return Arrays.stream(values())
                .filter(status -> status.displayValue.equals(value))
                .findFirst()
                .orElse(ACTIVE);
    }

    /** Maps user input to the enum; returns null when the value is invalid. */
    public static StudentStatus fromDisplayValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.displayValue.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}
