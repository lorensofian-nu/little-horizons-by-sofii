package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.StudentGuardianDao;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.GuardianDocumentType;
import com.littlehorizons.erp.model.StudentGuardianLink;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.model.PageRequest;
import com.littlehorizons.erp.model.PageResult;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.PrivacyUtils;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for guardian CRUD and the student-guardian link flows.
 * Business rules enforced here: unique guardian document, a student must
 * keep at least one guardian, and no duplicate links.
 */
public class GuardianController {

    private final GuardianDao guardianDao;
    private final StudentGuardianDao studentGuardianDao;
    private final StudentDao studentDao;

    public GuardianController(GuardianDao guardianDao, StudentGuardianDao studentGuardianDao,
                              StudentDao studentDao) {
        this.guardianDao = guardianDao;
        this.studentGuardianDao = studentGuardianDao;
        this.studentDao = studentDao;
    }

    /** Guardian registration form. */
    public void createGuardian() {
        ConsoleUtils.printHeader("REGISTER NEW GUARDIAN");
        try {
            Guardian guardian = readGuardianForm(null);
            if (guardian == null) {
                return;
            }
            if (guardianDao.findByDocumentNumber(guardian.getDocumentNumber()) != null) {
                ConsoleUtils.printError("A guardian with document "
                        + PrivacyUtils.maskIdentifier(guardian.getDocumentNumber())
                        + " already exists.");
                return;
            }
            int id = guardianDao.save(guardian);
            ConsoleUtils.printSuccess("Guardian registered with id " + id + ": " + guardian.getFullName());
            LoggerUtil.logInfo("Guardian created: id=" + id);
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not register the guardian.");
            LoggerUtil.logError("GuardianController.createGuardian", exception);
        }
    }

    /** Lists all guardians or filters by name/document using Stream API. */
    public void listGuardians() {
        ConsoleUtils.printHeader("GUARDIAN LIST");
        try {
            String term = ConsoleUtils.readLine("Search by name or document (empty = show all): ");
            int pageNumber = ConsoleUtils.readInt("Page number (1 = first): ",
                    1, 1_000_000, 1);
            int pageSize = ConsoleUtils.readInt("Page size (1-100): ", 1, 100, 20);
            PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
            PageResult<Guardian> page = term.isBlank()
                    ? guardianDao.findPage(pageRequest)
                    : guardianDao.searchPage(term, pageRequest);
            List<Guardian> guardians = page.items();
            if (guardians.isEmpty()) {
                ConsoleUtils.printError("No guardians found.");
                return;
            }
            guardians.stream()
                    .sorted(Comparator.comparing(Guardian::getLastName)
                            .thenComparing(Guardian::getFirstName))
                    .forEach(guardian -> System.out.println("  " + guardian));
            System.out.println("  Page " + pageNumber + " (" + guardians.size()
                    + " guardian(s))" + (page.hasNext() ? "; more results available." : "."));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not retrieve the guardian list.");
            LoggerUtil.logError("GuardianController.listGuardians", exception);
        }
    }

    /** Updates an existing guardian keeping current values when input is empty. */
    public void updateGuardian() {
        ConsoleUtils.printHeader("UPDATE GUARDIAN");
        try {
            Guardian current = findGuardianByIdOrCancel();
            if (current == null) {
                return;
            }
            System.out.println("  Current: " + current);
            Guardian updated = readGuardianForm(current);
            if (updated == null) {
                return;
            }
            Guardian duplicate = guardianDao.findByDocumentNumber(updated.getDocumentNumber());
            if (duplicate != null && duplicate.getId() != current.getId()) {
                ConsoleUtils.printError("Document " + PrivacyUtils.maskIdentifier(updated.getDocumentNumber())
                        + " already belongs to another guardian.");
                return;
            }
            updated.setId(current.getId());
            guardianDao.update(updated);
            ConsoleUtils.printSuccess("Guardian updated: " + updated.getFullName());
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not update the guardian.");
            LoggerUtil.logError("GuardianController.updateGuardian", exception);
        }
    }

