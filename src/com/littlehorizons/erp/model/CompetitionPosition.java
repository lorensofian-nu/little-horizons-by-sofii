package com.littlehorizons.erp.model;

/**
 * Position obtained in a competitive event. Only these values are allowed by
 * the business rules, matching the {@code posicion_obtenida} column values.
 */
public enum CompetitionPosition {

    FIRST_PLACE("1er Lugar"),
    SECOND_PLACE("2do Lugar"),
    THIRD_PLACE("3er Lugar"),
    PARTICIPANT("Participante");

    private final String label;

    CompetitionPosition(String label) {
        this.label = label;
    }

    /** Exact string stored in the database. */
    public String getLabel() {
        return label;
    }

    /** Maps a DB string to the enum; unknown values fall back to PARTICIPANT. */
    public static CompetitionPosition fromLabel(String label) {
        for (CompetitionPosition position : values()) {
            if (position.label.equalsIgnoreCase(label)) {
                return position;
            }
        }
        return PARTICIPANT;
    }
}
