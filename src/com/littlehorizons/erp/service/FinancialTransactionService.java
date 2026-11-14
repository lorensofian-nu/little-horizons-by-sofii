package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.GuardianSummary;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.InvoiceGenerator;
import com.littlehorizons.erp.util.DocumentSequenceUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Coordinates the payment and invoice write in one database transaction. */
public class FinancialTransactionService {

    private static final String INSERT_PAYMENT = """
            INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago,
                               metodo_pago, numero_recibo, año_academico, periodo_academico, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_INVOICE = """
            INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe,
                                  nombre_archivo_txt, ruta_archivo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    /**
     * Registers the payment and its TXT invoice atomically in the database.
     * The file is written only after commit; a missing file can be regenerated.
     */
    public Invoice registerPayment(Payment payment, PaymentConcept concept,
                                   GuardianSummary guardian) throws SQLException, IOException {
        return registerPayment(payment, concept, guardian, false);
    }

    public Invoice registerPayment(Payment payment, PaymentConcept concept,
                                   GuardianSummary guardian,
                                   boolean includeSensitiveClientData)
            throws SQLException, IOException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (payment.getPaymentDate() == null) {
                    payment.setPaymentDate(LocalDate.now());
                }
                if (payment.getAcademicYear() == null) {
                    payment.setAcademicYear(payment.getPaymentDate().getYear());
                }
                if (payment.getReceiptNumber() == null || payment.getReceiptNumber().isBlank()) {
                    payment.setReceiptNumber(DocumentSequenceUtil.next(connection,
                            "REC-" + payment.getAcademicYear() + "-", "receipt"));
                }
                insertPayment(connection, payment);

                String invoiceNumber = DocumentSequenceUtil.next(connection, "FAC-", "invoice");
                Invoice invoice = InvoiceGenerator.buildInvoice(
                        payment, concept, guardian, invoiceNumber);
                insertInvoice(connection, invoice);
                connection.commit();

                // The database is already consistent if the filesystem is unavailable.
                InvoiceGenerator.writeInvoiceFile(invoice, payment, concept, guardian,
                        includeSensitiveClientData);
                return invoice;
            } catch (SQLException | IOException exception) {
                rollback(connection);
                throw exception;
            }
        }
    }

    private void insertPayment(Connection connection, Payment payment) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_PAYMENT, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, payment.getStudentId());
            if (payment.getGuardianId() == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, payment.getGuardianId());
            }
            statement.setInt(3, payment.getConceptId());
            statement.setBigDecimal(4, payment.getAmountPaid());
            statement.setDate(5, java.sql.Date.valueOf(payment.getPaymentDate()));
            statement.setString(6, payment.getMethod().getLabel());
            statement.setString(7, payment.getReceiptNumber());
            if (payment.getAcademicYear() == null) {
                statement.setNull(8, java.sql.Types.INTEGER);
            } else {
                statement.setInt(8, payment.getAcademicYear());
            }
            if (payment.getPeriod() == null) {
                statement.setNull(9, java.sql.Types.VARCHAR);
            } else {
                statement.setString(9, payment.getPeriod().getLabel());
            }
            statement.setString(10, payment.getNotes());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Payment insert did not return a generated key.");
                }
                payment.setId(keys.getInt(1));
            }
        }
    }

    private void insertInvoice(Connection connection, Invoice invoice) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_INVOICE, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, invoice.getInvoiceNumber());
            statement.setInt(2, invoice.getPaymentId());
            statement.setDate(3, java.sql.Date.valueOf(invoice.getIssueDate()));
            statement.setString(4, invoice.getCufe());
            statement.setString(5, invoice.getFileName());
            statement.setString(6, invoice.getFilePath());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    invoice.setId(keys.getInt(1));
                }
            }
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the original failure.
        }
    }
}
