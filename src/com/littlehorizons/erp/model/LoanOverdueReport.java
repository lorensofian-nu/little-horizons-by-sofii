package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Overdue resource loan (estado 'Activo' with fecha_devolucion_esperada in the
 * past).
 *
 * @param loanType           "Student" or "Professor"
 * @param resourceName       resource name (nombre_recurso)
 * @param holderName         full name of the student or professor holding it
 * @param loanDate           loan start date (fecha_prestamo)
 * @param expectedReturnDate promised return date (fecha_devolucion_esperada)
 * @param daysOverdue        days past the expected return date
 */
public record LoanOverdueReport(
        String loanType,
        String resourceName,
        String holderName,
        LocalDate loanDate,
        LocalDate expectedReturnDate,
        long daysOverdue) {
}
