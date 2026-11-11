package com.littlehorizons.erp.model;

/**
 * Attendance alert for a student that exceeded the allowed absences in a
 * single ISO week.
 *
 * @param studentId        student identifier (id_estudiante)
 * @param studentName      full student name
 * @param weekLabel        ISO week label, e.g. "2026-W14"
 * @param absences         number of 'Ausente' records in that week
 * @param weekRecords      total attendance records in that week
 * @param weeklyPercentage attendance percentage of that week (0 - 100)
 */
public record AttendanceAlert(
        long studentId,
        String studentName,
        String weekLabel,
        int absences,
        int weekRecords,
        double weeklyPercentage) {
}
