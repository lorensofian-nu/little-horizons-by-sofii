package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.dao.impl.TeacherDaoImpl;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherDocumentType;
import com.littlehorizons.erp.model.TeacherStatus;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.PrivacyUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic of the teachers directory: CRUD plus the status-change
 * workflow preferred over hard delete.
 *
 * <p>All {@link SQLException}s are caught here, reported through
 * {@link ConsoleUtils#printError} and written to the log file with
 * {@link LoggerUtil#logError}. Business validations (unique document number,
 * unique email) print friendly errors to the console.</p>
 */
public class TeacherController {

    private final TeacherDao teacherDao;

    /** Builds the controller with its default DAO implementation. */
    public TeacherController() {
        this.teacherDao = new TeacherDaoImpl();
    }

    /** Creates a teacher from console input. */
    public void createTeacher() {
        try {
            ConsoleUtils.printHeader("REGISTER NEW TEACHER");
            Teacher teacher = readNewTeacherData();
            int id = teacherDao.save(teacher);
            ConsoleUtils.printSuccess("Teacher registered with id " + id + ".");
        } catch (SQLException exception) {
            handleSqlError("createTeacher", exception);
        }
    }

    /** Lists every teacher using the Stream API to format the report lines. */
    public void listAllTeachers() {
        try {
            ConsoleUtils.printHeader("TEACHER DIRECTORY (ALL)");
            List<Teacher> teachers = teacherDao.findAll();
            if (teachers.isEmpty()) {
                ConsoleUtils.printError("No teachers found in the database.");
                return;
            }
            System.out.println("  Total teachers: " + teachers.size());
            System.out.println();
            teachers.stream()
                    .map(this::formatListLine)
                    .forEach(line -> System.out.println("  " + line));
        } catch (SQLException exception) {
            handleSqlError("listAllTeachers", exception);
        }
    }

    /** Lists only active teachers (helper report required by the spec). */
    public void listActiveTeachers() {
        try {
            ConsoleUtils.printHeader("ACTIVE TEACHERS");
            List<Teacher> teachers = teacherDao.listActiveTeachers();
            if (teachers.isEmpty()) {
                ConsoleUtils.printError("There are no active teachers.");
                return;
            }
            teachers.stream()
                    .map(this::formatListLine)
                    .forEach(line -> System.out.println("  " + line));
        } catch (SQLException exception) {
            handleSqlError("listActiveTeachers", exception);
        }
    }

    /** Finds and shows one teacher by id. */
    public void findTeacherById() {
        try {
            ConsoleUtils.printHeader("FIND TEACHER BY ID");
            int id = ConsoleUtils.readInt("  Teacher id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
            if (id == 0) {
                return;
            }
            Optional<Teacher> teacher = teacherDao.findById(id);
            if (teacher.isPresent()) {
                printTeacherDetail(teacher.get());
            } else {
                ConsoleUtils.printError("No teacher found with id " + id + ".");
            }
        } catch (SQLException exception) {
            handleSqlError("findTeacherById", exception);
        }
    }

    /** Finds and shows one teacher by document number. */
    public void findTeacherByDocument() {
        try {
            ConsoleUtils.printHeader("FIND TEACHER BY DOCUMENT");
            String document = ConsoleUtils.readRequiredText("  Document number: ");
            Optional<Teacher> teacher = teacherDao.findByDocument(document);
            if (teacher.isPresent()) {
                printTeacherDetail(teacher.get());
            } else {
                ConsoleUtils.printError("No teacher found with document "
                        + PrivacyUtils.maskIdentifier(document) + ".");
            }
        } catch (SQLException exception) {
            handleSqlError("findTeacherByDocument", exception);
        }
    }

    /** Updates an existing teacher, keeping current values as empty-input fallbacks. */
    public void updateTeacher() {
        try {
            ConsoleUtils.printHeader("UPDATE TEACHER");
            int id = ConsoleUtils.readInt("  Teacher id to update (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (id == 0) {
                return;
            }
            Optional<Teacher> optional = teacherDao.findById(id);
            if (optional.isEmpty()) {
                ConsoleUtils.printError("No teacher found with id " + id + ".");
                return;
            }
            Teacher current = optional.get();
            printTeacherDetail(current);
            if (!ConsoleUtils.confirm("  Do you want to edit this teacher?")) {
                return;
            }
            Teacher updated = readUpdateData(current);
            if (!isDocumentAvailable(updated.getDocumentNumber(), id)
                    || !isEmailAvailable(updated.getEmail(), id)) {
                return;
            }
            teacherDao.update(updated);
            ConsoleUtils.printSuccess("Teacher updated successfully.");
        } catch (SQLException exception) {
            handleSqlError("updateTeacher", exception);
        }
    }

    /** Changes only the employment status of a teacher. */
    public void changeTeacherStatus() {
        try {
            ConsoleUtils.printHeader("CHANGE TEACHER STATUS");
            int id = ConsoleUtils.readInt("  Teacher id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
            if (id == 0) {
                return;
            }
            Optional<Teacher> optional = teacherDao.findById(id);
            if (optional.isEmpty()) {
                ConsoleUtils.printError("No teacher found with id " + id + ".");
                return;
            }
            Teacher teacher = optional.get();
            printTeacherDetail(teacher);
            String newStatus = ConsoleUtils.readOption("  New status:",
                    TeacherStatus.valuesAsStrings());
            teacherDao.updateStatus(id, TeacherStatus.fromValue(newStatus));
            ConsoleUtils.printSuccess("Status changed to " + newStatus + ".");
        } catch (SQLException exception) {
            handleSqlError("changeTeacherStatus", exception);
        }
    }

    /** Hard-deletes a teacher after a double confirmation. */
    public void deleteTeacher() {
        try {
            ConsoleUtils.printHeader("DELETE TEACHER (PERMANENT)");
            int id = ConsoleUtils.readInt("  Teacher id to delete (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (id == 0) {
                return;
            }
            Optional<Teacher> optional = teacherDao.findById(id);
            if (optional.isEmpty()) {
                ConsoleUtils.printError("No teacher found with id " + id + ".");
                return;
            }
            Teacher teacher = optional.get();
            printTeacherDetail(teacher);
            ConsoleUtils.printError("This will permanently remove the teacher and all their "
                    + "subject assignments. Prefer the 'Change status' option when possible.");
            boolean first = ConsoleUtils.confirm("  Are you sure you want to delete this teacher?");
            boolean second = first && ConsoleUtils.confirm("  FINAL confirmation. Really delete?");
            if (!second) {
                ConsoleUtils.printSuccess("Operation cancelled, nothing was deleted.");
                return;
            }
            if (teacherDao.delete(id)) {
                ConsoleUtils.printSuccess("Teacher deleted.");
            } else {
                ConsoleUtils.printError("The teacher could not be deleted.");
            }
        } catch (SQLException exception) {
            handleSqlError("deleteTeacher", exception);
        }
    }

    // ------------------------------------------------------------------
    // Console input helpers
    // ------------------------------------------------------------------

    private Teacher readNewTeacherData() throws SQLException {
        Teacher teacher = new Teacher();
        fillBasicData(teacher, 0);
        return teacher;
    }

    private Teacher readUpdateData(Teacher current) {
        // Copy every current value first so empty console input keeps them.
        Teacher updated = new Teacher(current.getId(), current.getDocumentType(),
                current.getDocumentNumber(), current.getFirstName(), current.getSecondName(),
                current.getLastName(), current.getSecondLastName(), current.getEmail(),
                current.getPhone(), current.getSpecialization(), current.getHireDate(),
                current.getStatus(), current.getCreatedAt(), current.getUpdatedAt());
        fillBasicData(updated, current.getId());
        return updated;
    }

    /** Reads every teacher field; when {@code existingId} > 0 empty input keeps old values. */
    private void fillBasicData(Teacher teacher, int existingId) {
        boolean isUpdate = existingId > 0;
        String documentType = ConsoleUtils.readOption("  Document type:",
                TeacherDocumentType.valuesAsStrings());
        teacher.setDocumentType(TeacherDocumentType.fromValue(documentType));

        String documentNumber = isUpdate
                ? ConsoleUtils.readOptionalText("  Document number (empty = keep current): ",
                        "")
                : ConsoleUtils.readRequiredText("  Document number: ");
        if (documentNumber.isEmpty()) {
            documentNumber = teacher.getDocumentNumber();
        }
        teacher.setDocumentNumber(documentNumber);

        teacher.setFirstName(isUpdate
                ? ConsoleUtils.readOptionalText("  First name (empty = keep current): ",
                        teacher.getFirstName())
                : ConsoleUtils.readRequiredText("  First name: "));
        teacher.setSecondName(isUpdate
                ? ConsoleUtils.readOptionalText("  Second name (empty = keep current): ",
                        nullToEmpty(teacher.getSecondName()))
                : ConsoleUtils.readOptionalText("  Second name (optional): ", null));
        teacher.setLastName(isUpdate
                ? ConsoleUtils.readOptionalText("  First last name (empty = keep current): ",
                        teacher.getLastName())
                : ConsoleUtils.readRequiredText("  First last name: "));
        teacher.setSecondLastName(isUpdate
                ? ConsoleUtils.readOptionalText("  Second last name (empty = keep current): ",
                        nullToEmpty(teacher.getSecondLastName()))
                : ConsoleUtils.readOptionalText("  Second last name (optional): ", null));

        String email = ConsoleUtils.readOptionalEmail(
                "  Email (empty = keep current): ", nullToEmpty(teacher.getEmail()));
        teacher.setEmail(email.isEmpty() ? ConsoleUtils.readRequiredText("  Email: ") : email);

        teacher.setPhone(isUpdate
                ? ConsoleUtils.readOptionalText("  Phone (empty = keep current): ",
                        nullToEmpty(teacher.getPhone()))
                : ConsoleUtils.readOptionalText("  Phone (optional): ", null));

        teacher.setSpecialization(isUpdate
                ? ConsoleUtils.readOptionalText("  Specialization (empty = keep current): ",
                        nullToEmpty(teacher.getSpecialization()))
                : ConsoleUtils.readOptionalText("  Specialization (optional): ", null));

        LocalDate fallbackHireDate = teacher.getHireDate() == null ? LocalDate.now()
                : teacher.getHireDate();
        teacher.setHireDate(isUpdate
                ? ConsoleUtils.readOptionalDate("  Hire date", fallbackHireDate)
                : ConsoleUtils.readDate("  Hire date"));

        String status = ConsoleUtils.readOption("  Status:", TeacherStatus.valuesAsStrings());
        teacher.setStatus(TeacherStatus.fromValue(status));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    // ------------------------------------------------------------------
    // Business validations
    // ------------------------------------------------------------------

    private boolean isDocumentAvailable(String documentNumber, int currentId) throws SQLException {
        Optional<Teacher> found = teacherDao.findByDocument(documentNumber);
        if (found.isPresent() && found.get().getId() != currentId) {
            ConsoleUtils.printError("Document number " + PrivacyUtils.maskIdentifier(documentNumber)
                    + " already belongs to teacher id " + found.get().getId() + ".");
            return false;
        }
        return true;
    }

    private boolean isEmailAvailable(String email, int currentId) throws SQLException {
        Optional<Teacher> found = teacherDao.findByEmail(email);
        if (found.isPresent() && found.get().getId() != currentId) {
            ConsoleUtils.printError("That email already belongs to teacher id "
                    + found.get().getId() + ".");
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Output helpers
    // ------------------------------------------------------------------

    private String formatListLine(Teacher teacher) {
        return String.format("#%-5d %-16s %-35s %-30s %-9s %s",
                teacher.getId(),
                PrivacyUtils.maskIdentifier(teacher.getIdentityLabel()),
                teacher.getFullName(),
                teacher.getEmail() == null ? "-" : maskEmail(teacher.getEmail()),
                teacher.getStatus() == null ? "-" : teacher.getStatus().getValue(),
                teacher.getSpecialization() == null ? "-" : teacher.getSpecialization());
    }

    private void printTeacherDetail(Teacher teacher) {
        System.out.println("  Id:              " + teacher.getId());
        System.out.println("  Document:        " + PrivacyUtils.maskIdentifier(teacher.getIdentityLabel()));
        System.out.println("  Full name:       " + teacher.getFullName());
        System.out.println("  Email:           " + maskEmail(teacher.getEmail()));
        System.out.println("  Phone:           " + PrivacyUtils.maskIdentifier(teacher.getPhone()));
        System.out.println("  Specialization:  " + nullToEmpty(teacher.getSpecialization()));
        System.out.println("  Hire date:       " + teacher.getHireDate());
        System.out.println("  Status:          "
                + (teacher.getStatus() == null ? "-" : teacher.getStatus().getValue()));
        System.out.println();
    }

    private void handleSqlError(String context, SQLException exception) {
        ConsoleUtils.printError("A database error occurred. Check the log file for details.");
        LoggerUtil.logError("TeacherController." + context, exception);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "-";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
