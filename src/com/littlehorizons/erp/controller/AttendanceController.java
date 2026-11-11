package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.dao.impl.AttendanceDaoImpl;
import com.littlehorizons.erp.dao.impl.SubjectDaoImpl;
import com.littlehorizons.erp.dao.impl.SubjectEnrollmentDaoImpl;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.AttendanceStatus;
import com.littlehorizons.erp.model.AcademicStudentSummary;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.AcademicTeacherSummary;
import com.littlehorizons.erp.util.AppConstants;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller of the attendance module ({@code asistencias}).
 *
 * <p>Reports (computed with the Stream API): students with more than three
 * 'Ausente' records in the same ISO week, and the attendance percentage per
 * student (Presente + Tardanza count as attended).</p>
 */
public class AttendanceController {

    /** Threshold used by the worrying-pattern report. */
    private static final int ABSENCES_PER_WEEK_LIMIT = 3;

    private final AttendanceDao attendanceDao;
    private final SubjectDao subjectDao;
    private final SubjectEnrollmentDao enrollmentDao;

    /** Creates the controller with its default DAOs. */
    public AttendanceController() {
        this.attendanceDao = new AttendanceDaoImpl();
        this.subjectDao = new SubjectDaoImpl();
        this.enrollmentDao = new SubjectEnrollmentDaoImpl();
    }

