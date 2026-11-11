package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.GradeDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.StudentGuardianDao;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.EnrollmentStatus;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.util.AppConstants;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Controller for the enrollment (matriculas) flow. Business rules enforced
 * here: student exists and is active, has at least one guardian, the grade
 * exists, and only one enrollment per student per academic year (UNIQUE).
 */
public class EnrollmentController {

    private final EnrollmentDao enrollmentDao;
    private final StudentDao studentDao;
    private final GradeDao gradeDao;
    private final StudentGuardianDao studentGuardianDao;

    public EnrollmentController(EnrollmentDao enrollmentDao, StudentDao studentDao,
                                GradeDao gradeDao, StudentGuardianDao studentGuardianDao) {
        this.enrollmentDao = enrollmentDao;
        this.studentDao = studentDao;
        this.gradeDao = gradeDao;
        this.studentGuardianDao = studentGuardianDao;
    }

    /** Enrolls a student in a grade for an academic year (status 'Activa'). */
    public void enrollStudent() {
        ConsoleUtils.printHeader("NEW ENROLLMENT");
        try {
            Student student = pickStudentById();
            if (student == null) {
                return;
            }
            if (student.getStatus() != null && !"Activo".equals(student.getStatus().toString())) {
                ConsoleUtils.printError("Only active students can be enrolled. Current status: "
                        + student.getStatus());
                return;
            }
            int guardianCount = studentGuardianDao.countGuardiansForStudent(student.getId());
            if (guardianCount == 0) {
                ConsoleUtils.printError("The student must have at least one guardian before enrolling. "
                        + "Link a guardian first (menu 3).");
                return;
            }
            Grade grade = pickGrade();
            if (grade == null) {
                return;
            }
            int academicYear = ConsoleUtils.readInt("Academic year (0 = cancel): ",
                    AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (academicYear == 0) {
                System.out.println("  Operation cancelled.");
                return;
            }
            if (enrollmentDao.existsEnrollment(student.getId(), academicYear)) {
                ConsoleUtils.printError("Student '" + student.getFullName()
                        + "' already has an enrollment for the year " + academicYear
                        + ". Only one enrollment per student per year is allowed.");
                return;
            }
            Enrollment enrollment = new Enrollment();
            enrollment.setStudentId(student.getId());
            enrollment.setGradeId(grade.getId());
            enrollment.setAcademicYear(academicYear);
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setEnrollmentDate(ConsoleUtils.readOptionalDate("Enrollment date", LocalDate.now()));
            enrollment.setObservations(ConsoleUtils.readOptionalText("Observations (empty = none): ", null));
            int id = enrollmentDao.save(enrollment);
            ConsoleUtils.printSuccess("Enrollment created with id " + id + " for '"
                    + student.getFullName() + "' in grade '" + grade.getName()
                    + "' for year " + academicYear + ".");
            LoggerUtil.logInfo("Enrollment created: id=" + id + ", student=" + student.getId()
                    + ", grade=" + grade.getId() + ", year=" + academicYear);
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not create the enrollment.");
            LoggerUtil.logError("EnrollmentController.enrollStudent", exception);
        }
    }

    /** Lists enrollments with filters and a per-year report using Stream API. */
    public void listEnrollments() {
        ConsoleUtils.printHeader("ENROLLMENT LIST");
        try {
            System.out.println("  1. All enrollments");
            System.out.println("  2. By student");
            System.out.println("  3. By academic year");
            int option = ConsoleUtils.readMenuOption("Filter: ", 3);
            List<Enrollment> enrollments;
            switch (option) {
                case 1 -> enrollments = enrollmentDao.findAll();
                case 2 -> {
                    Student student = pickStudentById();
                    if (student == null) {
                        return;
                    }
                    enrollments = enrollmentDao.findByStudentId(student.getId());
                }
                case 3 -> {
                    int year = ConsoleUtils.readInt("Academic year (0 = cancel): ",
                            AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
                    if (year == 0) {
                        System.out.println("  Operation cancelled.");
                        return;
                    }
                    enrollments = enrollmentDao.findByAcademicYear(year);
                }
                default -> {
                    return;
                }
            }
            if (enrollments.isEmpty()) {
                ConsoleUtils.printError("No enrollments found.");
                return;
            }
            // Report: enrollments grouped by academic year, sorted, with counts.
            Map<Integer, List<Enrollment>> byYear = enrollments.stream()
                    .collect(Collectors.groupingBy(Enrollment::getAcademicYear, TreeMap::new,
                            Collectors.toList()));
            byYear.forEach((year, yearEnrollments) -> {
                System.out.println("  --- Academic year " + year + " (" + yearEnrollments.size()
                        + " enrollment(s)) ---");
                yearEnrollments.forEach(enrollment -> System.out.println("    " + enrollment));
            });
            long activeCount = enrollments.stream()
                    .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                    .count();
            System.out.println("  Total: " + enrollments.size() + " enrollment(s), "
                    + activeCount + " active.");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not retrieve the enrollments.");
            LoggerUtil.logError("EnrollmentController.listEnrollments", exception);
        }
    }

    /** Withdraws an active enrollment: status 'Retirada' + withdrawal date. */
    public void withdrawEnrollment() {
        ConsoleUtils.printHeader("WITHDRAW ENROLLMENT");
        try {
            Student student = pickStudentById();
            if (student == null) {
                return;
            }
            List<Enrollment> enrollments = enrollmentDao.findByStudentId(student.getId());
            List<Enrollment> activeEnrollments = enrollments.stream()
                    .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                    .toList();
            if (activeEnrollments.isEmpty()) {
                ConsoleUtils.printError("Student '" + student.getFullName()
                        + "' has no active enrollments.");
                return;
            }
            activeEnrollments.forEach(enrollment -> System.out.println("  " + enrollment));
            int id = ConsoleUtils.readInt("Enrollment id to withdraw (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (id == 0) {
                System.out.println("  Operation cancelled.");
                return;
            }
            Enrollment enrollment = activeEnrollments.stream()
                    .filter(item -> item.getId() == id)
                    .findFirst()
                    .orElse(null);
            if (enrollment == null) {
                ConsoleUtils.printError("That enrollment id does not belong to this student "
                        + "or is not active.");
                return;
            }
            if (ConsoleUtils.confirm("Withdraw enrollment " + id + " (status 'Retirada')?")) {
                enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
                enrollment.setWithdrawalDate(ConsoleUtils.readOptionalDate("Withdrawal date",
                        LocalDate.now()));
                enrollmentDao.update(enrollment);
                ConsoleUtils.printSuccess("Enrollment withdrawn on "
                        + enrollment.getWithdrawalDate() + ".");
            } else {
                System.out.println("  Operation cancelled.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not withdraw the enrollment.");
            LoggerUtil.logError("EnrollmentController.withdrawEnrollment", exception);
        }
    }

    /** Shows active students and asks for an id; returns the full student or null. */
    private Student pickStudentById() throws SQLException {
        List<StudentSummary> summaries = studentDao.findActiveSummaries();
        if (summaries.isEmpty()) {
            ConsoleUtils.printError("There are no active students.");
            return null;
        }
        System.out.println("  Active students:");
        summaries.forEach(summary -> System.out.println("    " + summary));
        int id = ConsoleUtils.readInt("Student id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (id == 0) {
            System.out.println("  Operation cancelled.");
            return null;
        }
        Student student = studentDao.findById(id);
        if (student == null) {
            ConsoleUtils.printError("Student with id " + id + " not found.");
        }
        return student;
    }

    /** Shows the grades and asks for an id; validates that the grade exists. */
    private Grade pickGrade() throws SQLException {
        List<Grade> grades = gradeDao.findAll();
        if (grades.isEmpty()) {
            ConsoleUtils.printError("There are no grades configured in the database.");
            return null;
        }
        System.out.println("  Available grades:");
        grades.forEach(grade -> System.out.println("    " + grade));
        int id = ConsoleUtils.readInt("Grade id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (id == 0) {
            System.out.println("  Operation cancelled.");
            return null;
        }
        Grade grade = gradeDao.findById(id);
        if (grade == null) {
            ConsoleUtils.printError("Grade with id " + id + " does not exist.");
        }
        return grade;
    }
}
