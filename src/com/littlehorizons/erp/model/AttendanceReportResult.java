package com.littlehorizons.erp.model;

import java.util.List;

/**
 * Complete attendance report for one year.
 *
 * @param weeklyAlerts       student/week combinations with more than 3
 *                           absences, most absences first
 * @param studentPercentages overall attendance percentage per student,
 *                           best attendance first
 */
public record AttendanceReportResult(
        List<AttendanceAlert> weeklyAlerts,
        List<AttendancePercentage> studentPercentages) {
}
