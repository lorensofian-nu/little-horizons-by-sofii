package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.DocumentType;
import com.littlehorizons.erp.model.Gender;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentStatus;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for the student CRUD flow. All business validations
 * (unique document, coherent dates) are enforced here.
 */
public class StudentController {

    private final StudentDao studentDao;

    public StudentController(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    /** Full student registration form. */
    public void createStudent() {
        ConsoleUtils.printHeader("REGISTER NEW STUDENT");
        try {
            Student student = readStudentForm(null);
            if (student == null) {
                return;
            }
            if (studentDao.findByDocumentNumber(student.getDocumentNumber()) != null) {
                ConsoleUtils.printError("A student with document " + student.getDocumentNumber()
                        + " already exists.");
                return;
            }
            int id = studentDao.save(student);
            ConsoleUtils.printSuccess("Student registered with id " + id + ": " + student.getFullName());
            LoggerUtil.logInfo("Student created: id=" + id + ", document=" + student.getDocumentNumber());
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not register the student.");
            LoggerUtil.logError("StudentController.createStudent", exception);
        }
    }

    /** Lists all students or filters by name/document using Stream API. */
    public void listStudents() {
        ConsoleUtils.printHeader("STUDENT LIST");
        try {
            String term = ConsoleUtils.readLine("Search by name or document (empty = show all): ");
            List<Student> students = term.isBlank()
                    ? studentDao.findAll()
                    : studentDao.searchByNameOrDocument(term);
            if (students.isEmpty()) {
                ConsoleUtils.printError("No students found.");
                return;
            }
            students.stream()
                    .sorted(Comparator.comparing(Student::getLastName)
                            .thenComparing(Student::getFirstName))
                    .forEach(student -> System.out.println("  " + student));
            long total = students.stream().count();
            System.out.println("  Total: " + total + " student(s).");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not retrieve the student list.");
            LoggerUtil.logError("StudentController.listStudents", exception);
        }
    }

    /** Updates an existing student keeping current values when input is empty. */
    public void updateStudent() {
        ConsoleUtils.printHeader("UPDATE STUDENT");
        try {
            Student current = findStudentByIdOrCancel();
            if (current == null) {
                return;
            }
            System.out.println("  Current: " + current);
            Student updated = readStudentForm(current);
            if (updated == null) {
                return;
            }
            Student duplicate = studentDao.findByDocumentNumber(updated.getDocumentNumber());
            if (duplicate != null && duplicate.getId() != current.getId()) {
                ConsoleUtils.printError("Document " + updated.getDocumentNumber()
                        + " already belongs to another student.");
                return;
            }
            updated.setId(current.getId());
            studentDao.update(updated);
            ConsoleUtils.printSuccess("Student updated: " + updated.getFullName());
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not update the student.");
            LoggerUtil.logError("StudentController.updateStudent", exception);
        }
    }

    /** Changes the lifecycle status of a student (soft delete uses 'Retirado'). */
    public void changeStudentStatus() {
        ConsoleUtils.printHeader("CHANGE STUDENT STATUS");
        try {
            Student student = findStudentByIdOrCancel();
            if (student == null) {
                return;
            }
            System.out.println("  Current status: " + student.getStatus());
            StudentStatus newStatus = readStatus(student.getStatus());
            student.setStatus(newStatus);
            studentDao.update(student);
            ConsoleUtils.printSuccess("Status changed to '" + newStatus + "'.");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not change the student status.");
            LoggerUtil.logError("StudentController.changeStudentStatus", exception);
        }
    }

    /**
     * Deletes a student. Soft delete (estado = 'Retirado') is offered first;
     * hard delete is only allowed after an explicit confirmation because the
     * CASCADE removes enrollments and guardian links.
     */
    public void deleteStudent() {
        ConsoleUtils.printHeader("DELETE STUDENT");
        try {
            Student student = findStudentByIdOrCancel();
            if (student == null) {
                return;
            }
            System.out.println("  " + student);
            if (ConsoleUtils.confirm("Mark this student as 'Retirado' (recommended soft delete)?")) {
                student.setStatus(StudentStatus.WITHDRAWN);
                studentDao.update(student);
                ConsoleUtils.printSuccess("Student marked as 'Retirado'.");
                return;
            }
            if (ConsoleUtils.confirm("PERMANENTLY delete the student? All enrollments and "
                    + "guardian links will be removed (CASCADE).")) {
                studentDao.deleteById(student.getId());
                ConsoleUtils.printSuccess("Student permanently deleted.");
                LoggerUtil.logInfo("Student hard-deleted: id=" + student.getId());
            } else {
                System.out.println("  Operation cancelled.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not delete the student.");
            LoggerUtil.logError("StudentController.deleteStudent", exception);
        }
    }

    /**
     * Lightweight list of active students (id + full name) used by other
     * flows such as enrollments and guardian linking.
     */
    public List<StudentSummary> getActiveStudentSummaries() throws SQLException {
        return studentDao.findActiveSummaries().stream()
                .sorted(Comparator.comparing(StudentSummary::getFullName))
                .toList();
    }

    /** Reads a student id from the console and returns the student, or null if cancelled/not found. */
    private Student findStudentByIdOrCancel() throws SQLException {
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

    /**
     * Reads the full student form. When {@code current} is not null the current
     * values become the fallback (empty input keeps them); when null the form
     * starts empty for a new registration. Returns null when cancelled.
     */
    private Student readStudentForm(Student current) throws SQLException {
        DocumentType documentType = readDocumentType(current == null ? DocumentType.TI : current.getDocumentType());
        String documentNumber = ConsoleUtils.readRequiredText("Document number: ");
        String firstName = ConsoleUtils.readRequiredText("First name"
                + fallbackHint(current == null ? null : current.getFirstName()) + ": ");
        String secondName = ConsoleUtils.readOptionalText("Second name"
                + fallbackHint(current == null ? null : current.getSecondName())
                + " (empty = none): ", current == null ? null : current.getSecondName());
        String lastName = ConsoleUtils.readRequiredText("First last name"
                + fallbackHint(current == null ? null : current.getLastName()) + ": ");
        String secondLastName = ConsoleUtils.readOptionalText("Second last name"
                + fallbackHint(current == null ? null : current.getSecondLastName())
                + " (empty = none): ", current == null ? null : current.getSecondLastName());

        LocalDate defaultBirthDate = current == null ? null : current.getBirthDate();
        LocalDate birthDate;
        if (defaultBirthDate == null) {
            birthDate = ConsoleUtils.readDate("Birth date");
        } else {
            birthDate = ConsoleUtils.readOptionalDate("Birth date", defaultBirthDate);
        }
        if (birthDate.isAfter(LocalDate.now())) {
            ConsoleUtils.printError("Birth date cannot be in the future.");
            return null;
        }

        Gender gender = readGender(current == null ? Gender.MALE : current.getGender());
        String address = ConsoleUtils.readOptionalText("Address"
                + fallbackHint(current == null ? null : current.getAddress())
                + " (empty = none): ", current == null ? null : current.getAddress());
        String phone = ConsoleUtils.readOptionalText("Phone"
                + fallbackHint(current == null ? null : current.getPhone())
                + " (empty = none): ", current == null ? null : current.getPhone());
        String email = ConsoleUtils.readOptionalText("Email"
                + fallbackHint(current == null ? null : current.getEmail())
                + " (empty = none): ", current == null ? null : current.getEmail());
        String bloodType = ConsoleUtils.readOptionalText("Blood type"
                + fallbackHint(current == null ? null : current.getBloodType())
                + " (empty = none): ", current == null ? null : current.getBloodType());
        String allergies = ConsoleUtils.readOptionalText("Allergies"
                + fallbackHint(current == null ? null : current.getAllergies())
                + " (empty = none): ", current == null ? null : current.getAllergies());
        String medicalConditions = ConsoleUtils.readOptionalText("Medical conditions"
                + fallbackHint(current == null ? null : current.getMedicalConditions())
                + " (empty = none): ", current == null ? null : current.getMedicalConditions());

        LocalDate defaultAdmissionDate = current == null ? LocalDate.now() : current.getAdmissionDate();
        LocalDate admissionDate = ConsoleUtils.readOptionalDate("Admission date", defaultAdmissionDate);
        if (admissionDate.isBefore(birthDate)) {
            ConsoleUtils.printError("Admission date cannot be before the birth date.");
            return null;
        }

        StudentStatus status = readStatus(current == null ? StudentStatus.ACTIVE : current.getStatus());

        Student student = new Student();
        student.setDocumentType(documentType);
        student.setDocumentNumber(documentNumber);
        student.setFirstName(firstName);
        student.setSecondName(secondName);
        student.setLastName(lastName);
        student.setSecondLastName(secondLastName);
        student.setBirthDate(birthDate);
        student.setGender(gender);
        student.setAddress(address);
        student.setPhone(phone);
        student.setEmail(email);
        student.setBloodType(bloodType);
        student.setAllergies(allergies);
        student.setMedicalConditions(medicalConditions);
        student.setAdmissionDate(admissionDate);
        student.setStatus(status);
        return student;
    }

    private String fallbackHint(String current) {
        return current == null || current.isBlank() ? "" : " (current: " + current + ")";
    }

    private DocumentType readDocumentType(DocumentType current) {
        while (true) {
            String raw = ConsoleUtils.readLine("Document type " + DocumentType.displayValues()
                    + " (empty = keep '" + current + "'): ");
            if (raw.isBlank()) {
                return current;
            }
            DocumentType match = DocumentType.fromDisplayValue(raw);
            if (match != null) {
                return match;
            }
            ConsoleUtils.printError("Invalid option. Choose one of: " + DocumentType.displayValues());
        }
    }

    private Gender readGender(Gender current) {
        while (true) {
            String raw = ConsoleUtils.readLine("Gender " + Gender.displayValues()
                    + " (empty = keep '" + current + "'): ");
            if (raw.isBlank()) {
                return current;
            }
            Gender match = Gender.fromDisplayValue(raw);
            if (match != null) {
                return match;
            }
            ConsoleUtils.printError("Invalid option. Choose one of: " + Gender.displayValues());
        }
    }

    private StudentStatus readStatus(StudentStatus current) {
        while (true) {
            String raw = ConsoleUtils.readLine("Status " + StudentStatus.displayValues()
                    + " (empty = keep '" + current + "'): ");
            if (raw.isBlank()) {
                return current;
            }
            StudentStatus match = StudentStatus.fromDisplayValue(raw);
            if (match != null) {
                return match;
            }
            ConsoleUtils.printError("Invalid option. Choose one of: " + StudentStatus.displayValues());
        }
    }
}
