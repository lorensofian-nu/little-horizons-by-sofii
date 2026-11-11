package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Single payment record (pago) fetched from the database.
 *
 * @param studentId   student identifier (id_estudiante)
 * @param studentName full student name
 * @param conceptName payment concept name (nombre_concepto)
 * @param amount      amount paid in Colombian pesos (monto_pagado)
 * @param paymentDate payment date (fecha_pago)
 */
public record PaymentRecord(
        long studentId,
        String studentName,
        String conceptName,
        double amount,
        LocalDate paymentDate) {
}
