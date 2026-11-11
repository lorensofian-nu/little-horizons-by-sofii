package com.littlehorizons.erp.model;

import java.util.List;

/**
 * Complete resources / library report.
 *
 * @param loansByState  number of loans (students + professors) per loan state
 * @param overdueLoans  active loans whose expected return date is in the past,
 *                      oldest first
 * @param inventory     number of resources per category and state
 *                      (label = "category - state", value = count)
 */
public record ResourcesReportResult(
        List<ReportRow> loansByState,
        List<LoanOverdueReport> overdueLoans,
        List<ReportRow> inventory) {
}
