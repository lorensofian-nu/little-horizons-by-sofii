package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.TeacherAssignmentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.dao.impl.TeacherAssignmentDaoImpl;
import com.littlehorizons.erp.dao.impl.TeacherDaoImpl;
import com.littlehorizons.erp.model.SubjectSummary;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.model.TeacherStatus;
import com.littlehorizons.erp.util.AppConstants;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Business logic of the teacher-subject assignments
 * ({@code asignaciones_profesor}): assign, list by teacher, list by
 * subject/year and delete, with uniqueness, existence and active-state
 * validations.
 *
 * <p>Known limitation (per project instruction): the DDL has no schedule
 * columns, so the day/time of each class cannot be stored. To help the
 * operator avoid basic conflicts, the teacher's existing assignments are
 * displayed in the console before confirming a new assignment; the operator
 * must verify visually that the time slots do not overlap.</p>
 */
public class TeacherAssignmentController {

    private static final List<String> SECTIONS = List.of("A", "B", "C");

    private final TeacherAssignmentDao assignmentDao;
    private final TeacherDao teacherDao;

    /** Builds the controller with its default DAO implementations. */
    public TeacherAssignmentController() {
        this.assignmentDao = new TeacherAssignmentDaoImpl();
        this.teacherDao = new TeacherDaoImpl();
    }

