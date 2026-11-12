package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ReportDao;
import com.littlehorizons.erp.dao.impl.ReportDaoImpl;
import com.littlehorizons.erp.model.AcademicReportResult;
import com.littlehorizons.erp.model.AttendanceAlert;
import com.littlehorizons.erp.model.AttendancePercentage;
import com.littlehorizons.erp.model.AttendanceRecord;
import com.littlehorizons.erp.model.AttendanceReportResult;
import com.littlehorizons.erp.model.ClubOccupancyReport;
import com.littlehorizons.erp.model.ConceptCollection;
import com.littlehorizons.erp.model.DelinquencyReport;
import com.littlehorizons.erp.model.EventParticipationRecord;
import com.littlehorizons.erp.model.ExtracurricularReportResult;
import com.littlehorizons.erp.model.FinancialReportResult;
import com.littlehorizons.erp.model.GradeRecord;
import com.littlehorizons.erp.model.LastPaymentRecord;
import com.littlehorizons.erp.model.LoanOverdueReport;
import com.littlehorizons.erp.model.MonthlyCollection;
import com.littlehorizons.erp.model.PaymentRecord;
import com.littlehorizons.erp.model.ReportRow;
import com.littlehorizons.erp.model.ResourcesReportResult;
import com.littlehorizons.erp.model.StudentAverageReport;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Orchestrates the reports and statistics module (main menu option 7).
 *
 * <p>Fetches read-only rows through {@link ReportDao} and turns them into the
 * report holders using the Stream API prominently (groupingBy, summarizing,
 * comparing, counting, joining...).</p>
 */
public class ReportController {

    /** General average below this value triggers an academic alert. */
    public static final double ACADEMIC_ALERT_THRESHOLD = 3.0;

    /** More absences than this in a single week trigger an attendance alert. */
    public static final int MAX_WEEKLY_ABSENCES = 3;

    /** Days without payments after which a student is considered delinquent. */
    public static final int DELINQUENCY_DAYS = 30;

    /** Current academic year of the school. */
    public static final int CURRENT_ACADEMIC_YEAR = LocalDate.now().getYear();

