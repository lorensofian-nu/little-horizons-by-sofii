package com.littlehorizons.erp.model;

/**
 * Total money collected for one payment concept during the selected year.
 *
 * @param conceptName    concept name (nombre_concepto)
 * @param totalCollected sum of monto_pagado in Colombian pesos
 * @param paymentCount   number of payments registered
 */
public record ConceptCollection(
        String conceptName,
        double totalCollected,
        long paymentCount) {
}
