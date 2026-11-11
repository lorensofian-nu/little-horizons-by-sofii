package com.littlehorizons.erp.model;

/**
 * Occupancy of one extracurricular club.
 *
 * @param clubName       club name (nombre_club)
 * @param supervisorName full name of the supervising teacher
 * @param activeMembers  memberships with estado_membresia = 'Activa'
 * @param capacity       maximum capacity (capacidad_maxima)
 */
public record ClubOccupancyReport(
        String clubName,
        String supervisorName,
        int activeMembers,
        int capacity) {

    /** Occupancy percentage against the maximum capacity (0 - 100). */
    public double occupancyPercentage() {
        return capacity <= 0 ? 0.0 : (activeMembers * 100.0) / capacity;
    }
}
