package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.InvoiceDao;
import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.dao.impl.InvoiceDaoImpl;
import com.littlehorizons.erp.dao.impl.PaymentDaoImpl;
import com.littlehorizons.erp.model.GuardianSummary;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.PaymentMethod;
import com.littlehorizons.erp.model.PaymentPeriod;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.InvoiceGenerator;
import com.littlehorizons.erp.util.LoggerUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Payment use cases: registration + automatic invoice generation, history, delinquency report. */
public class PaymentController {

    private final PaymentDao paymentDao;
    private final PaymentConceptController conceptController;
    private final InvoiceDao invoiceDao;

    public PaymentController() {
        this.paymentDao = new PaymentDaoImpl();
        this.conceptController = new PaymentConceptController();
        this.invoiceDao = new InvoiceDaoImpl();
    }

    // ------------------------------------------------------------------
    // 1. Register payment + automatic electronic invoice.
    // ------------------------------------------------------------------

    /** Full console flow: registers a payment and generates its .txt invoice. */
    public void registerPayment() {
        ConsoleUtils.printHeader("REGISTER PAYMENT & GENERATE INVOICE");
        try {
            StudentSummary student = selectStudent();
            if (student == null) {
                return;
            }
            GuardianSummary guardian = selectGuardian(student.getStudentId());

            PaymentConcept concept = conceptController.selectActiveConcept();
            if (concept == null) {
                return;
            }

            // Personalized amount per student (sibling discounts, etc.).
            double defaultAmount = concept.getBaseAmount().doubleValue();
            System.out.printf("  Base amount of the concept: %s%n", InvoiceGenerator.formatMoney(defaultAmount));
            double amountValue = ConsoleUtils.readMoney("Amount paid (empty = base amount): ");
            if (amountValue <= 0) {
                amountValue = defaultAmount;
            }

            LocalDate paymentDate = ConsoleUtils.readOptionalDate("Payment date", LocalDate.now());
            String methodLabel = ConsoleUtils.readOption("Payment method:", PaymentMethod.labels());
            PaymentMethod method = PaymentMethod.fromLabel(methodLabel);
            int academicYear = ConsoleUtils.readInt("Academic year (0 = " + LocalDate.now().getYear() + "): ",
                    2000, 2100, 0);
            if (academicYear == 0) {
                academicYear = LocalDate.now().getYear();
            }
            String periodLabel = ConsoleUtils.readOption("Academic period:", PaymentPeriod.labels());
            String notes = ConsoleUtils.readOptionalText("Notes (empty = none): ", null);

            // Receipt number: empty -> auto-generated sequential number.
            String receiptNumber = ConsoleUtils.readOptionalText(
                    "Receipt number (empty = auto): ", null);
            if (receiptNumber == null || receiptNumber.isBlank()) {
                receiptNumber = paymentDao.nextReceiptNumber(academicYear);
            }

            Payment payment = new Payment(0, student.getStudentId(),
                    guardian == null ? null : guardian.getId(),
                    concept.getId(),
                    BigDecimal.valueOf(amountValue).setScale(2, RoundingMode.HALF_UP),
                    paymentDate, method, receiptNumber, academicYear,
                    PaymentPeriod.fromLabel(periodLabel), notes, null);

            // ---- Atomic-ish flow: pago -> factura -> archivo .txt ----
            paymentDao.save(payment);
            Invoice invoice = generateInvoice(payment, concept, guardian);

            ConsoleUtils.printSuccess("Payment registered (id " + payment.getId()
                    + ", receipt " + receiptNumber + ").");
            ConsoleUtils.printSuccess("Invoice " + invoice.getInvoiceNumber()
                    + " generated: " + invoice.getFilePath());
            if (ConsoleUtils.confirm("Show the invoice on screen now?")) {
                printInvoiceFile(invoice);
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while registering the payment.");
            LoggerUtil.logError("PaymentController.registerPayment", exception);
        } catch (IOException exception) {
            ConsoleUtils.printError("The payment was registered but the invoice file could not be written.");
            LoggerUtil.logError("PaymentController.registerPayment", exception);
        }
    }

    /** Generates and persists the invoice of a confirmed payment. */
    public Invoice generateInvoice(Payment payment, PaymentConcept concept, GuardianSummary guardian)
            throws SQLException, IOException {
        String invoiceNumber = invoiceDao.nextInvoiceNumber();
        Invoice invoice = InvoiceGenerator.generateInvoice(payment, concept, guardian, invoiceNumber);
        invoiceDao.save(invoice);
        return invoice;
    }

    // ------------------------------------------------------------------
    // 2. Payment history.
    // ------------------------------------------------------------------

    /** History per student. */
    public void showStudentHistory() {
        ConsoleUtils.printHeader("PAYMENT HISTORY - STUDENT");
        try {
            StudentSummary student = selectStudent();
            if (student == null) {
                return;
            }
            printHistory(paymentDao.findByStudent(student.getStudentId()));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while reading the payment history.");
            LoggerUtil.logError("PaymentController.showStudentHistory", exception);
        }
    }

    /** History per guardian (all their children, via estudiante_acudiente). */
    public void showGuardianHistory() {
        ConsoleUtils.printHeader("PAYMENT HISTORY - GUARDIAN (ALL CHILDREN)");
        try {
            GuardianSummary guardian = selectGuardian(null);
            if (guardian == null) {
                return;
            }
            List<Payment> payments = paymentDao.findByGuardian(guardian.getId());
            if (payments.isEmpty()) {
                System.out.println("  No payments found for " + guardian.getFullName() + ".");
                return;
            }
            System.out.println("  Guardian: " + guardian.getFullName());
            printHistory(payments);
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while reading the payment history.");
            LoggerUtil.logError("PaymentController.showGuardianHistory", exception);
        }
    }

    private void printHistory(List<Payment> payments) {
        System.out.printf("%-4s %-12s %-28s %14s %-12s %-12s %-16s%n",
                "ID", "DATE", "CONCEPT", "AMOUNT", "METHOD", "PERIOD", "RECEIPT");
        payments.forEach(payment -> System.out.printf("%-4d %-12s %-28s %14s %-12s %-12s %-16s%n",
                payment.getId(),
                payment.getPaymentDate(),
                truncate(payment.getConceptName(), 28),
                InvoiceGenerator.formatMoney(payment.getAmountPaid()),
                payment.getMethod() == null ? "N/A" : payment.getMethod().getLabel(),
                payment.getPeriod() == null ? "N/A" : payment.getPeriod().getLabel(),
                payment.getReceiptNumber()));
        // Totals by payment method using the Stream API.
        Map<String, Double> totalsByMethod = payments.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        payment -> payment.getMethod() == null ? "N/A" : payment.getMethod().getLabel(),
                        java.util.stream.Collectors.summingDouble(
                                payment -> payment.getAmountPaid().doubleValue())));
        System.out.println("  Totals by payment method:");
        totalsByMethod.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.printf("    %-20s %s%n", entry.getKey(),
                        InvoiceGenerator.formatMoney(entry.getValue())));
    }

    // ------------------------------------------------------------------
    // 3. Delinquency report ("morosos").
    // ------------------------------------------------------------------

    /**
     * Active students whose last payment in the current academic year is older
     * than 30 days, or who have no payments at all this year.
     */
    public void showDelinquencyReport() {
        ConsoleUtils.printHeader("DELINQUENCY REPORT (MORE THAN 30 DAYS WITHOUT PAYING)");
        try {
            int currentYear = LocalDate.now().getYear();
            List<StudentSummary> activeStudents = paymentDao.listActiveStudents();
            Map<Integer, LocalDate> lastPayments =
                    paymentDao.findLastPaymentDatesByYear(currentYear);
            LocalDate today = LocalDate.now();

            Map<Integer, LocalDate> finalLastPayments = lastPayments;
            List<DelinquencyRow> rows = activeStudents.stream()
                    .map(student -> {
                        LocalDate lastDate = finalLastPayments.get(student.getStudentId());
                        long days = lastDate == null ? -1 : ChronoUnit.DAYS.between(lastDate, today);
                        boolean delinquent = lastDate == null
                                || ChronoUnit.DAYS.between(lastDate, today) > 30;
                        return delinquent ? new DelinquencyRow(student, lastDate, days) : null;
                    })
                    .filter(row -> row != null)
                    .sorted((a, b) -> Long.compare(b.daysSincePayment, a.daysSincePayment))
                    .collect(java.util.stream.Collectors.toList());

            if (rows.isEmpty()) {
                ConsoleUtils.printSuccess("No delinquent students. Everyone paid within the last 30 days.");
                return;
            }
            System.out.printf("%-6s %-34s %-14s %-12s%n", "ID", "STUDENT", "LAST PAYMENT", "DAYS");
            rows.forEach(row -> System.out.printf("%-6d %-34s %-14s %-12s%n",
                    row.student.getStudentId(),
                    truncate(row.student.getFullName(), 34),
                    row.lastPaymentDate == null ? "No payments in " + currentYear
                            : row.lastPaymentDate.toString(),
                    row.daysSincePayment < 0 ? "N/A" : row.daysSincePayment));
            System.out.println("  Total delinquent students: " + rows.size());
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while building the delinquency report.");
            LoggerUtil.logError("PaymentController.showDelinquencyReport", exception);
        }
    }

    /** Internal record for the report rows. */
    private static final class DelinquencyRow {
        private final StudentSummary student;
        private final LocalDate lastPaymentDate;
        private final long daysSincePayment;

        private DelinquencyRow(StudentSummary student, LocalDate lastPaymentDate, long daysSincePayment) {
            this.student = student;
            this.lastPaymentDate = lastPaymentDate;
            this.daysSincePayment = daysSincePayment;
        }
    }

    // ------------------------------------------------------------------
    // Console pickers.
    // ------------------------------------------------------------------

    /** Lets the user choose one active student (0 = cancel). */
    public StudentSummary selectStudent() throws SQLException {
        List<StudentSummary> students = paymentDao.listActiveStudents();
        if (students.isEmpty()) {
            ConsoleUtils.printError("No active students registered yet.");
            return null;
        }
        System.out.println("  Active students:");
        students.forEach(student -> System.out.printf("    %d. %s%n", student.getStudentId(), student.getFullName()));
        int studentId = ConsoleUtils.readInt("Student id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (studentId == 0) {
            return null;
        }
        StudentSummary student = paymentDao.findStudentById(studentId);
        if (student == null) {
            ConsoleUtils.printError("Student " + studentId + " not found or not active.");
            return null;
        }
        return student;
    }

    /**
     * Lets the user choose a guardian. When {@code studentId} is given, first
     * offers the guardians linked to that student, then optionally all guardians.
     */
    public GuardianSummary selectGuardian(Integer studentId) throws SQLException {
        List<GuardianSummary> guardians = studentId == null
                ? paymentDao.listGuardians()
                : paymentDao.findGuardiansByStudent(studentId);
        if (studentId != null && guardians.isEmpty()) {
            if (!ConsoleUtils.confirm("This student has no linked guardians. Show all guardians?")) {
                return null;
            }
            guardians = paymentDao.listGuardians();
        }
        if (guardians.isEmpty()) {
            ConsoleUtils.printError("No guardians registered yet.");
            return null;
        }
        System.out.println("  Guardians:");
        guardians.forEach(guardian -> System.out.printf("    %d. %s (%s)%n",
                guardian.getId(), guardian.getFullName(), guardian.getDocumentDescription()));
        int guardianId = ConsoleUtils.readInt("Guardian id (0 = skip / cancel): ", 1, Integer.MAX_VALUE, 0);
        if (guardianId == 0) {
            return null;
        }
        GuardianSummary guardian = paymentDao.findGuardianById(guardianId);
        if (guardian == null) {
            ConsoleUtils.printError("Guardian " + guardianId + " not found.");
            return null;
        }
        return guardian;
    }

    // ------------------------------------------------------------------
    // Shared helpers.
    // ------------------------------------------------------------------

    /** Prints the content of an invoice .txt file on the console. */
    public void printInvoiceFile(Invoice invoice) {
        try {
            Path filePath = Paths.get(invoice.getFilePath());
            if (!Files.exists(filePath)) {
                ConsoleUtils.printError("Invoice file not found: " + invoice.getFilePath());
                return;
            }
            System.out.println();
            Files.readAllLines(filePath, java.nio.charset.StandardCharsets.UTF_8)
                    .forEach(System.out::println);
        } catch (IOException exception) {
            ConsoleUtils.printError("Could not read the invoice file.");
            LoggerUtil.logError("PaymentController.printInvoiceFile", exception);
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }

    /** Exposes the concept controller for the view's concepts submenu. */
    public PaymentConceptController getConceptController() {
        return conceptController;
    }

    /** Exposes the payment DAO (used by the invoice controller). */
    public PaymentDao getPaymentDao() {
        return paymentDao;
    }

    /** Exposes the invoice DAO (used by the invoice controller). */
    public InvoiceDao getInvoiceDao() {
        return invoiceDao;
    }
}
