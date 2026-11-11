package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.dao.impl.SubjectDaoImpl;
import com.littlehorizons.erp.dao.impl.SubjectEnrollmentDaoImpl;
import com.littlehorizons.erp.model.EnrollmentPeriod;
import com.littlehorizons.erp.model.AcademicStudentSummary;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.SubjectEnrollmentStatus;
import com.littlehorizons.erp.util.AppConstants;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller of the subject enrollments ({@code inscripciones_materia}).
 * Business rules: the student must have an ACTIVE enrollment (matriculas) for
 * the year, the subject must belong to the student's grade, and each
 * student + subject + year + period combination is unique.
 */
public class SubjectEnrollmentController {

    private final SubjectEnrollmentDao enrollmentDao;
    private final SubjectDao subjectDao;

    /** Creates the controller with its default DAOs. */
    public SubjectEnrollmentController() {
        this.enrollmentDao = new SubjectEnrollmentDaoImpl();
        this.subjectDao = new SubjectDaoImpl();
    }

    /** Shows the enrollments submenu. Option 0 goes back. */
    public void show() {
        while (true) {
            ConsoleUtils.printHeader("SUBJECT ENROLLMENTS");
            System.out.println(" 1. Enroll student in a subject");
            System.out.println(" 2. Withdraw enrollment");
            System.out.println(" 3. Mark enrollment as completed");
            System.out.println(" 4. List enrollments of a student");
            System.out.println(" 5. List enrollments by year and period");
            int option = ConsoleUtils.readMenuOption(" Select an option: ", 5);
            switch (option) {
                case 1 -> enroll();
                case 2 -> changeStatus(SubjectEnrollmentStatus.WITHDRAWN);
                case 3 -> changeStatus(SubjectEnrollmentStatus.COMPLETED);
                case 4 -> listByStudent();
                case 5 -> listByYearAndPeriod();
                case 0 -> {
                    return;
                }
                default -> { /* unreachable */ }
            }
        }
    }

