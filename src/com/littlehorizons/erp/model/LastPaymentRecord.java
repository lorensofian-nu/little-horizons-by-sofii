package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Last payment date registered for an active student. Used to build the
 * delinquency (cartera vencida) report.
 *
 * @param studentId       student identifier (id_estudiante)
 * @param studentName     full student name
 * @param lastPaymentDate most recent payment date, or {@code null} when the
 *                        student has never paid (MAX(fecha_pago) is null)
 */
public record LastPaymentRecord(
        long studentId,
        String studentName,
        LocalDate lastPaymentDate) {
}
