package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ReportController;
import com.littlehorizons.erp.dao.impl.ReportDaoImpl;
import com.littlehorizons.erp.model.AcademicReportResult;
import com.littlehorizons.erp.model.AttendanceAlert;
import com.littlehorizons.erp.model.AttendancePercentage;
import com.littlehorizons.erp.model.AttendanceReportResult;
import com.littlehorizons.erp.model.ClubOccupancyReport;
import com.littlehorizons.erp.model.ConceptCollection;
import com.littlehorizons.erp.model.DelinquencyReport;
import com.littlehorizons.erp.model.ExtracurricularReportResult;
import com.littlehorizons.erp.model.FinancialReportResult;
import com.littlehorizons.erp.model.LoanOverdueReport;
import com.littlehorizons.erp.model.ReportRow;
import com.littlehorizons.erp.model.ResourcesReportResult;
import com.littlehorizons.erp.model.StudentAverageReport;
import com.littlehorizons.erp.model.UserRole;
import com.littlehorizons.erp.util.AppConstants;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Console view of the reports and statistics module (main menu option 7).
 *
 * <p>Handles every user interaction through {@link ConsoleUtils} and delegates
 * all the computation to {@link ReportController}. Any {@link SQLException} is
 * reported to the user and logged to the error log file.</p>
 */
public class ReportsView {

    private final ReportController reportController;
    private final UserRole userRole;

    /** Creates the view with its own controller and DAO implementation. */
    public ReportsView() {
        this(UserRole.ADMINISTRATOR);
    }

    public ReportsView(UserRole userRole) {
        this.userRole = userRole;
        this.reportController = new ReportController(new ReportDaoImpl());
    }

    /** Shows the reports menu until the user chooses 0 (back). */
    public void show() {
        while (true) {
            ConsoleUtils.printHeader("REPORTS AND STATISTICS");
            System.out.println("  1. General statistics dashboard");
            System.out.println("  2. Academic report");
            System.out.println("  3. Attendance report");
            System.out.println("  4. Financial report");
            System.out.println("  5. Extracurricular report");
            System.out.println("  6. Resources report");
            System.out.println("  0. Back to main menu");

            int option = ConsoleUtils.readMenuOption("Select an option: ", 6);
            if (option == 0) {
                return;
            }
            switch (option) {
                case 1 -> showDashboard();
                case 2 -> showAcademicReport();
                case 3 -> showAttendanceReport();
                case 4 -> showFinancialReport();
                case 5 -> showExtracurricularReport();
                case 6 -> showResourcesReport();
                default -> ConsoleUtils.printError("Unknown option.");
            }
        }
    }

    // ---------- 1. Dashboard ----------

    private void showDashboard() {
        ConsoleUtils.printHeader("GENERAL STATISTICS DASHBOARD");
        try {
            List<ReportRow> rows = reportController.buildDashboard();
            rows.forEach(row -> System.out.printf("  %-55s %s%n",
                    row.label(), row.value()));
        } catch (SQLException exception) {
            handleSqlError("General statistics dashboard", exception);
        }
        ConsoleUtils.pause();
    }

    // ---------- 2. Academic report ----------

    private void showAcademicReport() {
        ConsoleUtils.printHeader("ACADEMIC REPORT");
        Integer year = readAcademicYear();
        if (year == null) {
            return;
        }
        try {
            AcademicReportResult result = reportController.buildAcademicReport(year);

            System.out.println("\n  PER-STUDENT AVERAGES");
            if (result.allStudents().isEmpty()) {
                System.out.println("  No grades registered for " + year + ".");
            }
            result.allStudents().forEach(this::printStudentAverage);

            System.out.println("\n  TOP " + result.topFive().size()
                    + " STUDENTS BY GENERAL AVERAGE");
            if (result.topFive().isEmpty()) {
                System.out.println("  No grades registered for " + year + ".");
            }
            result.topFive().forEach(report -> System.out.printf(
                    "  #%d  %-40s (%s)  Average: %.2f%n",
                    result.topFive().indexOf(report) + 1,
                    report.studentName(), report.gradeName(),
                    report.generalAverage()));

            System.out.println("\n  ACADEMIC ALERTS (average below "
                    + ReportController.ACADEMIC_ALERT_THRESHOLD + ")");
            if (result.academicAlerts().isEmpty()) {
                ConsoleUtils.printSuccess(
                        "No student is below the alert threshold. Congratulations!");
            }
            result.academicAlerts().forEach(report -> System.out.printf(
                    "  [!] %-40s (%s)  Average: %.2f%n",
                    report.studentName(), report.gradeName(),
                    report.generalAverage()));
        } catch (SQLException exception) {
            handleSqlError("Academic report", exception);
        }
        ConsoleUtils.pause();
    }

