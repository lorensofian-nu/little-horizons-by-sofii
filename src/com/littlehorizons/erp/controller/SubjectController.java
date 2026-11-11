package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.impl.SubjectDaoImpl;
import com.littlehorizons.erp.model.GradeSummary;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller of the subject management CRUD ({@code materias}).
 * Business validations: unique subject code, weekly hours 1..40,
 * and a grade taken from the grados catalog.
 */
public class SubjectController {

    private static final int MIN_WEEKLY_HOURS = 1;
    private static final int MAX_WEEKLY_HOURS = 40;

    private final SubjectDao subjectDao;

    /** Creates the controller with its default DAO. */
    public SubjectController() {
        this.subjectDao = new SubjectDaoImpl();
    }

    /** Shows the subjects submenu. Option 0 goes back. */
    public void show() {
        while (true) {
            ConsoleUtils.printHeader("SUBJECTS MANAGEMENT");
            System.out.println(" 1. Create subject");
            System.out.println(" 2. List all subjects");
            System.out.println(" 3. List subjects by grade");
            System.out.println(" 4. Update subject");
            System.out.println(" 5. Delete subject");
            int option = ConsoleUtils.readMenuOption(" Select an option: ", 5);
            switch (option) {
                case 1 -> create();
                case 2 -> listAll();
                case 3 -> listByGrade();
                case 4 -> update();
                case 5 -> delete();
                case 0 -> {
                    return;
                }
                default -> { /* unreachable */ }
            }
        }
    }

    private void create() {
        ConsoleUtils.printHeader("CREATE SUBJECT");
        try {
            int gradeId = readGradeId();
            if (gradeId == 0) {
                return;
            }
            Subject subject = new Subject();
            subject.setCode(readUniqueCode(0));
            subject.setName(ConsoleUtils.readRequiredText(" Subject name: "));
            subject.setDescription(ConsoleUtils.readOptionalText(" Description (empty = none): ", null));
            subject.setGradeId(gradeId);
            subject.setWeeklyHours(ConsoleUtils.readInt(
                    " Weekly hours (1-40): ", MIN_WEEKLY_HOURS, MAX_WEEKLY_HOURS, 0));
            if (subject.getWeeklyHours() == 0) {
                return;
            }
            subject.setActive(ConsoleUtils.confirm(" Is the subject active? "));
            if (!ConsoleUtils.confirm(" Save this subject? ")) {
                return;
            }
            subjectDao.save(subject);
            ConsoleUtils.printSuccess("Subject created with code " + subject.getCode() + ".");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while creating the subject.");
            LoggerUtil.logError("SubjectController.create", exception);
        }
    }

    private void listAll() {
        ConsoleUtils.printHeader("SUBJECT LIST");
        try {
            List<Subject> subjects = subjectDao.findAll();
            if (subjects.isEmpty()) {
                ConsoleUtils.printError("No subjects registered yet.");
                return;
            }
            printSubjectTable(subjects);
            System.out.println(" Total: " + subjects.size() + " subject(s).");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while listing subjects.");
            LoggerUtil.logError("SubjectController.listAll", exception);
        }
    }

    private void listByGrade() {
        ConsoleUtils.printHeader("SUBJECTS BY GRADE");
        try {
            int gradeId = readGradeId();
            if (gradeId == 0) {
                return;
            }
            List<Subject> subjects = subjectDao.findByGrade(gradeId);
            if (subjects.isEmpty()) {
                ConsoleUtils.printError("No subjects registered for that grade.");
                return;
            }
            printSubjectTable(subjects);
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while listing subjects.");
            LoggerUtil.logError("SubjectController.listByGrade", exception);
        }
    }

