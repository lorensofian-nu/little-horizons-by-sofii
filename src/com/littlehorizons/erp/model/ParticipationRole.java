package com.littlehorizons.erp.model;

/**
 * Role of a student in an event, matching the {@code rol_participacion} DB ENUM.
 */
public enum ParticipationRole {

    PARTICIPANT("Participante"),
    COMPETITOR("Competidor"),
    ORGANIZER("Organizador"),
    ATTENDEE("Asistente");

    private final String label;

    ParticipationRole(String label) {
        this.label = label;
    }

    /** Exact string stored in the database. */
    public String getLabel() {
        return label;
    }

    /** Maps a DB string to the enum; unknown values fall back to PARTICIPANT. */
    public static ParticipationRole fromLabel(String label) {
        for (ParticipationRole role : values()) {
            if (role.label.equalsIgnoreCase(label)) {
                return role;
            }
        }
        return PARTICIPANT;
    }
}