    /** Number of students shown in the academic top ranking. */
    private static final int TOP_STUDENTS = 5;

    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH);

    /** Ranking value of the podium positions defined in the database. */
    private static final Map<String, Integer> PODIUM_RANK = Map.of(
            "1er Lugar", 1, "2do Lugar", 2, "3er Lugar", 3);

    private final ReportDao reportDao;

    /** Creates the controller with its own DAO implementation. */
    public ReportController() {
        this(new ReportDaoImpl());
    }

    /** Creates the controller with an injected DAO (useful for testing). */
    public ReportController(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    // ---------- 1. General statistics dashboard ----------

    /**
     * Assembles the general statistics dashboard with entity counts, resource
     * states and total money collected this year.
     */
    public List<ReportRow> buildDashboard() throws SQLException {
        double totalPaymentsThisYear =
                reportDao.totalPaymentsInYear(CURRENT_ACADEMIC_YEAR);

        List<ReportRow> rows = List.of(
                new ReportRow("Total active students",
                        String.valueOf(reportDao.countActiveStudents())),
                new ReportRow("Total active teachers",
                        String.valueOf(reportDao.countActiveTeachers())),
                new ReportRow("Total active subjects",
                        String.valueOf(reportDao.countActiveSubjects())),
                new ReportRow("Total active clubs",
                        String.valueOf(reportDao.countActiveClubs())),
                new ReportRow("Payments collected in " + CURRENT_ACADEMIC_YEAR,
                        String.format(Locale.US, "$%,.2f", totalPaymentsThisYear)));

        List<ReportRow> resourcesByState = reportDao.countResourcesByState().stream()
                .map(row -> new ReportRow("Resources - " + row.label(), row.value()))
                .toList();

        return Stream.concat(rows.stream(), resourcesByState.stream()).toList();
    }

    // ---------- 2. Academic report ----------

    /** Builds the academic report: averages, top 5 and low-average alerts. */
    public AcademicReportResult buildAcademicReport(int year) throws SQLException {
        List<GradeRecord> grades = reportDao.findGradesByYear(year);

        Map<Long, List<GradeRecord>> gradesByStudent = grades.stream()
                .collect(Collectors.groupingBy(GradeRecord::studentId,
                        TreeMap::new,
                        Collectors.toList()));

        List<StudentAverageReport> allStudents = gradesByStudent.values().stream()
                .map(this::toStudentAverage)
                .toList();

        List<StudentAverageReport> topFive = allStudents.stream()
                .sorted(Comparator.comparing(StudentAverageReport::generalAverage)
                        .reversed())
                .limit(TOP_STUDENTS)
                .toList();

        List<StudentAverageReport> alerts = allStudents.stream()
                .filter(report -> report.generalAverage() < ACADEMIC_ALERT_THRESHOLD)
                .sorted(Comparator.comparing(StudentAverageReport::generalAverage))
                .toList();

        return new AcademicReportResult(allStudents, topFive, alerts);
    }

    /**
     * Computes one student's averages. For every subject, the 'Final' grade is
     * used when present; otherwise the average of the I-IV periods is taken.
     */
    private StudentAverageReport toStudentAverage(List<GradeRecord> records) {
        GradeRecord first = records.get(0);

        Map<String, Double> subjectAverages = records.stream()
                .collect(Collectors.groupingBy(GradeRecord::subjectName,
                        java.util.LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(),
                                this::resolveSubjectGrade)));

        double generalAverage = subjectAverages.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return new StudentAverageReport(first.studentId(), first.studentName(),
                first.gradeName(), subjectAverages, generalAverage);
    }

    /** Final grade wins over the average of the remaining periods. */
    private double resolveSubjectGrade(List<GradeRecord> subjectRecords) {
        return subjectRecords.stream()
                .filter(record -> "Final".equals(record.period()))
                .findFirst()
                .map(GradeRecord::note)
                .orElseGet(() -> subjectRecords.stream()
                        .mapToDouble(GradeRecord::note)
                        .average()
                        .orElse(0.0));
    }

    // ---------- 3. Attendance report ----------

    /** Builds the attendance report: weekly absence alerts and percentages. */
    public AttendanceReportResult buildAttendanceReport(int year)
            throws SQLException {
        List<AttendanceRecord> records = reportDao.findAttendanceByYear(year);

        Map<Long, Map<String, List<AttendanceRecord>>> byStudentAndWeek =
                records.stream()
                        .collect(Collectors.groupingBy(AttendanceRecord::studentId,
                                Collectors.groupingBy(
                                        record -> weekKey(record.attendanceDate()))));

        List<AttendanceAlert> weeklyAlerts = byStudentAndWeek.entrySet().stream()
                .flatMap(studentEntry -> studentEntry.getValue().entrySet().stream()
                        .map(weekEntry -> toAttendanceAlert(
                                weekEntry.getKey(), weekEntry.getValue())))
                .filter(alert -> alert.absences() > MAX_WEEKLY_ABSENCES)
                .sorted(Comparator.comparingInt(AttendanceAlert::absences)
                        .reversed()
                        .thenComparing(AttendanceAlert::studentName))
                .toList();

        List<AttendancePercentage> studentPercentages = byStudentAndWeek.entrySet()
                .stream()
                .map(studentEntry -> {
                    List<AttendanceRecord> studentRecords =
                            studentEntry.getValue().values().stream()
                                    .flatMap(List::stream)
                                    .toList();
                    long attended = studentRecords.stream()
                            .filter(record -> !"Ausente".equals(record.attendanceState()))
                            .count();
                    double percentage = attended * 100.0 / studentRecords.size();
                    return new AttendancePercentage(
                            studentRecords.get(0).studentName(),
                            attended, studentRecords.size(), percentage);
                })
                .sorted(Comparator.comparing(AttendancePercentage::percentage)
                        .reversed())
                .toList();

        return new AttendanceReportResult(weeklyAlerts, studentPercentages);
    }

    /** Builds a weekly alert from all the records of one student in one week. */
    private AttendanceAlert toAttendanceAlert(String weekLabel,
                                              List<AttendanceRecord> weekRecords) {
        AttendanceRecord first = weekRecords.get(0);
        long absences = weekRecords.stream()
                .filter(record -> "Ausente".equals(record.attendanceState()))
                .count();
        double weeklyPercentage = attendancePercentage(weekRecords);
        return new AttendanceAlert(first.studentId(), first.studentName(),
                weekLabel, (int) absences, weekRecords.size(), weeklyPercentage);
    }

    /** Records that are not 'Ausente' count as attended. */
    private double attendancePercentage(List<AttendanceRecord> records) {
        if (records.isEmpty()) {
            return 0.0;
        }
        long attended = records.stream()
                .filter(record -> !"Ausente".equals(record.attendanceState()))
                .count();
        return attended * 100.0 / records.size();
    }

    /** ISO week label such as "2026-W14". */
    private String weekKey(LocalDate date) {
        return String.format(Locale.US, "%d-W%02d",
                date.get(IsoFields.WEEK_BASED_YEAR),
                date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
    }

    // ---------- 4. Financial report ----------

    /** Builds the financial report: concepts, months and delinquency. */
    public FinancialReportResult buildFinancialReport(int year)
            throws SQLException {
        List<PaymentRecord> payments = reportDao.findPaymentsByYear(year);

        Map<String, DoubleSummaryStatistics> conceptStats = payments.stream()
                .collect(Collectors.groupingBy(PaymentRecord::conceptName,
                        TreeMap::new,
                        Collectors.summarizingDouble(PaymentRecord::amount)));

        List<ConceptCollection> conceptTotals = conceptStats.entrySet().stream()
                .map(entry -> new ConceptCollection(entry.getKey(),
                        entry.getValue().getSum(), entry.getValue().getCount()))
                .toList();

        List<MonthlyCollection> monthTotals = payments.stream()
                .collect(Collectors.groupingBy(
                        payment -> YearMonth.from(payment.paymentDate()),
                        TreeMap::new,
                        Collectors.summarizingDouble(PaymentRecord::amount)))
                .entrySet().stream()
                .map(entry -> new MonthlyCollection(
                        entry.getKey().format(MONTH_FORMAT),
                        entry.getValue().getSum()))
                .toList();

        LocalDate today = LocalDate.now();
        List<DelinquencyReport> delinquency = reportDao.findLastPaymentDates().stream()
                .map(lastPayment -> toDelinquencyReport(lastPayment, today))
                .filter(report -> report.daysSinceLastPayment() < 0
                        || report.daysSinceLastPayment() > DELINQUENCY_DAYS)
                .sorted(Comparator.comparingLong(DelinquencyReport::daysSinceLastPayment)
                        .reversed())
                .toList();

        double totalCollected = payments.stream()
                .mapToDouble(PaymentRecord::amount)
                .sum();

        return new FinancialReportResult(conceptTotals, monthTotals, delinquency,
                totalCollected);
    }

    /** Maps the last payment date into a delinquency row for today. */
    private DelinquencyReport toDelinquencyReport(LastPaymentRecord lastPayment,
                                                  LocalDate today) {
        LocalDate lastDate = lastPayment.lastPaymentDate();
        long daysSince = lastDate == null
                ? -1
                : ChronoUnit.DAYS.between(lastDate, today);
        return new DelinquencyReport(lastPayment.studentId(),
                lastPayment.studentName(), lastDate, daysSince);
    }

    // ---------- 5. Extracurricular report ----------

    /** Builds the extracurricular report: clubs, participation and podium. */
    public ExtracurricularReportResult buildExtracurricularReport(int year)
            throws SQLException {
        List<ClubOccupancyReport> clubOccupancy = reportDao.findClubOccupancy().stream()
                .sorted(Comparator.comparingDouble(ClubOccupancyReport::occupancyPercentage)
                        .reversed())
                .toList();

        List<EventParticipationRecord> participations =
                reportDao.findEventParticipations(year);

        List<ReportRow> participationPerStudent = participations.stream()
                .collect(Collectors.groupingBy(
                        EventParticipationRecord::studentName,
                        TreeMap::new,
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new ReportRow(entry.getKey(),
                        entry.getValue() + (entry.getValue() == 1 ? " event" : " events")))
                .toList();

        List<ReportRow> rankedStudents = participations.stream()
                .filter(record -> record.position() != null
                        && PODIUM_RANK.containsKey(record.position()))
                .sorted(Comparator
                        .comparing((EventParticipationRecord record) ->
                                PODIUM_RANK.get(record.position()))
                        .thenComparing(EventParticipationRecord::studentName))
                .collect(Collectors.groupingBy(
                        EventParticipationRecord::studentName,
                        java.util.LinkedHashMap::new,
                        Collectors.mapping(EventParticipationRecord::position,
                                Collectors.toList())))
                .entrySet().stream()
                .map(entry -> new ReportRow(entry.getKey(),
                        String.join(", ", entry.getValue())))
                .toList();

        return new ExtracurricularReportResult(clubOccupancy,
                participationPerStudent, rankedStudents);
    }

    // ---------- 6. Resources report ----------

    /** Builds the resources report: loans, overdue loans and inventory. */
    public ResourcesReportResult buildResourcesReport(LocalDate referenceDate)
            throws SQLException {
        List<LoanOverdueReport> overdueLoans = reportDao.findOverdueLoans(referenceDate)
                .stream()
                .sorted(Comparator.comparingLong(LoanOverdueReport::daysOverdue)
                        .reversed())
                .toList();

        return new ResourcesReportResult(reportDao.countLoansByState(),
                overdueLoans,
                reportDao.countInventoryByCategoryAndState());
    }
}