    private void enroll() {
        ConsoleUtils.printHeader("ENROLL STUDENT IN SUBJECT");
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
            Subject subject = readSubjectOfGrade(student.gradeId());
            if (subject == null) {
                return;
            }
            EnrollmentPeriod period = readPeriod();
            if (period == null) {
                return;
            }
            String section = ConsoleUtils.readOptionalText(" Section (e.g. A, empty = none): ", null);
            LocalDate enrollmentDate = ConsoleUtils.readOptionalDate(" Enrollment date", LocalDate.now());

            if (enrollmentDao.existsFor(student.id(), subject.getId(), year, period, null)) {
                ConsoleUtils.printError("That student is already enrolled in " + subject.getName()
                        + " for " + period + " of " + year + ".");
                return;
            }
            SubjectEnrollment enrollment = new SubjectEnrollment(
                    student.id(), subject.getId(), year, period, section,
                    enrollmentDate, SubjectEnrollmentStatus.ACTIVE);
            if (!ConsoleUtils.confirm(" Save this enrollment? ")) {
                return;
            }
            enrollmentDao.save(enrollment);
            ConsoleUtils.printSuccess("Student " + student.fullName()
                    + " enrolled in " + subject.getCode() + " - " + subject.getName()
                    + " (" + period + ", " + year + ").");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while enrolling the student.");
            LoggerUtil.logError("SubjectEnrollmentController.enroll", exception);
        }
    }

    /** Shared flow for withdrawing (Retirada) or completing (Completada). */
    private void changeStatus(SubjectEnrollmentStatus newStatus) {
        ConsoleUtils.printHeader(newStatus == SubjectEnrollmentStatus.WITHDRAWN
                ? "WITHDRAW ENROLLMENT" : "COMPLETE ENROLLMENT");
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
            List<SubjectEnrollment> activeEnrollments = enrollmentDao.findByStudentAndYear(student.id(), year)
                    .stream()
                    .filter(enrollment -> enrollment.getStatus() == SubjectEnrollmentStatus.ACTIVE)
                    .toList();
            if (activeEnrollments.isEmpty()) {
                ConsoleUtils.printError("The student has no active subject enrollments in " + year + ".");
                return;
            }
            Map<Integer, SubjectEnrollment> byId = activeEnrollments.stream()
                    .collect(Collectors.toMap(SubjectEnrollment::getId, Function.identity()));
            activeEnrollments.forEach(enrollment -> System.out.println("  "
                    + describeEnrollment(enrollment)));
            int enrollmentId = ConsoleUtils.readInt(
                    " Enrollment id to update (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
            if (enrollmentId == 0) {
                return;
            }
            SubjectEnrollment enrollment = byId.get(enrollmentId);
            if (enrollment == null) {
                ConsoleUtils.printError("That enrollment id does not belong to this student's list.");
                return;
            }
            if (!ConsoleUtils.confirm(" Set the enrollment to '" + newStatus.getLabel() + "'? ")) {
                return;
            }
            enrollment.setStatus(newStatus);
            if (enrollmentDao.update(enrollment)) {
                ConsoleUtils.printSuccess("Enrollment updated to '" + newStatus.getLabel() + "'.");
            } else {
                ConsoleUtils.printError("The enrollment could not be updated.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while updating the enrollment.");
            LoggerUtil.logError("SubjectEnrollmentController.changeStatus", exception);
        }
    }

    private void listByStudent() {
        ConsoleUtils.printHeader("ENROLLMENTS OF A STUDENT");
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
            List<SubjectEnrollment> enrollments = enrollmentDao.findByStudentAndYear(student.id(), year);
            if (enrollments.isEmpty()) {
                ConsoleUtils.printError("The student has no subject enrollments in " + year + ".");
                return;
            }
            System.out.println(" Student: " + student.fullName());
            enrollments.forEach(enrollment -> System.out.println("  " + describeEnrollment(enrollment)));
            // Small stream summary of the enrollment states.
            Map<SubjectEnrollmentStatus, Long> byStatus = enrollments.stream()
                    .collect(Collectors.groupingBy(SubjectEnrollment::getStatus, Collectors.counting()));
            byStatus.forEach((status, count) -> System.out.println("  " + status.getLabel() + ": " + count));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while listing the enrollments.");
            LoggerUtil.logError("SubjectEnrollmentController.listByStudent", exception);
        }
    }

    private void listByYearAndPeriod() {
        ConsoleUtils.printHeader("ENROLLMENTS BY YEAR AND PERIOD");
        try {
            int year = ConsoleUtils.readInt(
                    " Academic year (0 = cancel): ", AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (year == 0) {
                return;
            }
            EnrollmentPeriod period = readPeriod();
            if (period == null) {
                return;
            }
            List<SubjectEnrollment> enrollments = enrollmentDao.findByYearAndPeriod(year, period);
            if (enrollments.isEmpty()) {
                ConsoleUtils.printError("No enrollments for that year and period.");
                return;
            }
            enrollments.forEach(enrollment -> System.out.println("  " + describeEnrollment(enrollment)));
            System.out.println(" Total: " + enrollments.size() + " enrollment(s).");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while listing the enrollments.");
            LoggerUtil.logError("SubjectEnrollmentController.listByYearAndPeriod", exception);
        }
    }

    /** One-line description including subject code and name for clear UX. */
    private String describeEnrollment(SubjectEnrollment enrollment) {
        StringBuilder text = new StringBuilder();
        text.append("id ").append(enrollment.getId())
                .append(" | period ").append(enrollment.getPeriod().getLabel())
                .append(" | ").append(enrollment.getStatus().getLabel());
        try {
            Subject subject = subjectDao.findById(enrollment.getSubjectId());
            if (subject != null) {
                text.append(" | ").append(subject.getCode()).append(" - ").append(subject.getName());
            } else {
                text.append(" | subject ").append(enrollment.getSubjectId());
            }
        } catch (SQLException exception) {
            LoggerUtil.logError("SubjectEnrollmentController.describeEnrollment", exception);
            text.append(" | subject ").append(enrollment.getSubjectId());
        }
        if (enrollment.getSection() != null && !enrollment.getSection().isEmpty()) {
            text.append(" | section ").append(enrollment.getSection());
        }
        return text.toString();
    }

    /** Lists the students with an active matricula and picks one; null cancels. */
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

    /** Lists the active subjects of one grade and picks one; null cancels. */
    private Subject readSubjectOfGrade(int gradeId) throws SQLException {
        List<Subject> subjects = subjectDao.findByGrade(gradeId)
                .stream()
                .filter(Subject::isActive)
                .toList();
        if (subjects.isEmpty()) {
            ConsoleUtils.printError("The grade has no active subjects yet. Create them in Subjects first.");
            return null;
        }
        subjects.forEach(subject -> System.out.println("  id " + subject.getId()
                + " - " + subject.getCode() + " - " + subject.getName()
                + " (" + subject.getWeeklyHours() + " h/week)"));
        int subjectId = ConsoleUtils.readInt(" Subject id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (subjectId == 0) {
            return null;
        }
        Subject subject = subjects.stream()
                .filter(candidate -> candidate.getId() == subjectId)
                .findFirst()
                .orElse(null);
        if (subject == null) {
            ConsoleUtils.printError("That id is not in the list of subjects for this grade.");
        }
        return subject;
    }

    /** Reads one enrollment period (I..IV); null cancels. */
    private EnrollmentPeriod readPeriod() {
        List<String> labels = java.util.Arrays.stream(EnrollmentPeriod.values())
                .map(EnrollmentPeriod::getLabel)
                .toList();
        String label = ConsoleUtils.readOption(" Academic period", labels);
        return EnrollmentPeriod.fromLabel(label);
    }
}
