package com.littlehorizons.erp.model;

import java.util.Arrays;

/**
 * Academic period for grade records, matching the DB ENUM
 * {@code registros_academicos.periodo_academico} ('I','II','III','IV','Final').
 */
public enum Period {

    FIRST("I"),
    SECOND("II"),
    THIRD("III"),
    FOURTH("IV"),
    FINAL("Final");

    /** Spanish value stored in the database. */
    private final String label;

    Period(String label) {
        this.label = label;
    }

    /** Spanish value stored in the database column. */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    /** Finds the enum whose label matches the Spanish DB value, or null. */
    public static Period fromLabel(String label) {
        return Arrays.stream(values())
                .filter(period -> period.label.equalsIgnoreCase(label))
                .findFirst()
                .orElse(null);
    }

    /** Friendly display name used in menus and reports. */
    public String getDisplayName() {
        return switch (this) {
            case FIRST -> "Period I";
            case SECOND -> "Period II";
            case THIRD -> "Period III";
            case FOURTH -> "Period IV";
            case FINAL -> "Final grade";
        };
    }
}
