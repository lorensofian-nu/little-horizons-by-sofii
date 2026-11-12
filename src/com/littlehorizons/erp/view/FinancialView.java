package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.InvoiceController;
import com.littlehorizons.erp.controller.PaymentConceptController;
import com.littlehorizons.erp.controller.PaymentController;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.model.UserRole;

/**
 * Financial module menu. Instantiates its controllers with no-arg constructor
 * chains; all console I/O goes through {@link ConsoleUtils}.
 */
public class FinancialView {

    private final PaymentConceptController conceptController;
    private final PaymentController paymentController;
    private final InvoiceController invoiceController;

    public FinancialView() {
        this(UserRole.ADMINISTRATOR);
    }

    public FinancialView(UserRole userRole) {
        this.conceptController = new PaymentConceptController();
        this.paymentController = new PaymentController(userRole);
        this.invoiceController = new InvoiceController(userRole);
    }

    /** Main financial menu loop (0 = back). */
    public void show() {
        while (true) {
            ConsoleUtils.printHeader("FINANCIAL MODULE");
            System.out.println("  1. Payment concepts (CRUD)");
            System.out.println("  2. Register payment & generate invoice");
            System.out.println("  3. Payment history");
            System.out.println("  4. Invoices");
            System.out.println("  5. Delinquency report");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 5);
            switch (option) {
                case 1 -> showConceptsMenu();
                case 2 -> paymentController.registerPayment();
                case 3 -> showHistoryMenu();
                case 4 -> showInvoicesMenu();
                case 5 -> paymentController.showDelinquencyReport();
                case 0 -> {
                    return;
                }
                default -> { }
            }
            ConsoleUtils.pause();
        }
    }

    /** Submenu: payment concepts CRUD (0 = back). */
    private void showConceptsMenu() {
        while (true) {
            ConsoleUtils.printHeader("PAYMENT CONCEPTS");
            System.out.println("  1. Create concept");
            System.out.println("  2. List concepts");
            System.out.println("  3. Update concept");
            System.out.println("  4. Delete concept");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 4);
            switch (option) {
                case 1 -> conceptController.createConcept();
                case 2 -> conceptController.listConcepts();
                case 3 -> conceptController.updateConcept();
                case 4 -> conceptController.deleteConcept();
                case 0 -> {
                    return;
                }
                default -> { }
            }
            ConsoleUtils.pause();
        }
    }

    /** Submenu: payment history per student / per guardian (0 = back). */
    private void showHistoryMenu() {
        while (true) {
            ConsoleUtils.printHeader("PAYMENT HISTORY");
            System.out.println("  1. By student");
            System.out.println("  2. By guardian (all their children)");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 2);
            switch (option) {
                case 1 -> paymentController.showStudentHistory();
                case 2 -> paymentController.showGuardianHistory();
                case 0 -> {
                    return;
                }
                default -> { }
            }
            ConsoleUtils.pause();
        }
    }

    /** Submenu: invoice listing / viewing / regeneration (0 = back). */
    private void showInvoicesMenu() {
        while (true) {
            ConsoleUtils.printHeader("ELECTRONIC INVOICES (.TXT)");
            System.out.println("  1. List invoices");
            System.out.println("  2. View invoice on screen");
            System.out.println("  3. Regenerate invoice file");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 3);
            switch (option) {
                case 1 -> invoiceController.listInvoices();
                case 2 -> invoiceController.viewInvoice();
                case 3 -> invoiceController.regenerateInvoice();
                case 0 -> {
                    return;
                }
                default -> { }
            }
            ConsoleUtils.pause();
        }
    }
}
