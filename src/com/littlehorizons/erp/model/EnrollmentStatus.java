package com.littlehorizons.erp.model;

import java.util.Arrays;
import java.util.List;

/**
 * Enrollment statuses. Enum values match the database ENUM column
 * {@code matriculas.estado_matricula} ('Activa', 'Completada', 'Retirada').
 */
public enum EnrollmentStatus {

    ACTIVE("Activa"),
    COMPLETED("Completada"),
    WITHDRAWN("Retirada");

    private final String displayValue;

    EnrollmentStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    /** Value stored in the database column. */
    @Override
    public String toString() {
        return displayValue;
    }

    /** All database values, useful for console prompts. */
    public static List<String> displayValues() {
        return Arrays.stream(values()).map(EnrollmentStatus::toString).toList();
    }

    /** Maps a database value to the enum, defaulting to Activa. */
    public static EnrollmentStatus fromDb(String value) {
        return Arrays.stream(values())
                .filter(status -> status.displayValue.equals(value))
                .findFirst()
                .orElse(ACTIVE);
    }

    /** Maps user input to the enum; returns null when the value is invalid. */
    public static EnrollmentStatus fromDisplayValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.displayValue.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}