    /**
     * Deletes a guardian. Referential restrictions prevent deleting a guardian
     * while student links still exist, preserving historical relationships.
     */
    public void deleteGuardian() {
        ConsoleUtils.printHeader("DELETE GUARDIAN");
        try {
            Guardian guardian = findGuardianByIdOrCancel();
            if (guardian == null) {
                return;
            }
            System.out.println("  " + guardian);
            List<StudentGuardianLink> linkedStudents =
                    studentGuardianDao.findStudentsOfGuardian(guardian.getId());
            if (!linkedStudents.isEmpty()) {
                System.out.println("  Linked students (they will lose this guardian):");
                linkedStudents.forEach(link -> System.out.println("    " + link));
            }
            if (ConsoleUtils.confirm("PERMANENTLY delete this guardian?")) {
                guardianDao.deleteById(guardian.getId());
                ConsoleUtils.printSuccess("Guardian deleted.");
                LoggerUtil.logInfo("Guardian deleted: id=" + guardian.getId());
            } else {
                System.out.println("  Operation cancelled.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not delete the guardian.");
            LoggerUtil.logError("GuardianController.deleteGuardian", exception);
        }
    }

    /** Links a guardian to a student (a guardian may be linked to several siblings). */
    public void assignGuardianToStudent() {
        ConsoleUtils.printHeader("ASSIGN GUARDIAN TO STUDENT");
        try {
            StudentSummary student = pickStudent();
            if (student == null) {
                return;
            }
            Guardian guardian = pickGuardian();
            if (guardian == null) {
                return;
            }
            if (studentGuardianDao.existsLink(student.getStudentId(), guardian.getId())) {
                ConsoleUtils.printError("Guardian '" + guardian.getFullName()
                        + "' is already linked to student '" + student.getFullName() + "'.");
                return;
            }
            StudentGuardianLink link = new StudentGuardianLink();
            link.setStudentId(student.getStudentId());
            link.setGuardianId(guardian.getId());
            link.setPrimaryContact(ConsoleUtils.confirm("Is this the primary guardian?"));
            link.setEmergencyContact(ConsoleUtils.confirm("Is this an emergency contact?"));
            studentGuardianDao.assign(link);
            ConsoleUtils.printSuccess("Guardian '" + guardian.getFullName()
                    + "' linked to student '" + student.getFullName() + "'.");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not assign the guardian.");
            LoggerUtil.logError("GuardianController.assignGuardianToStudent", exception);
        }
    }

    /** Removes a link, refusing to leave a student without any guardian. */
    public void removeGuardianLink() {
        ConsoleUtils.printHeader("REMOVE GUARDIAN LINK");
        try {
            StudentSummary student = pickStudent();
            if (student == null) {
                return;
            }
            List<StudentGuardianLink> links = studentGuardianDao.findGuardiansOfStudent(student.getStudentId());
            if (links.isEmpty()) {
                ConsoleUtils.printError("Student '" + student.getFullName() + "' has no linked guardians.");
                return;
            }
            System.out.println("  Linked guardians of '" + student.getFullName() + "':");
            links.forEach(link -> System.out.println("    " + link));
            int guardianId = ConsoleUtils.readInt("Guardian id to unlink (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
            if (guardianId == 0) {
                System.out.println("  Operation cancelled.");
                return;
            }
            if (links.stream().noneMatch(link -> link.getGuardianId() == guardianId)) {
                ConsoleUtils.printError("That guardian is not linked to this student.");
                return;
            }
            if (links.size() == 1) {
                ConsoleUtils.printError("Cannot remove the only guardian. Every student must keep "
                        + "at least one guardian. Assign another guardian first.");
                return;
            }
            if (ConsoleUtils.confirm("Remove this link?")) {
                studentGuardianDao.removeLink(student.getStudentId(), guardianId);
                ConsoleUtils.printSuccess("Link removed.");
            } else {
                System.out.println("  Operation cancelled.");
            }
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not remove the link.");
            LoggerUtil.logError("GuardianController.removeGuardianLink", exception);
        }
    }

    /** Lists the guardians of one student. */
    public void listGuardiansOfStudent() {
        ConsoleUtils.printHeader("GUARDIANS OF A STUDENT");
        try {
            StudentSummary student = pickStudent();
            if (student == null) {
                return;
            }
            List<StudentGuardianLink> links = studentGuardianDao.findGuardiansOfStudent(student.getStudentId());
            if (links.isEmpty()) {
                ConsoleUtils.printError("Student '" + student.getFullName() + "' has no linked guardians.");
                return;
            }
            links.stream()
                    .sorted(Comparator.comparing(StudentGuardianLink::getGuardianName))
                    .forEach(link -> System.out.println("  " + link));
            System.out.println("  Total: " + links.size() + " guardian(s).");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not retrieve the guardians.");
            LoggerUtil.logError("GuardianController.listGuardiansOfStudent", exception);
        }
    }

    /** Lists the students linked to one guardian (siblings report). */
    public void listStudentsOfGuardian() {
        ConsoleUtils.printHeader("STUDENTS OF A GUARDIAN");
        try {
            Guardian guardian = pickGuardian();
            if (guardian == null) {
                return;
            }
            List<StudentGuardianLink> links =
                    studentGuardianDao.findStudentsOfGuardian(guardian.getId());
            if (links.isEmpty()) {
                ConsoleUtils.printError("Guardian '" + guardian.getFullName()
                        + "' has no linked students.");
                return;
            }
            links.stream()
                    .sorted(Comparator.comparing(StudentGuardianLink::getStudentName))
                    .forEach(link -> System.out.println("  " + link));
            System.out.println("  Total: " + links.size() + " student(s).");
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not retrieve the students.");
            LoggerUtil.logError("GuardianController.listStudentsOfGuardian", exception);
        }
    }

    /** Reads a guardian id from the console, or null if cancelled/not found. */
    private Guardian findGuardianByIdOrCancel() throws SQLException {
        int id = ConsoleUtils.readInt("Guardian id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (id == 0) {
            System.out.println("  Operation cancelled.");
            return null;
        }
        Guardian guardian = guardianDao.findById(id);
        if (guardian == null) {
            ConsoleUtils.printError("Guardian with id " + id + " not found.");
        }
        return guardian;
    }

    /** Shows the active students and asks for an id; null when cancelled or not found. */
    private StudentSummary pickStudent() throws SQLException {
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
        StudentSummary match = summaries.stream()
                .filter(summary -> summary.getStudentId() == id)
                .findFirst()
                .orElse(null);
        if (match == null) {
            ConsoleUtils.printError("Student with id " + id + " not found or not active.");
        }
        return match;
    }

    /** Shows the guardians and asks for an id; null when cancelled or not found. */
    private Guardian pickGuardian() throws SQLException {
        List<Guardian> guardians = guardianDao.findAll();
        if (guardians.isEmpty()) {
            ConsoleUtils.printError("There are no registered guardians.");
            return null;
        }
        System.out.println("  Registered guardians:");
        guardians.forEach(guardian -> System.out.println("    " + guardian));
        int id = ConsoleUtils.readInt("Guardian id (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (id == 0) {
            System.out.println("  Operation cancelled.");
            return null;
        }
        Guardian match = guardians.stream()
                .filter(guardian -> guardian.getId() == id)
                .findFirst()
                .orElse(null);
        if (match == null) {
            ConsoleUtils.printError("Guardian with id " + id + " not found.");
        }
        return match;
    }

    /**
     * Reads the guardian form. When {@code current} is not null the current
     * values become the fallback. Returns null when cancelled.
     */
    private Guardian readGuardianForm(Guardian current) {
        GuardianDocumentType documentType =
                readDocumentType(current == null ? GuardianDocumentType.CC : current.getDocumentType());
        String documentNumber = ConsoleUtils.readRequiredText("Document number: ");
        String firstName = ConsoleUtils.readRequiredText("First name: ");
        String secondName = ConsoleUtils.readOptionalText("Second name (empty = none): ",
                current == null ? null : current.getSecondName());
        String lastName = ConsoleUtils.readRequiredText("First last name: ");
        String secondLastName = ConsoleUtils.readOptionalText("Second last name (empty = none): ",
                current == null ? null : current.getSecondLastName());
        String relationship = ConsoleUtils.readRequiredText("Relationship (parentesco): ");
        String primaryPhone = ConsoleUtils.readRequiredText("Primary phone: ");
        String secondaryPhone = ConsoleUtils.readOptionalText("Secondary phone (empty = none): ",
                current == null ? null : current.getSecondaryPhone());
        String email = ConsoleUtils.readOptionalEmail("Email (empty = none): ",
                current == null ? null : current.getEmail());
        String address = ConsoleUtils.readOptionalText("Address (empty = none): ",
                current == null ? null : current.getAddress());
        String occupation = ConsoleUtils.readOptionalText("Occupation (empty = none): ",
                current == null ? null : current.getOccupation());
        boolean financialResponsible = current == null
                ? ConsoleUtils.confirm("Is this guardian the financial responsible?")
                : ConsoleUtils.confirm("Is this guardian the financial responsible? (current: "
                        + (current.isFinancialResponsible() ? "yes" : "no") + ")");

        Guardian guardian = new Guardian();
        guardian.setDocumentType(documentType);
        guardian.setDocumentNumber(documentNumber);
        guardian.setFirstName(firstName);
        guardian.setSecondName(secondName);
        guardian.setLastName(lastName);
        guardian.setSecondLastName(secondLastName);
        guardian.setRelationship(relationship);
        guardian.setPrimaryPhone(primaryPhone);
        guardian.setSecondaryPhone(secondaryPhone);
        guardian.setEmail(email);
        guardian.setAddress(address);
        guardian.setOccupation(occupation);
        guardian.setFinancialResponsible(financialResponsible);
        return guardian;
    }

    private GuardianDocumentType readDocumentType(GuardianDocumentType current) {
        while (true) {
            String raw = ConsoleUtils.readLine("Document type " + GuardianDocumentType.displayValues()
                    + " (empty = keep '" + current + "'): ");
            if (raw.isBlank()) {
                return current;
            }
            GuardianDocumentType match = GuardianDocumentType.fromDisplayValue(raw);
            if (match != null) {
                return match;
            }
            ConsoleUtils.printError("Invalid option. Choose one of: "
                    + GuardianDocumentType.displayValues());
        }
    }
}
