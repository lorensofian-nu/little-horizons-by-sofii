package com.littlehorizons.erp.model;

import java.util.List;

/**
 * Complete academic report for one academic year.
 *
 * @param allStudents     per-student averages for every student with grades
 * @param topFive         five best general averages, highest first
 * @param academicAlerts  students with a general average below 3.0, lowest
 *                        (most critical) first
 */
public record AcademicReportResult(
        List<StudentAverageReport> allStudents,
        List<StudentAverageReport> topFive,
        List<StudentAverageReport> academicAlerts) {
}
