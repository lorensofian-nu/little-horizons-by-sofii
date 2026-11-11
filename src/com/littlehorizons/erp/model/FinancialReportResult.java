package com.littlehorizons.erp.model;

import java.util.List;

/**
 * Complete financial report for one year.
 *
 * @param conceptTotals   money collected per payment concept, alphabetical
 * @param monthTotals     money collected per calendar month, chronological
 * @param delinquency     active students overdue by more than 30 days (or
 *                        without any payment), oldest first
 * @param totalCollected  grand total collected during the year
 */
public record FinancialReportResult(
        List<ConceptCollection> conceptTotals,
        List<MonthlyCollection> monthTotals,
        List<DelinquencyReport> delinquency,
        double totalCollected) {
}
