package com.littlehorizons.erp.model;

/**
 * Generic label / value row used by dashboard-style reports where a simple
 * "concept → count/total" pair is enough.
 *
 * @param label description of the row (e.g. a database state or category)
 * @param value formatted value shown to the user (count, total, percentage...)
 */
public record ReportRow(String label, String value) {
}
