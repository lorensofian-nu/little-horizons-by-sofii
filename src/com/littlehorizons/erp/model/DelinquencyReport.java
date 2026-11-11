package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Delinquent active student for the financial report.
 *
 * @param studentId            student identifier (id_estudiante)
 * @param studentName          full student name
 * @param lastPaymentDate      most recent payment date, or {@code null} when
 *                             the student has never paid
 * @param daysSinceLastPayment days elapsed since the last payment; -1 means
 *                             no payments were ever registered
 */
public record DelinquencyReport(
        long studentId,
        String studentName,
        LocalDate lastPaymentDate,
        long daysSinceLastPayment) {
}
