package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Single event participation record (participantes_evento).
 *
 * @param eventName      event name (nombre_evento)
 * @param eventDate      event date (fecha_evento)
 * @param hasCompetition whether the event awards positions (tiene_competencia)
 * @param studentId      student identifier (id_estudiante)
 * @param studentName    full student name
 * @param role           participation role (rol_participacion)
 * @param position       position obtained such as "1er Lugar", may be null
 *                       (posicion_obtenida)
 */
public record EventParticipationRecord(
        String eventName,
        LocalDate eventDate,
        boolean hasCompetition,
        long studentId,
        String studentName,
        String role,
        String position) {
}