    private void update() {
        ConsoleUtils.printHeader("UPDATE SUBJECT");
        try {
            Subject subject = readExistingSubject();
            if (subject == null) {
                return;
            }
            System.out.println(" Current values: " + subject);
            String currentCode = subject.getCode();
            String newCode = ConsoleUtils.readOptionalText(
                    " New code (empty = keep " + currentCode + "): ", currentCode);
            if (!newCode.equals(currentCode) && subjectDao.existsByCode(newCode, subject.getId())) {
                ConsoleUtils.printError("The code '" + newCode + "' is already used by another subject.");
                return;
            }
            subject.setCode(newCode);
            subject.setName(ConsoleUtils.readOptionalText(
                    " New name (empty = keep): ", subject.getName()));
            subject.setDescription(ConsoleUtils.readOptionalText(
                    " New description (empty = keep): ", subject.getDescription()));
            subject.setWeeklyHours(ConsoleUtils.readInt(
                    " New weekly hours (1-40): ", MIN_WEEKLY_HOURS, MAX_WEEKLY_HOURS, subject.getWeeklyHours()));
            subject.setActive(ConsoleUtils.confirm(" Is the subject active? "));
            if (!ConsoleUtils.confirm(" Save the changes? ")) {
                return;
            }
            if (subjectDao.update(subject)) {
                ConsoleUtils.printSuccess("Subject updated.");
            } else {
                ConsoleUtils.printError("The subject could not be updated.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while updating the subject.");
            LoggerUtil.logError("SubjectController.update", exception);
        }
    }

    private void delete() {
        ConsoleUtils.printHeader("DELETE SUBJECT");
        try {
            Subject subject = readExistingSubject();
            if (subject == null) {
                return;
            }
            System.out.println(" " + subject);
            if (ConsoleUtils.confirm(" Deleting also removes its enrollments, grades and attendance. Confirm? ")) {
                if (subjectDao.delete(subject.getId())) {
                    ConsoleUtils.printSuccess("Subject deleted.");
                } else {
                    ConsoleUtils.printError("The subject could not be deleted.");
                }
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Database error while deleting the subject.");
            LoggerUtil.logError("SubjectController.delete", exception);
        }
    }

    /** Shared table printing used by the list operations. */
    private void printSubjectTable(List<Subject> subjects) {
        // Grade names are resolved once into a map, avoiding repeated queries.
        Map<Integer, String> gradeNames = gradeNames();
        System.out.printf(" %-5s %-12s %-30s %-14s %5s %8s%n",
                "ID", "CODE", "NAME", "GRADE", "HOURS", "ACTIVE");
        System.out.println(" " + "-".repeat(80));
        subjects.forEach(subject -> System.out.printf(
                " %-5d %-12s %-30s %-14s %5d %8s%n",
                subject.getId(),
                subject.getCode(),
                truncate(subject.getName(), 30),
                truncate(gradeNames.getOrDefault(subject.getGradeId(), "#" + subject.getGradeId()), 14),
                subject.getWeeklyHours(),
                subject.isActive() ? "Yes" : "No"));
    }

    /** Maps grade id -> grade name with a stream, for the subject table. */
    private Map<Integer, String> gradeNames() {
        try {
            return subjectDao.listGrades().stream()
                    .collect(Collectors.toMap(GradeSummary::id, GradeSummary::name));
        } catch (SQLException exception) {
            LoggerUtil.logError("SubjectController.gradeNames", exception);
            return Map.of();
        }
    }

    /** Reads a subject code, checking uniqueness (excluding {@code excludeId}). */
    private String readUniqueCode(Integer excludeId) throws SQLException {
        while (true) {
            String code = ConsoleUtils.readRequiredText(" Subject code (e.g. MAT-5): ");
            if (!subjectDao.existsByCode(code, excludeId)) {
                return code;
            }
            ConsoleUtils.printError("The code '" + code + "' already exists. Use another code.");
        }
    }

    /** Reads the grade from the grados catalog; 0 cancels. */
    private int readGradeId() throws SQLException {
        List<GradeSummary> grades = subjectDao.listGrades();
        grades.forEach(grade -> System.out.println("  " + grade));
        int gradeId = ConsoleUtils.readInt(" Grade id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        boolean valid = grades.stream().anyMatch(grade -> grade.id() == gradeId);
        if (gradeId != 0 && !valid) {
            ConsoleUtils.printError("That grade id does not exist.");
            return 0;
        }
        return gradeId;
    }

    /** Asks for a subject id and validates that it exists; null cancels. */
    private Subject readExistingSubject() throws SQLException {
        int id = ConsoleUtils.readInt(" Subject id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (id == 0) {
            return null;
        }
        Subject subject = subjectDao.findById(id);
        if (subject == null) {
            ConsoleUtils.printError("No subject found with id " + id + ".");
        }
        return subject;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }
}
