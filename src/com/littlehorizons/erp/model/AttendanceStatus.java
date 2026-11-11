package com.littlehorizons.erp.model;

import java.util.Arrays;
import java.util.List;

/**
 * Attendance state of a student in a subject on a given day, matching the DB
 * ENUM {@code asistencias.estado_asistencia}
 * ('Presente','Ausente','Tardanza','Excusa Médica').
 */
public enum AttendanceStatus {

    PRESENT("Presente"),
    ABSENT("Ausente"),
    LATE("Tardanza"),
    MEDICAL_EXCUSE("Excusa Médica");

    /** Spanish value stored in the database. */
    private final String label;

    AttendanceStatus(String label) {
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
    public static AttendanceStatus fromLabel(String label) {
        return Arrays.stream(values())
                .filter(status -> status.label.equalsIgnoreCase(label))
                .findFirst()
                .orElse(null);
    }

    /** Spanish labels in canonical order, for console selection. */
    public static List<String> labels() {
        return Arrays.stream(values())
                .map(AttendanceStatus::getLabel)
                .toList();
    }
}
