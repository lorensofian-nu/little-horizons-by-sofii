package com.littlehorizons.erp.model;

/**
 * Academic period a payment belongs to. The {@link #label} of every constant
 * matches the {@code periodo_academico} ENUM column of the {@code pagos} table exactly.
 */
public enum PaymentPeriod {

    FIRST("I"),
    SECOND("II"),
    THIRD("III"),
    FOURTH("IV"),
    ANNUAL("Anual");

    private final String label;

    PaymentPeriod(String label) {
        this.label = label;
    }

    /** Database representation of this period. */
    public String getLabel() {
        return label;
    }

    /** All labels, ready to feed {@code ConsoleUtils.readOption}. */
    public static java.util.List<String> labels() {
        java.util.List<String> labels = new java.util.ArrayList<>();
        for (PaymentPeriod period : values()) {
            labels.add(period.label);
        }
        return labels;
    }

    /** Resolves a database label back to the enum, or null when unknown. */
    public static PaymentPeriod fromLabel(String label) {
        for (PaymentPeriod period : values()) {
            if (period.label.equals(label)) {
                return period;
            }
        }
        return null;
    }
}
