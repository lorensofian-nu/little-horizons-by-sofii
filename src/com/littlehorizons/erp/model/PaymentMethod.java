package com.littlehorizons.erp.model;

/**
 * Payment methods accepted by the school. The {@link #label} of every constant
 * matches the {@code metodo_pago} ENUM column of the {@code pagos} table exactly.
 */
public enum PaymentMethod {

    CASH("Efectivo"),
    BANK_TRANSFER("Transferencia"),
    DEBIT_CARD("Tarjeta Débito"),
    CREDIT_CARD("Tarjeta Crédito"),
    CHECK("Cheque");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    /** Database representation of this payment method. */
    public String getLabel() {
        return label;
    }

    /** All labels, ready to feed {@code ConsoleUtils.readOption}. */
    public static java.util.List<String> labels() {
        java.util.List<String> labels = new java.util.ArrayList<>();
        for (PaymentMethod method : values()) {
            labels.add(method.label);
        }
        return labels;
    }

    /** Resolves a database label back to the enum, or null when unknown. */
    public static PaymentMethod fromLabel(String label) {
        for (PaymentMethod method : values()) {
            if (method.label.equals(label)) {
                return method;
            }
        }
        return null;
    }
}
