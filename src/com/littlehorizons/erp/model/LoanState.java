package com.littlehorizons.erp.model;

/**
 * Possible states of a loan, matching the DB ENUM column
 * {@code estado_prestamo} (shared by prestamos_estudiante and prestamos_profesor).
 */
public enum LoanState {

    ACTIVE("Activo"),
    RETURNED("Devuelto"),
    OVERDUE("Vencido");

    private final String dbValue;

    LoanState(String dbValue) {
        this.dbValue = dbValue;
    }

    /** The exact string stored in the database ENUM column. */
    public String getDbValue() {
        return dbValue;
    }

    /** Maps a DB ENUM string to its enum constant. */
    public static LoanState fromDbValue(String value) {
        for (LoanState state : values()) {
            if (state.dbValue.equals(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown loan state: " + value);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
