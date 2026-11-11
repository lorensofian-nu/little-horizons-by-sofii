package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Single attendance record (asistencia) fetched from the database.
 *
 * @param studentId       student identifier (id_estudiante)
 * @param studentName     full student name
 * @param attendanceDate  attendance date (fecha_asistencia)
 * @param attendanceState state of the record (Presente, Ausente, Tardanza,
 *                        Excusa Médica)
 */
public record AttendanceRecord(
        long studentId,
        String studentName,
        LocalDate attendanceDate,
        String attendanceState) {
}
