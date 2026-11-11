package com.littlehorizons.erp.model;

/**
 * State of a club membership, matching the {@code estado_membresia} DB ENUM.
 */
public enum MembershipStatus {

    ACTIVE("Activa"),
    WITHDRAWN("Retirada");

    private final String label;

    MembershipStatus(String label) {
        this.label = label;
    }

    /** Exact string stored in the database. */
    public String getLabel() {
        return label;
    }

    /** Maps a DB string to the enum; unknown values fall back to ACTIVE. */
    public static MembershipStatus fromLabel(String label) {
        for (MembershipStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
