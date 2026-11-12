package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.dao.impl.AcademicRecordDaoImpl;
import com.littlehorizons.erp.dao.impl.SubjectDaoImpl;
import com.littlehorizons.erp.dao.impl.SubjectEnrollmentDaoImpl;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Period;
import com.littlehorizons.erp.model.AcademicStudentSummary;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.AcademicTeacherSummary;
import com.littlehorizons.erp.util.AppConstants;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller of the grades module ({@code registros_academicos}).
 *
 * <p>Business rules: grade value between 0.0 and 5.0 with exactly one decimal
 * (validated and rounded), unique record per student + subject + year + period,
 * and updating an existing grade is allowed. All averages and reports are
 * computed in memory with the Stream API.</p>
 */
public class AcademicRecordController {

    /** Colombian passing threshold used to label the averages. */
    private static final double PASSING_GRADE = 3.0;

    private final AcademicRecordDao recordDao;
    private final SubjectDao subjectDao;
    private final SubjectEnrollmentDao enrollmentDao;

    /** Creates the controller with its default DAOs. */
    public AcademicRecordController() {
        this.recordDao = new AcademicRecordDaoImpl();
        this.subjectDao = new SubjectDaoImpl();
        this.enrollmentDao = new SubjectEnrollmentDaoImpl();
    }

    /** Shows the grades submenu. Option 0 goes back. */
    public void show() {
        while (true) {
            ConsoleUtils.printHeader("GRADES / QUALIFICATIONS");
            System.out.println(" 1. Register a grade");
            System.out.println(" 2. Update a grade");
            System.out.println(" 3. Delete a grade");
            System.out.println(" 4. Average per subject for a student (one year)");
            System.out.println(" 5. General average of a student for a period");
            System.out.println(" 6. Course average per subject");
            System.out.println(" 7. Full academic history of a student");
            int option = ConsoleUtils.readMenuOption(" Select an option: ", 7);
            switch (option) {
                case 1 -> registerGrade();
                case 2 -> updateGrade();
                case 3 -> deleteGrade();
                case 4 -> subjectAveragesForStudent();
                case 5 -> generalAverageForPeriod();
                case 6 -> courseAveragePerSubject();
                case 7 -> fullAcademicHistory();
                case 0 -> {
                    return;
                }
                default -> { /* unreachable */ }
            }
        }
    }

    // ------------------------------------------------------------------
    // Entry operations
    // ------------------------------------------------------------------

