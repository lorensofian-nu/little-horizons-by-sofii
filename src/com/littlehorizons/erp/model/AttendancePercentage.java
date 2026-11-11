package com.littlehorizons.erp.model;

/**
 * Overall attendance percentage of one student in the selected year.
 *
 * @param studentName  full student name
 * @param attended     records that are not 'Ausente' (Presente, Tardanza,
 *                     Excusa Médica)
 * @param totalRecords total attendance records registered
 * @param percentage   attendance percentage between 0 and 100
 */
public record AttendancePercentage(
        String studentName,
        long attended,
        long totalRecords,
        double percentage) {
}
