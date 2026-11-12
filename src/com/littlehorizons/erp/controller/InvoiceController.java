package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.GuardianSummary;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.UserRole;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.InvoiceGenerator;
import com.littlehorizons.erp.util.LoggerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/** Invoice use cases: listing, viewing and regenerating the .txt files. */
public class InvoiceController {

    private final PaymentController paymentController;
    private final UserRole userRole;

    public InvoiceController() {
        this(UserRole.ADMINISTRATOR);
    }

    public InvoiceController(UserRole userRole) {
        this.userRole = userRole;
        this.paymentController = new PaymentController(userRole);
    }

    /** Lists every invoice with its file status. */
    public void listInvoices() {
        ConsoleUtils.printHeader("ELECTRONIC INVOICES");
        try {
            List<Invoice> invoices = paymentController.getInvoiceDao().findAll();
            if (invoices.isEmpty()) {
                System.out.println("  No invoices generated yet.");
                return;
            }
            System.out.printf("%-4s %-14s %-10s %-12s %-18s %-9s%n",
                    "ID", "NUMBER", "PAYMENT", "ISSUE DATE", "FILE", "ON DISK");
            invoices.forEach(invoice -> System.out.printf("%-4d %-14s %-10d %-12s %-18s %-9s%n",
                    invoice.getId(),
                    invoice.getInvoiceNumber(),
                    invoice.getPaymentId(),
                    invoice.getIssueDate(),
                    invoice.getFileName(),
                    invoiceFileExists(invoice) ? "Yes" : "No"));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while listing invoices.");
            LoggerUtil.logError("InvoiceController.listInvoices", exception);
        }
    }

    /** Prints the content of a chosen invoice file on the console. */
    public void viewInvoice() {
        ConsoleUtils.printHeader("VIEW INVOICE");
        try {
            Invoice invoice = selectInvoice();
            if (invoice == null) {
                return;
            }
            paymentController.printInvoiceFile(invoice);
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while looking up the invoice.");
            LoggerUtil.logError("InvoiceController.viewInvoice", exception);
        }
    }

    /**
     * Regenerates the .txt file of an existing invoice using the stored payment
     * data (same number, date and CUFE). Useful after accidental file deletion.
     */
    public void regenerateInvoice() {
        ConsoleUtils.printHeader("REGENERATE INVOICE FILE");
        try {
            Invoice invoice = selectInvoice();
            if (invoice == null) {
                return;
            }
            Payment payment = paymentController.getPaymentDao().findById(invoice.getPaymentId());
            if (payment == null) {
                ConsoleUtils.printError("The payment of this invoice no longer exists.");
                return;
            }
            PaymentConceptDaoBridge concepts = new PaymentConceptDaoBridge();
            PaymentConcept concept = concepts.findById(payment.getConceptId());
            if (concept == null) {
                ConsoleUtils.printError("The payment concept of this invoice no longer exists.");
                return;
            }
            GuardianSummary guardian = payment.getGuardianId() == null
                    ? null
                    : paymentController.getPaymentDao().findGuardianById(payment.getGuardianId());

            // Rebuild the file with the original invoice number and issue date.
            Path filePath = InvoiceGenerator.resolveInvoicePath(invoice);

            String cufe = InvoiceGenerator.computeCufe(invoice.getInvoiceNumber(), invoice.getIssueDate());
            String content = InvoiceGenerator.rebuildContent(payment, concept, guardian,
                    invoice.getInvoiceNumber(), invoice.getIssueDate(), cufe,
                    userRole.canViewSensitiveClientData());
            InvoiceGenerator.writePrivateText(filePath, content);

            ConsoleUtils.printSuccess("Invoice file regenerated: " + filePath);
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while regenerating the invoice.");
            LoggerUtil.logError("InvoiceController.regenerateInvoice", exception);
        } catch (IOException exception) {
            ConsoleUtils.printError("Could not write the invoice file.");
            LoggerUtil.logError("InvoiceController.regenerateInvoice", exception);
        }
    }

    // ------------------------------------------------------------------
    // Internal helpers.
    // ------------------------------------------------------------------

    /** Asks for an invoice number (e.g. FAC-000001) and loads it (empty = cancel). */
    private Invoice selectInvoice() throws SQLException {
        String invoiceNumber = ConsoleUtils.readOptionalText(
                "Invoice number (empty = cancel): ", null);
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            return null;
        }
        Invoice invoice = paymentController.getInvoiceDao().findByInvoiceNumber(
                invoiceNumber.toUpperCase());
        if (invoice == null) {
            ConsoleUtils.printError("Invoice " + invoiceNumber + " not found.");
            return null;
        }
        return invoice;
    }

    private boolean invoiceFileExists(Invoice invoice) {
        try {
            return Files.exists(InvoiceGenerator.resolveInvoicePath(invoice));
        } catch (IOException exception) {
            return false;
        }
    }

    /** Minimal bridge so this controller does not need its own DAO instance wiring. */
    private static final class PaymentConceptDaoBridge {
        private final com.littlehorizons.erp.dao.PaymentConceptDao dao =
                new com.littlehorizons.erp.dao.impl.PaymentConceptDaoImpl();

        private PaymentConcept findById(int id) throws SQLException {
            return dao.findById(id);
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }
}
