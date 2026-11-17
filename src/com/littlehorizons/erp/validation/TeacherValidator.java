package com.littlehorizons.erp.validation;

import com.littlehorizons.erp.model.Teacher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain validator for teachers: identity, required e-mail and a coherent
 * hiring date (no future dates, inside the academic year bounds).
 */
public final class TeacherValidator {

    /** Document types accepted by the {@code profesores} table. */
    private static final List<String> ALLOWED_TYPES = List.of("CC", "CE", "Pasaporte");

    public List<String> validate(Teacher teacher) {
        List<String> errors = new ArrayList<>();
        if (teacher == null) {
            errors.add("The teacher data is missing.");
            return errors;
        }
        String type = teacher.getDocumentType() == null
                ? null : teacher.getDocumentType().toString();
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            errors.add("The document type must be one of: "
                    + String.join(", ", ALLOWED_TYPES) + ".");
        } else {
            String number = teacher.getDocumentNumber();
            if (number == null || number.isBlank()) {
                errors.add("The document number is required.");
            } else if (!FormatValidators.isValidDocumentNumber(type, number)) {
                errors.add("The " + type + " number is not valid: "
                        + FormatValidators.documentRuleDescription(type) + ".");
            }
        }
        if (isBlank(teacher.getFirstName())) {
            errors.add("The first name is required.");
        }
        if (isBlank(teacher.getLastName())) {
            errors.add("The first last name is required.");
        }
        if (isBlank(teacher.getEmail())) {
            errors.add("The e-mail is required.");
        } else if (!FormatValidators.isValidEmail(teacher.getEmail())) {
            errors.add("The e-mail is not valid.");
        }
        if (!FormatValidators.isValidOptionalPhone(teacher.getPhone())) {
            errors.add("The phone must be a Colombian mobile (3xx xxx xxxx) "
                    + "or landline (60x xxx xxxx).");
        }
        LocalDate hireDate = teacher.getHireDate();
        if (hireDate == null) {
            errors.add("The hire date is required.");
        } else {
            if (hireDate.isAfter(LocalDate.now())) {
                errors.add("The hire date cannot be in the future.");
            }
            int hireYear = hireDate.getYear();
            if (hireYear < ValidationRules.MIN_YEAR || hireYear > ValidationRules.MAX_YEAR) {
                errors.add("The hire year must be between "
                        + ValidationRules.MIN_YEAR + " and "
                        + ValidationRules.MAX_YEAR + ".");
            }
        }
        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
