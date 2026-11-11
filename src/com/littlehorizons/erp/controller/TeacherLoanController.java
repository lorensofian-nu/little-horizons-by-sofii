package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.TeacherLoanDao;
import com.littlehorizons.erp.dao.impl.ResourceDaoImpl;
import com.littlehorizons.erp.dao.impl.TeacherLoanDaoImpl;
import com.littlehorizons.erp.model.LoanState;
import com.littlehorizons.erp.model.LoanSummary;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceState;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.model.TeacherSummary;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for loans of resources to teachers: registration, returns,
 * listings and the overdue report. All business rules of the loan state
 * machine are enforced here.
 */
public class TeacherLoanController {

    private final TeacherLoanDao loanDao = new TeacherLoanDaoImpl();
    private final ResourceDao resourceDao = new ResourceDaoImpl();

    /** Registers a new loan of an available resource to an active teacher. */
    public void registerLoan() {
        try {
            ConsoleUtils.printHeader("NEW LOAN TO TEACHER");
            Resource resource = pickAvailableResource();
            if (resource == null) {
                return;
            }
            Integer teacherId = pickTeacherId();
            if (teacherId == null) {
                return;
            }
            LocalDate loanDate = ConsoleUtils.readOptionalDate("Loan date", LocalDate.now());
            LocalDate expectedReturnDate = readExpectedReturnDate(loanDate);
            String observations = ConsoleUtils.readOptionalText("Observations (optional): ", null);

            TeacherLoan loan = new TeacherLoan();
            loan.setResourceId(resource.getId());
            loan.setTeacherId(teacherId);
            loan.setLoanDate(loanDate);
            loan.setExpectedReturnDate(expectedReturnDate);
            loan.setRealReturnDate(null);
            loan.setState(LoanState.ACTIVE);
            loan.setObservations(observations);

            loanDao.saveAndLoanResource(loan);
            ConsoleUtils.printSuccess("Loan #" + loan.getId() + " registered. Resource '"
                    + resource.getCode() + "' is now Prestado.");
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            if (exception.getMessage() != null
                    && exception.getMessage().contains("no longer available")) {
                ConsoleUtils.printError(exception.getMessage());
            } else {
                ConsoleUtils.printError("Could not register the loan.");
            }
            LoggerUtil.logError("TeacherLoanController.registerLoan", exception);
        }
    }

