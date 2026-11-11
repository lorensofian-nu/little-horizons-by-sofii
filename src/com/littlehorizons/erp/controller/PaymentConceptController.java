package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.PaymentConceptDao;
import com.littlehorizons.erp.dao.impl.PaymentConceptDaoImpl;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.InvoiceGenerator;
import com.littlehorizons.erp.util.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/** Use cases for the payment concepts catalog ({@code conceptos_pago}). */
public class PaymentConceptController {

    private final PaymentConceptDao conceptDao;

    public PaymentConceptController() {
        this.conceptDao = new PaymentConceptDaoImpl();
    }

    /** Creates a new concept from console input. */
    public void createConcept() {
        ConsoleUtils.printHeader("CREATE PAYMENT CONCEPT");
        try {
            String code = ConsoleUtils.readRequiredText("Concept code (e.g. MAT): ").toUpperCase();
            if (conceptDao.findByCode(code) != null) {
                ConsoleUtils.printError("A concept with code " + code + " already exists.");
                return;
            }
            String name = ConsoleUtils.readRequiredText("Concept name: ");
            String description = ConsoleUtils.readOptionalText("Description (empty = none): ", null);
            double baseAmount = ConsoleUtils.readMoney("Base amount (COP): ");
            boolean recurring = ConsoleUtils.confirm("Is it a recurring payment?");

            PaymentConcept concept = new PaymentConcept(0, code, name, description,
                    BigDecimal.valueOf(baseAmount).setScale(2, RoundingMode.HALF_UP),
                    recurring, true, null, null);
            conceptDao.save(concept);
            ConsoleUtils.printSuccess("Concept created with id " + concept.getId() + ".");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while creating the concept.");
            LoggerUtil.logError("PaymentConceptController.createConcept", exception);
        }
    }

    /** Lists all concepts (active first). */
    public void listConcepts() {
        ConsoleUtils.printHeader("PAYMENT CONCEPTS CATALOG");
        try {
            List<PaymentConcept> concepts = conceptDao.findAll();
            if (concepts.isEmpty()) {
                System.out.println("  No payment concepts registered yet.");
                return;
            }
            System.out.printf("%-4s %-14s %-30s %14s %-9s %-7s%n",
                    "ID", "CODE", "NAME", "BASE AMOUNT", "RECURRING", "ACTIVE");
            concepts.stream()
                    .sorted((a, b) -> Boolean.compare(b.isActive(), a.isActive()))
                    .forEach(concept -> System.out.printf("%-4d %-14s %-30s %14s %-9s %-7s%n",
                            concept.getId(),
                            concept.getConceptCode(),
                            truncate(concept.getConceptName(), 30),
                            InvoiceGenerator.formatMoney(concept.getBaseAmount()),
                            concept.isRecurring() ? "Yes" : "No",
                            concept.isActive() ? "Yes" : "No"));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while listing concepts.");
            LoggerUtil.logError("PaymentConceptController.listConcepts", exception);
        }
    }

    /** Updates an existing concept chosen by id. */
    public void updateConcept() {
        ConsoleUtils.printHeader("UPDATE PAYMENT CONCEPT");
        try {
            PaymentConcept concept = selectConcept();
            if (concept == null) {
                return;
            }
            System.out.println("  Editing: " + concept);
            String code = ConsoleUtils.readOptionalText(
                    "Code [" + concept.getConceptCode() + "]: ", concept.getConceptCode()).toUpperCase();
            String name = ConsoleUtils.readOptionalText(
                    "Name [" + concept.getConceptName() + "]: ", concept.getConceptName());
            String description = ConsoleUtils.readOptionalText(
                    "Description [" + (concept.getDescription() == null ? "-" : concept.getDescription()) + "]: ",
                    concept.getDescription());
            double baseAmount = ConsoleUtils.readDouble(
                    "Base amount [" + concept.getBaseAmount() + "]: ", 0.0, Double.MAX_VALUE);
            boolean recurring = ConsoleUtils.confirm(
                    "Recurring? (current: " + (concept.isRecurring() ? "Yes" : "No") + ")");
            boolean active = ConsoleUtils.confirm(
                    "Active? (current: " + (concept.isActive() ? "Yes" : "No") + ")");

            if (!code.equals(concept.getConceptCode()) && conceptDao.findByCode(code) != null) {
                ConsoleUtils.printError("A concept with code " + code + " already exists.");
                return;
            }
            concept.setConceptCode(code);
            concept.setConceptName(name);
            concept.setDescription(description);
            concept.setBaseAmount(BigDecimal.valueOf(baseAmount).setScale(2, RoundingMode.HALF_UP));
            concept.setRecurring(recurring);
            concept.setActive(active);
            concept.setUpdateDate(LocalDateTime.now());

            if (conceptDao.update(concept)) {
                ConsoleUtils.printSuccess("Concept updated.");
            } else {
                ConsoleUtils.printError("The concept could not be updated.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while updating the concept.");
            LoggerUtil.logError("PaymentConceptController.updateConcept", exception);
        }
    }

    /** Deletes a concept (falls back to advice when it has linked payments). */
    public void deleteConcept() {
        ConsoleUtils.printHeader("DELETE PAYMENT CONCEPT");
        try {
            PaymentConcept concept = selectConcept();
            if (concept == null) {
                return;
            }
            if (!ConsoleUtils.confirm("Delete concept " + concept.getConceptCode() + " - "
                    + concept.getConceptName() + "?")) {
                System.out.println("  Operation cancelled.");
                return;
            }
            if (conceptDao.delete(concept.getId())) {
                ConsoleUtils.printSuccess("Concept deleted.");
            } else {
                ConsoleUtils.printError("The concept could not be deleted.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Cannot delete: the concept has linked payments. "
                    + "Consider deactivating it instead.");
            LoggerUtil.logError("PaymentConceptController.deleteConcept", exception);
        }
    }

    /**
     * Lets the user choose one ACTIVE concept for a payment (0 = cancel).
     * Public because the payment registration flow uses it.
     */
    public PaymentConcept selectActiveConcept() throws SQLException {
        List<PaymentConcept> concepts = conceptDao.findAllActive();
        if (concepts.isEmpty()) {
            ConsoleUtils.printError("No active payment concepts registered yet.");
            return null;
        }
        System.out.println("  Active concepts:");
        concepts.forEach(concept -> System.out.printf("    %d. %s (%s) - %s%n",
                concept.getId(),
                concept.getConceptCode(),
                concept.getConceptName(),
                InvoiceGenerator.formatMoney(concept.getBaseAmount())));
        int conceptId = ConsoleUtils.readInt("Concept id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (conceptId == 0) {
            return null;
        }
        PaymentConcept concept = conceptDao.findById(conceptId);
        if (concept == null || !concept.isActive()) {
            ConsoleUtils.printError("Concept " + conceptId + " not found or inactive.");
            return null;
        }
        return concept;
    }

    /** Asks for a concept id and loads it (0 = cancel). */
    private PaymentConcept selectConcept() throws SQLException {        List<PaymentConcept> concepts = conceptDao.findAll();
        if (concepts.isEmpty()) {
            System.out.println("  No payment concepts registered yet.");
            return null;
        }
        int maxId = concepts.stream().mapToInt(PaymentConcept::getId).max().orElse(0);
        int conceptId = ConsoleUtils.readInt("Concept id (0 = cancel): ", 1, maxId, 0);
        if (conceptId == 0) {
            return null;
        }
        PaymentConcept concept = conceptDao.findById(conceptId);
        if (concept == null) {
            ConsoleUtils.printError("Concept " + conceptId + " not found.");
            return null;
        }
        return concept;
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }
}