    /** Creates a new assignment guided by the console. */
    public void createAssignment() {
        try {
            ConsoleUtils.printHeader("NEW SUBJECT ASSIGNMENT");

            Optional<Teacher> teacher = selectActiveTeacher();
            if (teacher.isEmpty()) {
                return;
            }

            SubjectSummary subject = selectActiveSubject();
            if (subject == null) {
                return;
            }

            int currentYear = YearMonth.now().getYear();
            int academicYear = ConsoleUtils.readInt(
                    "  Academic year (" + AppConstants.MIN_YEAR + "-"
                            + AppConstants.MAX_YEAR + ", 0 = cancel): ",
                    AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (academicYear == 0) {
                return;
            }

            String section = ConsoleUtils.readOption("  Section:", SECTIONS);

            // Show current load so the operator can visually avoid time overlaps.
            // (No schedule columns exist in the DDL; see the class javadoc.)
            showTeacherAssignments(teacher.get());

            if (assignmentDao.existsAssignment(teacher.get().getId(), subject.getId(),
                    academicYear, section)) {
                ConsoleUtils.printError("That teacher is already assigned to subject '"
                        + subject.getName() + "' in year " + academicYear
                        + ", section " + section + ".");
                return;
            }

            if (!ConsoleUtils.confirm("  Confirm assignment of '"
                    + teacher.get().getFullName() + "' to '" + subject.getName()
                    + "' (year " + academicYear + ", section " + section + ")?")) {
                ConsoleUtils.printSuccess("Assignment cancelled.");
                return;
            }

            TeacherAssignment assignment = TeacherAssignment.newAssignment(
                    teacher.get().getId(), subject.getId(), academicYear, section);
            int id = assignmentDao.save(assignment);
            ConsoleUtils.printSuccess("Assignment registered with id " + id + ".");
        } catch (SQLException exception) {
            ConsoleUtils.printError("A database error occurred. Check the log file for details.");
            LoggerUtil.logError("TeacherAssignmentController.createAssignment", exception);
        }
    }

    /** Lists every assignment of one teacher. */
    public void listAssignmentsByTeacher() {
        try {
            ConsoleUtils.printHeader("ASSIGNMENTS BY TEACHER");
            int teacherId = ConsoleUtils.readInt("  Teacher id (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (teacherId == 0) {
                return;
            }
            Optional<Teacher> teacher = teacherDao.findById(teacherId);
            if (teacher.isEmpty()) {
                ConsoleUtils.printError("No teacher found with id " + teacherId + ".");
                return;
            }
            List<TeacherAssignment> assignments = assignmentDao.assignmentsByTeacher(teacherId);
            if (assignments.isEmpty()) {
                ConsoleUtils.printError("Teacher '" + teacher.get().getFullName()
                        + "' has no assignments yet.");
                return;
            }
            System.out.println("  Teacher: " + teacher.get().getFullName()
                    + " (" + teacher.get().getIdentityLabel() + ")");
            System.out.println("  Total assignments: " + assignments.size());
            System.out.println();
            assignments.stream()
                    .map(assignment -> String.format("#%-4d %s",
                            assignment.getId(), assignment.toDisplayString()))
                    .forEach(line -> System.out.println("  " + line));
        } catch (SQLException exception) {
            ConsoleUtils.printError("A database error occurred. Check the log file for details.");
            LoggerUtil.logError("TeacherAssignmentController.listAssignmentsByTeacher", exception);
        }
    }

    /** Lists every assignment of one subject in one academic year. */
    public void listAssignmentsBySubjectAndYear() {
        try {
            ConsoleUtils.printHeader("ASSIGNMENTS BY SUBJECT AND YEAR");
            int subjectId = ConsoleUtils.readInt("  Subject id (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (subjectId == 0) {
                return;
            }
            int academicYear = ConsoleUtils.readInt(
                    "  Academic year (" + AppConstants.MIN_YEAR + "-"
                            + AppConstants.MAX_YEAR + ", 0 = cancel): ",
                    AppConstants.MIN_YEAR, AppConstants.MAX_YEAR, 0);
            if (academicYear == 0) {
                return;
            }
            List<TeacherAssignment> assignments =
                    assignmentDao.assignmentsBySubjectAndYear(subjectId, academicYear);
            if (assignments.isEmpty()) {
                ConsoleUtils.printError("No assignments found for subject " + subjectId
                        + " in year " + academicYear + ".");
                return;
            }
            // Every row carries the subject name, so the first row titles the report.
            System.out.println("  Subject: " + assignments.get(0).getSubjectName()
                    + " (" + assignments.get(0).getSubjectCode() + ") | Year " + academicYear);
            System.out.println("  Total assignments: " + assignments.size());
            System.out.println();
            assignments.stream()
                    .map(assignment -> String.format("#%-4d %-35s Section %s",
                            assignment.getId(),
                            assignment.getTeacherFullName() == null
                                    ? ("Teacher #" + assignment.getTeacherId())
                                    : assignment.getTeacherFullName(),
                            assignment.getSection() == null ? "-" : assignment.getSection()))
                    .forEach(line -> System.out.println("  " + line));
        } catch (SQLException exception) {
            ConsoleUtils.printError("A database error occurred. Check the log file for details.");
            LoggerUtil.logError(
                    "TeacherAssignmentController.listAssignmentsBySubjectAndYear", exception);
        }
    }

    /** Deletes one assignment after confirmation. */
    public void deleteAssignment() {
        try {
            ConsoleUtils.printHeader("DELETE ASSIGNMENT");
            int id = ConsoleUtils.readInt("  Assignment id to delete (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (id == 0) {
                return;
            }
            Optional<TeacherAssignment> assignment = assignmentDao.findById(id);
            if (assignment.isEmpty()) {
                ConsoleUtils.printError("No assignment found with id " + id + ".");
                return;
            }
            System.out.println("  Assignment to delete: #" + id + " - "
                    + assignment.get().toDisplayString()
                    + " | Teacher: " + assignment.get().getTeacherFullName());
            if (!ConsoleUtils.confirm("  Confirm deletion?")) {
                ConsoleUtils.printSuccess("Operation cancelled, nothing was deleted.");
                return;
            }
            if (assignmentDao.delete(id)) {
                ConsoleUtils.printSuccess("Assignment deleted.");
            } else {
                ConsoleUtils.printError("The assignment could not be deleted.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("A database error occurred. Check the log file for details.");
            LoggerUtil.logError("TeacherAssignmentController.deleteAssignment", exception);
        }
    }

    // ------------------------------------------------------------------
    // Selection helpers
    // ------------------------------------------------------------------

    private Optional<Teacher> selectActiveTeacher() throws SQLException {
        List<Teacher> activeTeachers = teacherDao.listActiveTeachers();
        if (activeTeachers.isEmpty()) {
            ConsoleUtils.printError("There are no active teachers. Register one first.");
            return Optional.empty();
        }
        System.out.println("  Active teachers:");
        activeTeachers.forEach(teacher ->
                System.out.println("    #" + teacher.getId() + " - " + teacher.getFullName()));
        System.out.println();
        int teacherId = ConsoleUtils.readInt("  Teacher id (0 = cancel): ",
                1, Integer.MAX_VALUE, 0);
        if (teacherId == 0) {
            return Optional.empty();
        }
        Optional<Teacher> teacher = teacherDao.findById(teacherId);
        if (teacher.isEmpty()) {
            ConsoleUtils.printError("No teacher found with id " + teacherId + ".");
            return Optional.empty();
        }
        if (teacher.get().getStatus() != TeacherStatus.ACTIVO) {
            ConsoleUtils.printError("Teacher '" + teacher.get().getFullName()
                    + "' is not active (status: " + teacher.get().getStatus().getValue()
                    + "). Only active teachers can be assigned.");
            return Optional.empty();
        }
        return teacher;
    }

    private SubjectSummary selectActiveSubject() throws SQLException {
        List<SubjectSummary> subjects = assignmentDao.listSubjects();
        if (subjects.isEmpty()) {
            ConsoleUtils.printError("No subjects registered in the database.");
            return null;
        }
        System.out.println("  Subjects:");
        subjects.forEach(subject ->
                System.out.println("    " + subject.toDisplayString()));
        System.out.println();
        int subjectId = ConsoleUtils.readInt("  Subject id (0 = cancel): ",
                1, Integer.MAX_VALUE, 0);
        if (subjectId == 0) {
            return null;
        }
        SubjectSummary subject = subjects.stream()
                .filter(candidate -> candidate.getId() == subjectId)
                .findFirst()
                .orElse(null);
        if (subject == null) {
            ConsoleUtils.printError("No subject found with id " + subjectId + ".");
            return null;
        }
        if (!subject.isActive()) {
            ConsoleUtils.printError("Subject '" + subject.getName()
                    + "' is inactive and cannot receive assignments.");
            return null;
        }
        return subject;
    }

    private void showTeacherAssignments(Teacher teacher) throws SQLException {
        List<TeacherAssignment> assignments = assignmentDao.assignmentsByTeacher(teacher.getId());
        System.out.println();
        System.out.println("  Current assignments of '" + teacher.getFullName() + "':");
        if (assignments.isEmpty()) {
            System.out.println("    (none yet)");
        } else {
            assignments.forEach(assignment ->
                    System.out.println("    #" + assignment.getId() + " - "
                            + assignment.toDisplayString()));
        }
        System.out.println("  NOTE: the system does not store class day/time (not supported "
                + "by the data model), so verify visually that time slots do not overlap.");
        System.out.println();
    }
}