    private void registerGrade() {
        ConsoleUtils.printHeader("REGISTER GRADE");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year (0 = cancel): ", AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            AcademicStudentSummary student = readActiveStudent(year);
            if (student == null) {
                return;
            }
            Subject subject = readSubject();
            if (subject == null) {
                return;
            }
            AcademicTeacherSummary teacher = readTeacher();
            if (teacher == null) {
                return;
            }
            Period period = readPeriod(true);
            if (period == null) {
                return;
            }
            double grade = readGradeValue();
            String observations = ConsoleUtils.readOptionalText(" Observations (empty = none): ", null);
            LocalDate registrationDate = ConsoleUtils.readOptionalDate(" Registration date", LocalDate.now());

            // Unique per student + subject + year + period: offer to update instead.
            if (recordDao.existsFor(student.id(), subject.getId(), year, period, null)) {
                ConsoleUtils.printError("There is already a grade for " + student.fullName()
                        + " in " + subject.getCode() + ", " + period.getLabel() + " " + year + ".");
                if (ConsoleUtils.confirm(" Update the existing grade instead? ")) {
                    updateExisting(student, subject, year, period);
                }
                return;
            }
            AcademicRecord record = new AcademicRecord(
                    student.id(), subject.getId(), teacher.id(), year,
                    period, grade, observations, registrationDate);
            if (!ConsoleUtils.confirm(" Save grade " + formatGrade(grade)
                    + " for " + student.fullName() + " (" + subject.getCode() + ", "
                    + period.getLabel() + " " + year + ")? ")) {
                return;
            }
            recordDao.save(record);
            ConsoleUtils.printSuccess("Grade registered.");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while registering the grade.");
            LoggerUtil.logError("AcademicRecordController.registerGrade", exception);
        }
    }

    private void updateGrade() {
        ConsoleUtils.printHeader("UPDATE GRADE");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year (0 = cancel): ", AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            AcademicStudentSummary student = readActiveStudent(year);
            if (student == null) {
                return;
            }
            Period period = readPeriod(true);
            if (period == null) {
                return;
            }
            List<AcademicRecord> records = recordDao.findByStudentYearPeriod(student.id(), year, period);
            if (records.isEmpty()) {
                ConsoleUtils.printError("No grades found for that student, year and period.");
                return;
            }
            Map<Integer, AcademicRecord> byId = records.stream()
                    .collect(Collectors.toMap(AcademicRecord::getId, Function.identity()));
            records.forEach(record -> System.out.println("  " + describeRecord(record)));
            int recordId = ConsoleUtils.readInt(" Record id to update (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (recordId == 0) {
                return;
            }
            AcademicRecord record = byId.get(recordId);
            if (record == null) {
                ConsoleUtils.printError("That record id is not in the list.");
                return;
            }
            record.setGrade(readGradeValue());
            record.setObservations(ConsoleUtils.readOptionalText(
                    " New observations (empty = keep): ", record.getObservations()));
            if (!ConsoleUtils.confirm(" Save the changes? ")) {
                return;
            }
            if (recordDao.update(record)) {
                ConsoleUtils.printSuccess("Grade updated.");
            } else {
                ConsoleUtils.printError("The grade could not be updated.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while updating the grade.");
            LoggerUtil.logError("AcademicRecordController.updateGrade", exception);
        }
    }

    private void deleteGrade() {
        ConsoleUtils.printHeader("DELETE GRADE");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year (0 = cancel): ", AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            AcademicStudentSummary student = readActiveStudent(year);
            if (student == null) {
                return;
            }
            List<AcademicRecord> records = recordDao.findByStudentAndYear(student.id(), year);
            if (records.isEmpty()) {
                ConsoleUtils.printError("No grades found for that student in " + year + ".");
                return;
            }
            Map<Integer, AcademicRecord> byId = records.stream()
                    .collect(Collectors.toMap(AcademicRecord::getId, Function.identity()));
            records.forEach(record -> System.out.println("  " + describeRecord(record)));
            int recordId = ConsoleUtils.readInt(" Record id to delete (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (recordId == 0) {
                return;
            }
            if (!byId.containsKey(recordId)) {
                ConsoleUtils.printError("That record id is not in the list.");
                return;
            }
            if (ConsoleUtils.confirm(" Delete this grade permanently? ")) {
                if (recordDao.delete(recordId)) {
                    ConsoleUtils.printSuccess("Grade deleted.");
                } else {
                    ConsoleUtils.printError("The grade could not be deleted.");
                }
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while deleting the grade.");
            LoggerUtil.logError("AcademicRecordController.deleteGrade", exception);
        }
    }

    /** Shared update used when the register flow finds an existing grade. */
    private void updateExisting(AcademicStudentSummary student, Subject subject, int year, Period period)
            throws SQLException {
        List<AcademicRecord> records = recordDao.findByStudentYearPeriod(student.id(), year, period)
                .stream()
                .filter(record -> record.getSubjectId() == subject.getId())
                .toList();
        if (records.size() != 1) {
            ConsoleUtils.printError("Could not locate the existing grade.");
            return;
        }
        AcademicRecord record = records.get(0);
        System.out.println(" Current: " + describeRecord(record));
        record.setGrade(readGradeValue());
        record.setObservations(ConsoleUtils.readOptionalText(
                " New observations (empty = keep): ", record.getObservations()));
        if (recordDao.update(record)) {
            ConsoleUtils.printSuccess("Grade updated.");
        } else {
            ConsoleUtils.printError("The grade could not be updated.");
        }
    }

    // ------------------------------------------------------------------
    // Reports and averages (Stream API)
    // ------------------------------------------------------------------

    /** (a) Average per subject for a student in one year. */
    private void subjectAveragesForStudent() {
        ConsoleUtils.printHeader("STUDENT AVERAGES PER SUBJECT");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year (0 = cancel): ", AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            AcademicStudentSummary student = readActiveStudent(year);
            if (student == null) {
                return;
            }
            List<AcademicRecord> records = recordDao.findByStudentAndYear(student.id(), year);
            if (records.isEmpty()) {
                ConsoleUtils.printError("The student has no grades recorded for " + year + ".");
                return;
            }
            Map<Integer, Subject> subjects = subjectCatalog();
            System.out.println(" Student: " + student.fullName() + " - year " + year);
            System.out.printf(" %-24s %10s %10s%n", "SUBJECT", "AVERAGE", "STATUS");
            System.out.println(" " + "-".repeat(48));
            subjectAverages(records).entrySet().stream()
                    .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                    .forEach(entry -> {
                        Subject subject = entry.getKey();
                        double average = entry.getValue();
                        System.out.printf(" %-24s %10s %10s%n",
                                truncate(subject.getCode() + " - " + subject.getName(), 24),
                                formatGrade(average),
                                average >= PASSING_GRADE ? "APPROVED" : "FAILED");
                    });
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while computing the averages.");
            LoggerUtil.logError("AcademicRecordController.subjectAveragesForStudent", exception);
        }
    }

    /** (b) General average of the student for one period. */
    private void generalAverageForPeriod() {
        ConsoleUtils.printHeader("GENERAL AVERAGE FOR A PERIOD");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year (0 = cancel): ", AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            AcademicStudentSummary student = readActiveStudent(year);
            if (student == null) {
                return;
            }
            Period period = readPeriod(true);
            if (period == null) {
                return;
            }
            List<AcademicRecord> records = recordDao.findByStudentYearPeriod(student.id(), year, period);
            if (records.isEmpty()) {
                ConsoleUtils.printError("No grades for that student, year and period.");
                return;
            }
            DoubleSummaryStatistics statistics = records.stream()
                    .mapToDouble(AcademicRecord::getGrade)
                    .summaryStatistics();
            double average = statistics.getAverage();
            System.out.println(" Student: " + student.fullName()
                    + " - " + period.getDisplayName() + " " + year);
            records.stream()
                    .sorted(Comparator.comparing(AcademicRecord::getSubjectId))
                    .forEach(record -> System.out.println("  "
                            + describeRecord(record)));
            System.out.println(" Subjects graded: " + statistics.getCount());
            System.out.println(" Highest grade: " + formatGrade(statistics.getMax())
                    + " | Lowest grade: " + formatGrade(statistics.getMin()));
            System.out.println(" GENERAL AVERAGE: " + formatGrade(average)
                    + (average >= PASSING_GRADE ? " (APPROVED)" : " (FAILED)"));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while computing the average.");
            LoggerUtil.logError("AcademicRecordController.generalAverageForPeriod", exception);
        }
    }

    /** (c) Course average per subject, across all students of the course. */
    private void courseAveragePerSubject() {
        ConsoleUtils.printHeader("COURSE AVERAGE PER SUBJECT");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year (0 = cancel): ", AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            Subject subject = readSubject();
            if (subject == null) {
                return;
            }
            Period period = readPeriod(true);
            if (period == null) {
                return;
            }
            List<AcademicRecord> records = recordDao.findBySubjectYear(subject.getId(), year, period);
            if (records.isEmpty()) {
                ConsoleUtils.printError("No grades recorded for that subject, year and period.");
                return;
            }
            DoubleSummaryStatistics statistics = records.stream()
                    .mapToDouble(AcademicRecord::getGrade)
                    .summaryStatistics();
            System.out.println(" Subject: " + subject.getCode() + " - " + subject.getName()
                    + " | year " + year + (period == Period.FINAL ? " | Final grades"
                    : " | " + period.getDisplayName()));
            System.out.println(" Students graded: " + statistics.getCount());
            System.out.println(" Course average: " + formatGrade(statistics.getAverage()));
            System.out.println(" Highest: " + formatGrade(statistics.getMax())
                    + " | Lowest: " + formatGrade(statistics.getMin()));
            System.out.println(" Per-student detail:");
            records.stream()
                    .sorted(Comparator.comparing(AcademicRecord::getGrade).reversed())
                    .forEach(record -> System.out.printf("  student id %-6d grade %s%n",
                            record.getStudentId(), formatGrade(record.getGrade())));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while computing the course average.");
            LoggerUtil.logError("AcademicRecordController.courseAveragePerSubject", exception);
        }
    }

    /** (d) Full academic history of a student, all years, with averages. */
    private void fullAcademicHistory() {
        ConsoleUtils.printHeader("FULL ACADEMIC HISTORY");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year the student is currently active in (0 = cancel): ",
                    AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            AcademicStudentSummary student = readActiveStudent(year);
            if (student == null) {
                return;
            }
            List<AcademicRecord> records = recordDao.findByStudent(student.id());
            if (records.isEmpty()) {
                ConsoleUtils.printError("The student has no grades recorded yet.");
                return;
            }
            Map<Integer, Subject> subjects = subjectCatalog();
            System.out.println(" Student: " + student.fullName() + " (id " + student.id() + ")");

            // Group by year, then by subject: two-level stream grouping.
            Map<Integer, Map<Integer, List<AcademicRecord>>> byYearSubject = records.stream()
                    .collect(Collectors.groupingBy(AcademicRecord::getAcademicYear,
                            TreeMap::new,
                            Collectors.groupingBy(AcademicRecord::getSubjectId,
                                    Collectors.toList())));
            byYearSubject.forEach((recordYear, bySubject) -> {
                System.out.println();
                System.out.println(" === Year " + recordYear + " ===");
                bySubject.forEach((subjectId, subjectRecords) -> {
                    Subject subject = subjects.get(subjectId);
                    String subjectName = subject != null
                            ? subject.getCode() + " - " + subject.getName()
                            : "Subject " + subjectId;
                    System.out.println("  " + subjectName + ":");
                    subjectRecords.stream()
                            .sorted(Comparator.comparing(AcademicRecord::getPeriod))
                            .forEach(record -> System.out.println("    " + describeRecord(record)));
                    // Per-subject average for this year (period preference applied).
                    double average = averageForSubjectRecords(subjectRecords);
                    System.out.println("    Year subject average: " + formatGrade(average)
                            + (average >= PASSING_GRADE ? " (APPROVED)" : " (FAILED)"));
                });
                double yearAverage = averageForYear(bySubject.values().stream()
                        .flatMap(List::stream)
                        .toList());
                System.out.println("  >>> Year " + recordYear + " general average: "
                        + formatGrade(yearAverage)
                        + (yearAverage >= PASSING_GRADE ? " (APPROVED)" : " (FAILED)"));
            });
            double overallAverage = averageForYear(records);
            System.out.println();
            System.out.println(" OVERALL AVERAGE (all years): " + formatGrade(overallAverage)
                    + (overallAverage >= PASSING_GRADE ? " (APPROVED)" : " (FAILED)"));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while building the history.");
            LoggerUtil.logError("AcademicRecordController.fullAcademicHistory", exception);
        }
    }

    // ------------------------------------------------------------------
    // Stream-based calculation helpers
    // ------------------------------------------------------------------

    /**
     * Per-subject averages of a student's records, using Stream grouping.
     * If a 'Final' grade exists for the subject it is used as the subject's
     * final average; otherwise the average of the four periods is computed.
     */
    private Map<Subject, Double> subjectAverages(List<AcademicRecord> records) {
        Map<Integer, Subject> subjects = subjectCatalog();
        return records.stream()
                .collect(Collectors.groupingBy(AcademicRecord::getSubjectId))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> subjects.getOrDefault(entry.getKey(),
                                new Subject(entry.getKey(), "N/A", "Subject " + entry.getKey(),
                                        null, 0, 0, true)),
                        entry -> averageForSubjectRecords(entry.getValue())));
    }

    /**
     * Average of one student's records for a single subject.
     * Prefer the 'Final' record when present, otherwise average periods I-IV.
     */
    private double averageForSubjectRecords(List<AcademicRecord> records) {
        Optional<AcademicRecord> finalRecord = records.stream()
                .filter(record -> record.getPeriod() == Period.FINAL)
                .findFirst();
        return finalRecord
                .map(AcademicRecord::getGrade)
                .orElseGet(() -> records.stream()
                        .filter(record -> record.getPeriod() != Period.FINAL)
                        .mapToDouble(AcademicRecord::getGrade)
                        .average()
                        .orElse(0.0));
    }

    /** General average of a list of records, ignoring 'Final' duplicates. */
    private double averageForYear(List<AcademicRecord> records) {
        List<AcademicRecord> periodRecords = records.stream()
                .filter(record -> record.getPeriod() != Period.FINAL)
                .toList();
        List<AcademicRecord> effectiveRecords = periodRecords.isEmpty()
                ? new ArrayList<>(records)
                : periodRecords;
        return effectiveRecords.stream()
                .collect(Collectors.groupingBy(AcademicRecord::getSubjectId))
                .values().stream()
                .mapToDouble(this::averageForSubjectRecords)
                .average()
                .orElse(0.0);
    }

    // ------------------------------------------------------------------
    // Console helpers
    // ------------------------------------------------------------------

    /** Reads a grade, forcing one decimal: validated in [0.0, 5.0] and rounded. */
    private double readGradeValue() {
        double raw = ConsoleUtils.readDouble(
                " Grade (0.0 - 5.0): ", AppConstants.MIN_GRADE, AppConstants.MAX_GRADE);
        double rounded = Math.round(raw * 10.0) / 10.0;
        if (Math.abs(raw - rounded) > 1e-9) {
            System.out.println("  [i] Value adjusted to one decimal: " + formatGrade(rounded));
        }
        return rounded;
    }

    /** Formats a grade with exactly one decimal. */
    private String formatGrade(double grade) {
        return String.format("%.1f", grade);
    }

    /** Lists students with an active matricula for the year and picks one. */
    private AcademicStudentSummary readActiveStudent(int academicYear) throws SQLException {
        List<AcademicStudentSummary> students = enrollmentDao.listActiveStudents(academicYear);
        if (students.isEmpty()) {
            ConsoleUtils.printError("No students with an active enrollment (matricula) for " + academicYear + ".");
            return null;
        }
        students.forEach(student -> System.out.println("  " + student));
        int studentId = ConsoleUtils.readInt(" Student id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (studentId == 0) {
            return null;
        }
        AcademicStudentSummary student = students.stream()
                .filter(candidate -> candidate.id() == studentId)
                .findFirst()
                .orElse(null);
        if (student == null) {
            ConsoleUtils.printError("That id is not in the list of students active in " + academicYear + ".");
        }
        return student;
    }

    /** Lists active subjects and picks one; null cancels. */
    private Subject readSubject() throws SQLException {
        List<Subject> subjects = subjectDao.findActive();
        if (subjects.isEmpty()) {
            ConsoleUtils.printError("No active subjects registered.");
            return null;
        }
        subjects.forEach(subject -> System.out.println("  id " + subject.getId()
                + " - " + subject.getCode() + " - " + subject.getName()));
        int subjectId = ConsoleUtils.readInt(" Subject id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (subjectId == 0) {
            return null;
        }
        Subject subject = subjects.stream()
                .filter(candidate -> candidate.getId() == subjectId)
                .findFirst()
                .orElse(null);
        if (subject == null) {
            ConsoleUtils.printError("That id is not in the list of active subjects.");
        }
        return subject;
    }

    /** Lists active teachers and picks the one recording the grade. */
    private AcademicTeacherSummary readTeacher() throws SQLException {
        List<AcademicTeacherSummary> teachers = enrollmentDao.listActiveTeachers();
        if (teachers.isEmpty()) {
            ConsoleUtils.printError("No active teachers registered.");
            return null;
        }
        teachers.forEach(teacher -> System.out.println("  " + teacher
                + (teacher.specialization() != null ? " [" + teacher.specialization() + "]" : "")));
        int teacherId = ConsoleUtils.readInt(" Teacher id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (teacherId == 0) {
            return null;
        }
        AcademicTeacherSummary teacher = teachers.stream()
                .filter(candidate -> candidate.id() == teacherId)
                .findFirst()
                .orElse(null);
        if (teacher == null) {
            ConsoleUtils.printError("That id is not in the list of active teachers.");
        }
        return teacher;
    }

    /** Reads a period; {@code allowFinal} includes the 'Final' option. */
    private Period readPeriod(boolean allowFinal) {
        List<String> labels = java.util.Arrays.stream(Period.values())
                .filter(period -> allowFinal || period != Period.FINAL)
                .map(Period::getLabel)
                .toList();
        String label = ConsoleUtils.readOption(" Academic period", labels);
        return Period.fromLabel(label);
    }

    /** One-line description with subject name for clear console UX. */
    private String describeRecord(AcademicRecord record) {
        StringBuilder text = new StringBuilder();
        text.append("id ").append(record.getId())
                .append(" | ").append(record.getPeriod().getLabel())
                .append(" | grade ").append(formatGrade(record.getGrade()));
        try {
            Subject subject = subjectDao.findById(record.getSubjectId());
            text.append(" | ").append(subject != null
                    ? subject.getCode() + " - " + subject.getName()
                    : "subject " + record.getSubjectId());
        } catch (SQLException exception) {
            LoggerUtil.logError("AcademicRecordController.describeRecord", exception);
            text.append(" | subject ").append(record.getSubjectId());
        }
        if (record.getObservations() != null && !record.getObservations().isEmpty()) {
            text.append(" | obs: ").append(record.getObservations());
        }
        return text.toString();
    }

    /** Loads the subject catalog once into a map for the reports. */
    private Map<Integer, Subject> subjectCatalog() {
        try {
            return subjectDao.findAll().stream()
                    .collect(Collectors.toMap(Subject::getId, Function.identity()));
        } catch (SQLException exception) {
            LoggerUtil.logError("AcademicRecordController.subjectCatalog", exception);
            return Map.of();
        }
    }

    private String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }
}
