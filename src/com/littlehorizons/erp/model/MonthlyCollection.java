package com.littlehorizons.erp.model;

/**
 * Total money collected during one calendar month.
 *
 * @param monthLabel     month as "yyyy-MM"
 * @param totalCollected sum of monto_pagado in Colombian pesos
 */
public record MonthlyCollection(
        String monthLabel,
        double totalCollected) {
}
