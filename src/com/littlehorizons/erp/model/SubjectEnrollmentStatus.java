package com.littlehorizons.erp.model;

import java.util.Arrays;

/**
 * State of a subject enrollment, matching the DB ENUM
 * {@code inscripciones_materia.estado} ('Activa','Retirada','Completada').
 */
public enum SubjectEnrollmentStatus {

    ACTIVE("Activa"),
    WITHDRAWN("Retirada"),
    COMPLETED("Completada");

    /** Spanish value stored in the database. */
    private final String label;

    SubjectEnrollmentStatus(String label) {
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
    public static SubjectEnrollmentStatus fromLabel(String label) {
        return Arrays.stream(values())
                .filter(status -> status.label.equalsIgnoreCase(label))
                .findFirst()
                .orElse(null);
    }
}