    /** Prints one student with the average of every subject and the general. */
    private void printStudentAverage(StudentAverageReport report) {
        System.out.printf("  %-40s (%s)  General: %.2f%n",
                report.studentName(), report.gradeName(),
                report.generalAverage());
        String subjects = report.subjectAverages().entrySet().stream()
                .map(entry -> String.format(Locale.US, "%s %.1f",
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" | "));
        System.out.println("      " + subjects);
    }

    // ---------- 3. Attendance report ----------

    private void showAttendanceReport() {
        ConsoleUtils.printHeader("ATTENDANCE REPORT");
        Integer year = readAcademicYear();
        if (year == null) {
            return;
        }
        try {
            AttendanceReportResult result = reportController.buildAttendanceReport(year);

            System.out.println("\n  WEEKLY ABSENCE ALERTS (more than "
                    + ReportController.MAX_WEEKLY_ABSENCES
                    + " absences in one ISO week)");
            if (result.weeklyAlerts().isEmpty()) {
                ConsoleUtils.printSuccess(
                        "No student exceeded the allowed absences in any week.");
            }
            System.out.printf("  %-40s %-10s %9s %9s %10s%n",
                    "Student", "Week", "Absences", "Records", "Week %");
            result.weeklyAlerts().forEach(alert -> printAttendanceAlert(alert));

            System.out.println("\n  OVERALL ATTENDANCE PERCENTAGE");
            System.out.printf("  %-40s %12s %14s%n", "Student", "Attended", "Percentage");
            result.studentPercentages().forEach(this::printAttendancePercentage);
        } catch (SQLException exception) {
            handleSqlError("Attendance report", exception);
        }
        ConsoleUtils.pause();
    }

    private void printAttendanceAlert(AttendanceAlert alert) {
        System.out.printf("  %-40s %-10s %9d %9d %9.1f%%%n",
                alert.studentName(), alert.weekLabel(), alert.absences(),
                alert.weekRecords(), alert.weeklyPercentage());
    }

    private void printAttendancePercentage(AttendancePercentage percentage) {
        System.out.printf("  %-40s %7d/%-4d %13.1f%%%n",
                percentage.studentName(), percentage.attended(),
                percentage.totalRecords(), percentage.percentage());
    }

    // ---------- 4. Financial report ----------

    private void showFinancialReport() {
        if (!userRole.canViewFinancialData()) {
            ConsoleUtils.printError("You are not authorized to view financial reports.");
            return;
        }
        ConsoleUtils.printHeader("FINANCIAL REPORT");
        Integer year = readAcademicYear();
        if (year == null) {
            return;
        }
        try {
            FinancialReportResult result = reportController.buildFinancialReport(year);

            System.out.println("\n  TOTAL COLLECTED IN " + year + ": "
                    + money(result.totalCollected()));

            System.out.println("\n  COLLECTED PER CONCEPT");
            System.out.printf("  %-35s %10s %16s%n",
                    "Concept", "Payments", "Total collected");
            if (result.conceptTotals().isEmpty()) {
                System.out.println("  No payments registered for " + year + ".");
            }
            result.conceptTotals().forEach(concept -> System.out.printf(
                    "  %-35s %10d %16s%n", concept.conceptName(),
                    concept.paymentCount(), money(concept.totalCollected())));

            System.out.println("\n  COLLECTED PER MONTH");
            System.out.printf("  %-12s %16s%n", "Month", "Total collected");
            result.monthTotals().forEach(month -> System.out.printf(
                    "  %-12s %16s%n", month.monthLabel(),
                    money(month.totalCollected())));

            System.out.println("\n  DELINQUENCY (active students with no payment"
                    + " in the last " + ReportController.DELINQUENCY_DAYS + " days)");
            if (result.delinquency().isEmpty()) {
                ConsoleUtils.printSuccess("Every active student is up to date.");
            }
            System.out.printf("  %-40s %-14s %s%n",
                    "Student", "Last payment", "Days since");
            result.delinquency().forEach(this::printDelinquencyReport);
        } catch (SQLException exception) {
            handleSqlError("Financial report", exception);
        }
        ConsoleUtils.pause();
    }