    /** Shows the attendance submenu. Option 0 goes back. */
    public void show() {
        while (true) {
            ConsoleUtils.printHeader("ATTENDANCE");
            System.out.println(" 1. Register attendance");
            System.out.println(" 2. Update attendance");
            System.out.println(" 3. Delete attendance");
            System.out.println(" 4. Attendance of a subject on a date");
            System.out.println(" 5. Report: worrying absence patterns (> 3 absences per week)");
            System.out.println(" 6. Report: attendance percentage per student");
            int option = ConsoleUtils.readMenuOption(" Select an option: ", 6);
            switch (option) {
                case 1 -> registerAttendance();
                case 2 -> updateAttendance();
                case 3 -> deleteAttendance();
                case 4 -> listSubjectDate();
                case 5 -> reportAbsencePatterns();
                case 6 -> reportAttendancePercentage();
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

    private void registerAttendance() {
        ConsoleUtils.printHeader("REGISTER ATTENDANCE");
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
            LocalDate date = ConsoleUtils.readOptionalDate(" Attendance date", LocalDate.now());
            AcademicStudentSummary student = readActiveStudent(year);
            if (student == null) {
                return;
            }
            AcademicTeacherSummary teacher = readTeacher();
            if (teacher == null) {
                return;
            }
            AttendanceStatus status = readStatus();
            String observations = ConsoleUtils.readOptionalText(" Observations (empty = none): ", null);

            if (attendanceDao.existsFor(student.id(), subject.getId(), date, null)) {
                ConsoleUtils.printError("There is already an attendance record for " + student.fullName()
                        + " in " + subject.getCode() + " on " + date + ".");
                return;
            }
            Attendance attendance = new Attendance(
                    student.id(), subject.getId(), date, status, observations, teacher.id());
            if (!ConsoleUtils.confirm(" Save " + status.getLabel() + " for " + student.fullName()
                    + " (" + subject.getCode() + ", " + date + ")? ")) {
                return;
            }
            attendanceDao.save(attendance);
            ConsoleUtils.printSuccess("Attendance registered.");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while registering the attendance.");
            LoggerUtil.logError("AttendanceController.registerAttendance", exception);
        }
    }

    private void updateAttendance() {
        ConsoleUtils.printHeader("UPDATE ATTENDANCE");
        try {
            Subject subject = readSubject();
            if (subject == null) {
                return;
            }
            LocalDate date = ConsoleUtils.readOptionalDate(" Attendance date", LocalDate.now());
            List<Attendance> attendances = attendanceDao.findBySubjectAndDate(subject.getId(), date);
            if (attendances.isEmpty()) {
                ConsoleUtils.printError("No attendance records for that subject and date.");
                return;
            }
            Map<Integer, Attendance> byId = attendances.stream()
                    .collect(Collectors.toMap(Attendance::getId, Function.identity()));
            Map<Integer, AcademicStudentSummary> students = studentCatalog(date.getYear());
            attendances.forEach(attendance -> System.out.println("  " + describeAttendance(attendance, students)));
            int attendanceId = ConsoleUtils.readInt(" Attendance id to update (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (attendanceId == 0) {
                return;
            }
            Attendance attendance = byId.get(attendanceId);
            if (attendance == null) {
                ConsoleUtils.printError("That attendance id is not in the list.");
                return;
            }
            System.out.println(" Current status: " + attendance.getStatus().getLabel());
            attendance.setStatus(readStatus());
            attendance.setObservations(ConsoleUtils.readOptionalText(
                    " New observations (empty = keep): ", attendance.getObservations()));
            if (!ConsoleUtils.confirm(" Save the changes? ")) {
                return;
            }
            if (attendanceDao.update(attendance)) {
                ConsoleUtils.printSuccess("Attendance updated.");
            } else {
                ConsoleUtils.printError("The attendance could not be updated.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while updating the attendance.");
            LoggerUtil.logError("AttendanceController.updateAttendance", exception);
        }
    }

    private void deleteAttendance() {
        ConsoleUtils.printHeader("DELETE ATTENDANCE");
        try {
            Subject subject = readSubject();
            if (subject == null) {
                return;
            }
            LocalDate date = ConsoleUtils.readOptionalDate(" Attendance date", LocalDate.now());
            List<Attendance> attendances = attendanceDao.findBySubjectAndDate(subject.getId(), date);
            if (attendances.isEmpty()) {
                ConsoleUtils.printError("No attendance records for that subject and date.");
                return;
            }
            Map<Integer, Attendance> byId = attendances.stream()
                    .collect(Collectors.toMap(Attendance::getId, Function.identity()));
            Map<Integer, AcademicStudentSummary> students = studentCatalog(date.getYear());
            attendances.forEach(attendance -> System.out.println("  " + describeAttendance(attendance, students)));
            int attendanceId = ConsoleUtils.readInt(" Attendance id to delete (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (attendanceId == 0) {
                return;
            }
            if (!byId.containsKey(attendanceId)) {
                ConsoleUtils.printError("That attendance id is not in the list.");
                return;
            }
            if (ConsoleUtils.confirm(" Delete this attendance record permanently? ")) {
                if (attendanceDao.delete(attendanceId)) {
                    ConsoleUtils.printSuccess("Attendance deleted.");
                } else {
                    ConsoleUtils.printError("The attendance could not be deleted.");
                }
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while deleting the attendance.");
            LoggerUtil.logError("AttendanceController.deleteAttendance", exception);
        }
    }

    private void listSubjectDate() {
        ConsoleUtils.printHeader("ATTENDANCE OF A SUBJECT ON A DATE");
        try {
            Subject subject = readSubject();
            if (subject == null) {
                return;
            }
            LocalDate date = ConsoleUtils.readOptionalDate(" Attendance date", LocalDate.now());
            List<Attendance> attendances = attendanceDao.findBySubjectAndDate(subject.getId(), date);
            if (attendances.isEmpty()) {
                ConsoleUtils.printError("No attendance records for that subject and date.");
                return;
            }
            System.out.println(" Subject: " + subject.getCode() + " - " + subject.getName()
                    + " | " + date);
            Map<Integer, AcademicStudentSummary> students = studentCatalog(date.getYear());
            attendances.stream()
                    .sorted(java.util.Comparator.comparing(Attendance::getStudentId))
                    .forEach(attendance -> System.out.println("  " + describeAttendance(attendance, students)));
            // Small stream summary of the day's statuses.
            Map<AttendanceStatus, Long> byStatus = attendances.stream()
                    .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
            byStatus.forEach((status, count) -> System.out
                    .println("  " + status.getLabel() + ": " + count));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while listing the attendance.");
            LoggerUtil.logError("AttendanceController.listSubjectDate", exception);
        }
    }

    // ------------------------------------------------------------------
    // Reports (Stream API)
    // ------------------------------------------------------------------

    /**
     * Worrying patterns: students with MORE THAN 3 'Ausente' records in the
     * same ISO week of the chosen year.
     */
    private void reportAbsencePatterns() {
        ConsoleUtils.printHeader("REPORT: WORRYING ABSENCE PATTERNS");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year (0 = cancel): ", AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            List<Attendance> attendances = attendanceDao.findByYear(year)
                    .stream()
                    .filter(attendance -> attendance.getStatus() == AttendanceStatus.ABSENT)
                    .toList();
            if (attendances.isEmpty()) {
                ConsoleUtils.printError("No absences recorded for " + year + ". Nothing to report.");
                return;
            }
            Map<Integer, AcademicStudentSummary> students = studentCatalog(year);
            WeekFields weekFields = WeekFields.ISO;

            // Group by student, then by ISO year-week, and keep the alarming weeks.
            Map<Integer, Map<String, Long>> absencesByStudentWeek = attendances.stream()
                    .collect(Collectors.groupingBy(Attendance::getStudentId,
                            Collectors.groupingBy(attendance -> {
                                LocalDate date = attendance.getAttendanceDate();
                                return date.getYear() + "-W"
                                        + String.format("%02d", date.get(weekFields.weekOfWeekBasedYear()));
                            }, Collectors.counting())));

            Map<Integer, Map<String, Long>> worrying = absencesByStudentWeek.entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), entry.getValue().entrySet().stream()
                            .filter(weekEntry -> weekEntry.getValue() > ABSENCES_PER_WEEK_LIMIT)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                    (a, b) -> a, TreeMap::new))))
                    .filter(entry -> !entry.getValue().isEmpty())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (worrying.isEmpty()) {
                System.out.println(" No student exceeded " + ABSENCES_PER_WEEK_LIMIT
                        + " absences in one week. Good news!");
                return;
            }
            System.out.println(" Students with more than " + ABSENCES_PER_WEEK_LIMIT
                    + " absences in the same week (" + year + "):");
            worrying.forEach((studentId, weeks) -> {
                AcademicStudentSummary student = students.get(studentId);
                System.out.println("  " + (student != null ? student.toString() : "Student id " + studentId));
                weeks.forEach((week, count) -> System.out
                        .println("      Week " + week + ": " + count + " absences"));
            });
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while building the absence report.");
            LoggerUtil.logError("AttendanceController.reportAbsencePatterns", exception);
        }
    }

    /** Attendance percentage per student: (Presente + Tardanza) / total. */
    private void reportAttendancePercentage() {
        ConsoleUtils.printHeader("REPORT: ATTENDANCE PERCENTAGE PER STUDENT");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year (0 = cancel): ", AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            List<Attendance> attendances = attendanceDao.findByYear(year);
            if (attendances.isEmpty()) {
                ConsoleUtils.printError("No attendance records for " + year + ".");
                return;
            }
            Map<Integer, AcademicStudentSummary> students = studentCatalog(year);

            Map<Integer, List<Attendance>> byStudent = attendances.stream()
                    .collect(Collectors.groupingBy(Attendance::getStudentId));

            record StudentAttendance(String name, long attended, long total) {
                double percentage() {
                    return total == 0 ? 0.0 : attended * 100.0 / total;
                }
            }

            List<StudentAttendance> ranking = byStudent.entrySet().stream()
                    .map(entry -> {
                        List<Attendance> studentRecords = entry.getValue();
                        long attended = studentRecords.stream()
                                .filter(attendance -> attendance.getStatus() == AttendanceStatus.PRESENT
                                        || attendance.getStatus() == AttendanceStatus.LATE)
                                .count();
                        AcademicStudentSummary student = students.get(entry.getKey());
                        return new StudentAttendance(
                                student != null ? student.fullName() : "Student id " + entry.getKey(),
                                attended,
                                studentRecords.size());
                    })
                    .sorted(java.util.Comparator.comparingDouble(StudentAttendance::percentage).reversed())
                    .toList();

            System.out.println(" Note: 'Presente' and 'Tardanza' count as attended. Year " + year + ".");
            System.out.printf(" %-34s %10s %8s %8s%n", "STUDENT", "ATTENDED", "TOTAL", "%");
            System.out.println(" " + "-".repeat(64));
            ranking.forEach(entry -> System.out.printf(" %-34s %10d %8d %7.1f%%%n",
                    truncate(entry.name(), 34), entry.attended(), entry.total(), entry.percentage()));
            double overallPercentage = ranking.stream()
                    .mapToLong(StudentAttendance::attended)
                    .sum() * 100.0
                    / ranking.stream().mapToLong(StudentAttendance::total).sum();
            System.out.println(" Overall attendance percentage: " + String.format("%.1f", overallPercentage) + "%");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while building the percentage report.");
            LoggerUtil.logError("AttendanceController.reportAttendancePercentage", exception);
        }
    }

    // ------------------------------------------------------------------
    // Console helpers
    // ------------------------------------------------------------------

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

    /** Lists active teachers; null cancels (the teacher records the attendance). */
    private AcademicTeacherSummary readTeacher() throws SQLException {
        List<AcademicTeacherSummary> teachers = enrollmentDao.listActiveTeachers();
        if (teachers.isEmpty()) {
            ConsoleUtils.printError("No active teachers registered.");
            return null;
        }
        teachers.forEach(teacher -> System.out.println("  " + teacher
                + (teacher.specialization() != null ? " [" + teacher.specialization() + "]" : "")));
        int teacherId = ConsoleUtils.readInt(" Teacher id who registers (0 = cancel): ",
                1, Integer.MAX_VALUE, 0);
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

    /** Reads one attendance status using the Spanish DB labels. */
    private AttendanceStatus readStatus() {
        String label = ConsoleUtils.readOption(" Attendance status", AttendanceStatus.labels());
        return AttendanceStatus.fromLabel(label);
    }

    /** One-line description with student name for clear console UX. */
    private String describeAttendance(Attendance attendance, Map<Integer, AcademicStudentSummary> students) {
        StringBuilder text = new StringBuilder();
        text.append("id ").append(attendance.getId()).append(" | ");
        AcademicStudentSummary student = students.get(attendance.getStudentId());
        if (student != null) {
            text.append(student.fullName()).append(" (id ").append(attendance.getStudentId()).append(")");
        } else {
            text.append("student id ").append(attendance.getStudentId());
        }
        text.append(" | ").append(attendance.getAttendanceDate())
                .append(" | ").append(attendance.getStatus().getLabel());
        if (attendance.getObservations() != null && !attendance.getObservations().isEmpty()) {
            text.append(" | obs: ").append(attendance.getObservations());
        }
        return text.toString();
    }

    /** Loads the students active in the year into a map for the reports. */
    private Map<Integer, AcademicStudentSummary> studentCatalog(int academicYear) {
        try {
            return enrollmentDao.listActiveStudents(academicYear).stream()
                    .collect(Collectors.toMap(AcademicStudentSummary::id, Function.identity()));
        } catch (SQLException exception) {
            LoggerUtil.logError("AttendanceController.studentCatalog", exception);
            return Map.of();
        }
    }

    private String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }
}
