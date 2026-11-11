package com.littlehorizons.erp.model;

import java.util.Arrays;
import java.util.List;

/**
 * Student genders. Enum values match the database ENUM column
 * {@code estudiantes.genero} ('M', 'F', 'Otro').
 */
public enum Gender {

    MALE("M"),
    FEMALE("F"),
    OTHER("Otro");

    private final String displayValue;

    Gender(String displayValue) {
        this.displayValue = displayValue;
    }

    /** Value stored in the database column. */
    @Override
    public String toString() {
        return displayValue;
    }

    /** All database values, useful for console prompts. */
    public static List<String> displayValues() {
        return Arrays.stream(values()).map(Gender::toString).toList();
    }

    /** Maps a database value to the enum, defaulting to M. */
    public static Gender fromDb(String value) {
        return Arrays.stream(values())
                .filter(gender -> gender.displayValue.equals(value))
                .findFirst()
                .orElse(MALE);
    }

    /** Maps user input to the enum; returns null when the value is invalid. */
    public static Gender fromDisplayValue(String value) {
        return Arrays.stream(values())
                .filter(gender -> gender.displayValue.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}
