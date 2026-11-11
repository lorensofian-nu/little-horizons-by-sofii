package com.littlehorizons.erp.model;

/**
 * Possible states of a resource, matching the DB ENUM column
 * {@code recursos.estado_recurso}.
 */
public enum ResourceState {

    AVAILABLE("Disponible"),
    LOANED("Prestado"),
    IN_MAINTENANCE("En Mantenimiento"),
    WRITTEN_OFF("Dado de Baja");

    private final String dbValue;

    ResourceState(String dbValue) {
        this.dbValue = dbValue;
    }

    /** The exact string stored in the database ENUM column. */
    public String getDbValue() {
        return dbValue;
    }

    /** Maps a DB ENUM string to its enum constant. */
    public static ResourceState fromDbValue(String value) {
        for (ResourceState state : values()) {
            if (state.dbValue.equals(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown resource state: " + value);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
