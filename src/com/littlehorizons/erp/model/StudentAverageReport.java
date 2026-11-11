package com.littlehorizons.erp.model;

import java.util.Map;

/**
 * Averages computed for one student during a given academic year.
 *
 * @param studentId       student identifier (id_estudiante)
 * @param studentName     full student name
 * @param gradeName       grade of the student for that year (nombre_grado)
 * @param subjectAverages average grade per subject, in subject order
 * @param generalAverage  average of all subject averages (0.0 - 5.0)
 */
public record StudentAverageReport(
        long studentId,
        String studentName,
        String gradeName,
        Map<String, Double> subjectAverages,
        double generalAverage) {
}
