package com.littlehorizons.erp.model;

import java.util.List;

/**
 * Complete extracurricular report.
 *
 * @param clubOccupancy          occupancy of every active club, busiest first
 * @param participationPerStudent number of event participations per student
 *                                (label = name, value = count), most active
 *                                first
 * @param rankedStudents         students that obtained a podium position
 *                                (1st, 2nd, 3rd place) in competitive events
 */
public record ExtracurricularReportResult(
        List<ClubOccupancyReport> clubOccupancy,
        List<ReportRow> participationPerStudent,
        List<ReportRow> rankedStudents) {
}
