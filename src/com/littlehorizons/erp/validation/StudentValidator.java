package com.littlehorizons.erp.validation;

import com.littlehorizons.erp.model.Student;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain validator for students. Returns every violation at once so the
 * console can show all the problems before the user retries the form.
 */
public final class StudentValidator {

    /** Document types accepted by the {@code estudiantes} table. */
    private static final List<String> ALLOWED_TYPES = List.of("TI", "CC", "CE", "Pasaporte");

    public List<String> validate(Student student) {
        List<String> errors = new ArrayList<>();
        if (student == null) {
            errors.add("The student data is missing.");
            return errors;
        }
        validateIdentity(student, errors);
        validateNames(student, errors);
        validateContact(student, errors);
        validateDates(student, errors);
        validateLifecycle(student, errors);
        return errors;
    }

    private void validateIdentity(Student student, List<String> errors) {
        String type = student.getDocumentType() == null
                ? null : student.getDocumentType().toString();
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            errors.add("The document type must be one of: " + String.join(", ", ALLOWED_TYPES) + ".");
        } else {
            String number = student.getDocumentNumber();
            if (number == null || number.isBlank()) {
                errors.add("The document number is required.");
            } else if (!FormatValidators.isValidDocumentNumber(type, number)) {
                errors.add("The " + type + " number is not valid: "
                        + FormatValidators.documentRuleDescription(type) + ".");
            }
        }
    }

    private void validateNames(Student student, List<String> errors) {
        if (isBlank(student.getFirstName())) {
            errors.add("The first name is required.");
        }
        if (isBlank(student.getLastName())) {
            errors.add("The first last name is required.");
        }
    }

    private void validateContact(Student student, List<String> errors) {
        if (student.getEmail() != null && !student.getEmail().isBlank()
                && !FormatValidators.isValidEmail(student.getEmail())) {
            errors.add("The student e-mail is not valid.");
        }
        if (!FormatValidators.isValidOptionalPhone(student.getPhone())) {
            errors.add("The phone must be a Colombian mobile (3xx xxx xxxx) "
                    + "or landline (60x xxx xxxx).");
        }
    }

    private void validateDates(Student student, List<String> errors) {
        LocalDate today = LocalDate.now();
        LocalDate birthDate = student.getBirthDate();
        if (birthDate == null) {
            errors.add("The birth date is required.");
        } else {
            if (birthDate.isBefore(LocalDate.of(1900, 1, 1))) {
                errors.add("The birth date must be after 1900-01-01.");
            }
            if (birthDate.isAfter(today)) {
                errors.add("The birth date cannot be in the future.");
            } else {
                int age = Period.between(birthDate, today).getYears();
                if (age < ValidationRules.MIN_STUDENT_AGE_YEARS
                        || age > ValidationRules.MAX_STUDENT_AGE_YEARS) {
                    errors.add("The student age (" + age + " years) must be between "
                            + ValidationRules.MIN_STUDENT_AGE_YEARS + " and "
                            + ValidationRules.MAX_STUDENT_AGE_YEARS + " years.");
                }
            }
        }
        LocalDate admissionDate = student.getAdmissionDate();
        if (admissionDate == null) {
            errors.add("The admission date is required.");
        } else {
            if (admissionDate.isAfter(today)) {
                errors.add("The admission date cannot be in the future.");
            }
            if (birthDate != null && admissionDate.isBefore(birthDate)) {
                errors.add("The admission date cannot be before the birth date.");
            }
            int admissionYear = admissionDate.getYear();
            if (admissionYear < ValidationRules.MIN_YEAR
                    || admissionYear > ValidationRules.MAX_YEAR) {
                errors.add("The admission year must be between "
                        + ValidationRules.MIN_YEAR + " and "
                        + ValidationRules.MAX_YEAR + ".");
            }
        }
    }

    private void validateLifecycle(Student student, List<String> errors) {
        if (student.getStatus() == null) {
            errors.add("The student status is required.");
        }
        if (student.getPrimaryGuardianId() == null) {
            errors.add("Every student needs a primary guardian before being saved.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
