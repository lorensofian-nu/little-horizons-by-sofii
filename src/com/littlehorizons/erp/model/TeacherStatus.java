package com.littlehorizons.erp.model;

/**
 * Employment status of a teacher, matching the MySQL ENUM values of the
 * {@code profesores.estado} column ('Activo', 'Inactivo', 'Licencia').
 */
public enum TeacherStatus {

    /** Currently working at the school. */
    ACTIVO("Activo"),

    /** No longer working at the school. */
    INACTIVO("Inactivo"),

    /** On leave (medical, parental, etc.). */
    LICENCIA("Licencia");

    /** Exact value stored in the database column. */
    private final String value;

    TeacherStatus(String value) {
        this.value = value;
    }

    /** Returns the exact database string for this status. */
    public String getValue() {
        return value;
    }

    /** Returns the list of database values, ready for console prompts. */
    public static java.util.List<String> valuesAsStrings() {
        return java.util.Arrays.stream(values())
                .map(TeacherStatus::getValue)
                .toList();
    }

    /** Maps a database value to the enum; throws if the value is unknown. */
    public static TeacherStatus fromValue(String value) {
        for (TeacherStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown teacher status: " + value);
    }
}
