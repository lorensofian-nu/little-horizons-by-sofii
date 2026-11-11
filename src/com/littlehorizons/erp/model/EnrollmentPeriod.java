package com.littlehorizons.erp.model;

import java.util.Arrays;

/**
 * Academic period for subject enrollments, matching the DB ENUM
 * {@code inscripciones_materia.periodo_academico} ('I','II','III','IV').
 *
 * <p>Separate from {@link Period} because enrollments do not allow 'Final'.</p>
 */
public enum EnrollmentPeriod {

    FIRST("I"),
    SECOND("II"),
    THIRD("III"),
    FOURTH("IV");

    /** Spanish value stored in the database. */
    private final String label;

    EnrollmentPeriod(String label) {
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
    public static EnrollmentPeriod fromLabel(String label) {
        return Arrays.stream(values())
                .filter(period -> period.label.equalsIgnoreCase(label))
                .findFirst()
                .orElse(null);
    }

    /** Friendly display name used in menus. */
    public String getDisplayName() {
        return switch (this) {
            case FIRST -> "Period I";
            case SECOND -> "Period II";
            case THIRD -> "Period III";
            case FOURTH -> "Period IV";
        };
    }
}