    private void printDelinquencyReport(DelinquencyReport report) {
        String lastPayment = report.lastPaymentDate() == null
                ? "Never paid"
                : report.lastPaymentDate().toString();
        String days = report.daysSinceLastPayment() < 0
                ? "No payments"
                : report.daysSinceLastPayment() + " days";
        System.out.printf("  %-40s %-14s %s%n",
                report.studentName(), lastPayment, days);
    }

    // ---------- 5. Extracurricular report ----------

    private void showExtracurricularReport() {
        ConsoleUtils.printHeader("EXTRACURRICULAR REPORT");
        Integer year = readAcademicYear();
        if (year == null) {
            return;
        }
        try {
            ExtracurricularReportResult result =
                    reportController.buildExtracurricularReport(year);

            System.out.println("\n  CLUB OCCUPANCY (busiest first)");
            System.out.printf("  %-30s %-30s %9s %10s %10s%n",
                    "Club", "Supervisor", "Members", "Capacity", "Occupancy");
            if (result.clubOccupancy().isEmpty()) {
                System.out.println("  No active clubs registered.");
            }
            result.clubOccupancy().forEach(this::printClubOccupancy);

            System.out.println("\n  EVENT PARTICIPATION PER STUDENT");
            if (result.participationPerStudent().isEmpty()) {
                System.out.println("  No event participations for " + year + ".");
            }
            result.participationPerStudent().forEach(row ->
                    System.out.printf("  %-40s %s%n", row.label(), row.value()));

            System.out.println("\n  PODIUM POSITIONS (competitive events)");
            if (result.rankedStudents().isEmpty()) {
                System.out.println("  No podium positions for " + year + ".");
            }
            result.rankedStudents().forEach(row ->
                    System.out.printf("  %-40s %s%n", row.label(), row.value()));
        } catch (SQLException exception) {
            handleSqlError("Extracurricular report", exception);
        }
        ConsoleUtils.pause();
    }

    private void printClubOccupancy(ClubOccupancyReport club) {
        System.out.printf("  %-30s %-30s %9d %10d %9.1f%%%n",
                club.clubName(), club.supervisorName(), club.activeMembers(),
                club.capacity(), club.occupancyPercentage());
    }

    // ---------- 6. Resources report ----------

    private void showResourcesReport() {
        ConsoleUtils.printHeader("RESOURCES REPORT");
        try {
            ResourcesReportResult result =
                    reportController.buildResourcesReport(LocalDate.now());

            System.out.println("\n  LOANS BY STATE (students + professors)");
            result.loansByState().forEach(row ->
                    System.out.printf("  %-40s %s%n", row.label(), row.value()));

            System.out.println("\n  OVERDUE LOANS (oldest first)");
            if (result.overdueLoans().isEmpty()) {
                ConsoleUtils.printSuccess("No overdue loans. Everything on time!");
            }
            System.out.printf("  %-10s %-30s %-30s %-12s %-12s %s%n",
                    "Type", "Resource", "Holder", "Loan date", "Expected", "Days late");
            result.overdueLoans().forEach(this::printOverdueLoan);

            System.out.println("\n  INVENTORY BY CATEGORY AND STATE");
            result.inventory().forEach(row ->
                    System.out.printf("  %-50s %s%n", row.label(), row.value()));
        } catch (SQLException exception) {
            handleSqlError("Resources report", exception);
        }
        ConsoleUtils.pause();
    }

    private void printOverdueLoan(LoanOverdueReport loan) {
        System.out.printf("  %-10s %-30s %-30s %-12s %-12s %d%n",
                loan.loanType(), loan.resourceName(), loan.holderName(),
                loan.loanDate(), loan.expectedReturnDate(), loan.daysOverdue());
    }

    // ---------- Input helpers ----------

    /**
     * Asks for an academic year; returns {@code null} when the user types 0 to
     * cancel, in which case the report is skipped silently.
     */
    private Integer readAcademicYear() {
        int year = ConsoleUtils.readInt(
                "  Enter academic year (0 = cancel): ",
                AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
        return year == 0 ? null : year;
    }

    /** Formats an amount of Colombian pesos for console display. */
    private String money(double amount) {
        return String.format(Locale.US, "$%,.2f", amount);
    }

    /** Shows the error to the user and records it in the log file. */
    private void handleSqlError(String context, SQLException exception) {
        ConsoleUtils.printError("Could not generate the report: "
                + exception.getMessage());
        LoggerUtil.logError(context, exception);
    }
}