    /** Registers the return of an active loan. */
    public void registerReturn() {
        try {
            ConsoleUtils.printHeader("REGISTER LOAN RETURN");
            List<LoanSummary> activeLoans = loanDao.findActiveSummaries();
            if (activeLoans.isEmpty()) {
                ConsoleUtils.printError("There are no active teacher loans to return.");
                ConsoleUtils.pause();
                return;
            }
            printLoanTable(activeLoans);
            int loanId = ConsoleUtils.readInt("Loan id to return (0 = back): ", 1, Integer.MAX_VALUE, 0);
            if (loanId == 0) {
                return;
            }
            boolean active = activeLoans.stream()
                    .anyMatch(loan -> loan.getId() == loanId);
            if (!active) {
                ConsoleUtils.printError("Loan #" + loanId
                        + " is not an active loan. Only active loans can be returned.");
                ConsoleUtils.pause();
                return;
            }
            LoanSummary loan = activeLoans.stream()
                    .filter(candidate -> candidate.getId() == loanId)
                    .findFirst()
                    .orElseThrow();
            String observations = ConsoleUtils.readOptionalText("Return observations (optional): ", null);

            LocalDate today = LocalDate.now();
            if (loanDao.registerReturn(loanId, today, observations)) {
                ConsoleUtils.printSuccess("Loan #" + loanId + " returned on " + today
                        + ". Resource is back to Disponible.");
                if (today.isAfter(loan.getExpectedReturnDate())) {
                    ConsoleUtils.printError("This loan was returned LATE ("
                            + ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), today)
                            + " day(s) overdue).");
                }
            } else {
                ConsoleUtils.printError("The loan could not be returned (state machine violation).");
            }
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not register the loan return.");
            LoggerUtil.logError("TeacherLoanController.registerReturn", exception);
        }
    }

    /** Lists every active (or overdue) teacher loan. */
    public void listActiveLoans() {
        try {
            ConsoleUtils.printHeader("ACTIVE TEACHER LOANS");
            List<LoanSummary> activeLoans = loanDao.findActiveSummaries();
            if (activeLoans.isEmpty()) {
                ConsoleUtils.printError("There are no active teacher loans.");
            } else {
                printLoanTable(activeLoans);
            }
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not load the active teacher loans.");
            LoggerUtil.logError("TeacherLoanController.listActiveLoans", exception);
        }
    }

    /** Lists all loans (active and historical) of one teacher. */
    public void listLoansByTeacher() {
        try {
            ConsoleUtils.printHeader("LOANS BY TEACHER");
            Integer teacherId = pickTeacherId();
            if (teacherId == null) {
                return;
            }
            List<LoanSummary> loans = loanDao.findSummariesByTeacher(teacherId);
            if (loans.isEmpty()) {
                ConsoleUtils.printError("That teacher has no loan history.");
            } else {
                printLoanTable(loans);
            }
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not load the teacher loan history.");
            LoggerUtil.logError("TeacherLoanController.listLoansByTeacher", exception);
        }
    }

    /**
     * Overdue loans report: first marks expired active loans as 'Vencido',
     * then shows them sorted by the oldest expected return date first.
     */
    public void showOverdueReport() {
        try {
            ConsoleUtils.printHeader("OVERDUE TEACHER LOANS REPORT");
            LocalDate today = LocalDate.now();
            int marked = loanDao.markOverdue(today);
            if (marked > 0) {
                System.out.println("  Marked " + marked + " loan(s) as Vencido.");
            }
            List<LoanSummary> overdue = loanDao.findOverdueSummaries(today).stream()
                    .sorted(Comparator.comparing(LoanSummary::getExpectedReturnDate))
                    .collect(Collectors.toList());
            if (overdue.isEmpty()) {
                ConsoleUtils.printSuccess("No overdue teacher loans. Well done!");
            } else {
                System.out.println("Total overdue: " + overdue.size());
                printLoanTable(overdue);
            }
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not generate the overdue report.");
            LoggerUtil.logError("TeacherLoanController.showOverdueReport", exception);
        }
    }

    // ------------------------------------------------------------------
    // Shared helpers
    // ------------------------------------------------------------------

    /** Prints a formatted loan table. */
    private void printLoanTable(List<LoanSummary> loans) {
        System.out.printf("%-4s | %-12s | %-26s | %-22s | %-10s | %-10s | %s%n",
                "ID", "CODE", "RESOURCE", "TEACHER", "LOAN", "DUE", "STATE");
        System.out.println("---------------------------------------------------------------------------"
                + "---------------------------");
        loans.forEach(loan -> System.out.printf(
                "%-4d | %-12s | %-26s | %-22s | %-10s | %-10s | %s%n",
                loan.getId(),
                loan.getResourceCode(),
                loan.getResourceName(),
                loan.getBorrowerName(),
                loan.getLoanDate(),
                loan.getExpectedReturnDate(),
                loan.getState().getDbValue()));
    }

    /** Interactive picker restricted to resources in state 'Disponible'. */
    private Resource pickAvailableResource() throws SQLException {
        List<Resource> available = resourceDao.findByState(ResourceState.AVAILABLE);
        if (available.isEmpty()) {
            ConsoleUtils.printError("There are no resources in state 'Disponible' right now.");
            return null;
        }
        System.out.println("Available resources (Disponible):");
        available.forEach(resource -> System.out.printf("  %d. %s - %s%n",
                resource.getId(), resource.getCode(), resource.getName()));
        int resourceId = ConsoleUtils.readInt("Resource id (0 = back): ", 1, Integer.MAX_VALUE, 0);
        if (resourceId == 0) {
            return null;
        }
        Resource chosen = available.stream()
                .filter(resource -> resource.getId() == resourceId)
                .findFirst()
                .orElse(null);
        if (chosen == null) {
            ConsoleUtils.printError("Resource id " + resourceId
                    + " is not in state 'Disponible'. Only available resources can be loaned.");
        }
        return chosen;
    }

    /** Interactive teacher picker (active teachers only); null cancels. */
    private Integer pickTeacherId() throws SQLException {
        List<TeacherSummary> teachers = loanDao.listActiveTeachers();
        if (teachers.isEmpty()) {
            ConsoleUtils.printError("There are no active teachers registered.");
            return null;
        }
        System.out.println("Active teachers:");
        teachers.forEach(teacher -> System.out.printf("  %d. %s%n",
                teacher.getTeacherId(), teacher.getFullName()));
        int teacherId = ConsoleUtils.readInt("Teacher id (0 = back): ", 1, Integer.MAX_VALUE, 0);
        if (teacherId == 0) {
            return null;
        }
        boolean valid = teachers.stream().anyMatch(teacher -> teacher.getTeacherId() == teacherId);
        if (!valid) {
            ConsoleUtils.printError("Teacher id " + teacherId + " is not an active teacher.");
            return null;
        }
        return teacherId;
    }

    /** Reads the expected return date, enforcing it is strictly after the loan date. */
    private LocalDate readExpectedReturnDate(LocalDate loanDate) {
        while (true) {
            LocalDate expected = ConsoleUtils.readDate("Expected return date");
            if (expected.isAfter(loanDate)) {
                return expected;
            }
            ConsoleUtils.printError("The expected return date must be after the loan date ("
                    + loanDate + ").");
        }
    }
}
