package com.littlehorizons.erp.model;

/**
 * Type of an extracurricular event, matching the {@code tipo_evento} DB ENUM.
 */
public enum EventType {

    ACADEMIC("Académico"),
    CULTURAL("Cultural"),
    SPORTS("Deportivo"),
    SOCIAL("Social"),
    SCIENTIFIC("Científico");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    /** Exact string stored in the database. */
    public String getLabel() {
        return label;
    }

    /** Maps a DB string to the enum; unknown values fall back to ACADEMIC. */
    public static EventType fromLabel(String label) {
        for (EventType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        return ACADEMIC;
    }
}
