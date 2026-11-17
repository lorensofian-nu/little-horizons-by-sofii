package com.littlehorizons.erp.validation;

import com.littlehorizons.erp.model.Guardian;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain validator for guardians: identity per document type, required
 * contact data and contact formats.
 */
public final class GuardianValidator {

    /** Document types accepted by the {@code acudientes} table. */
    private static final List<String> ALLOWED_TYPES = List.of("CC", "CE", "Pasaporte", "PPT");

    public List<String> validate(Guardian guardian) {
        List<String> errors = new ArrayList<>();
        if (guardian == null) {
            errors.add("The guardian data is missing.");
            return errors;
        }
        String type = guardian.getDocumentType() == null
                ? null : guardian.getDocumentType().toString();
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            errors.add("The document type must be one of: "
                    + String.join(", ", ALLOWED_TYPES) + ".");
        } else {
            String number = guardian.getDocumentNumber();
            if (number == null || number.isBlank()) {
                errors.add("The document number is required.");
            } else if (!FormatValidators.isValidDocumentNumber(type, number)) {
                errors.add("The " + type + " number is not valid: "
                        + FormatValidators.documentRuleDescription(type) + ".");
            }
        }
        if (isBlank(guardian.getFirstName())) {
            errors.add("The first name is required.");
        }
        if (isBlank(guardian.getLastName())) {
            errors.add("The first last name is required.");
        }
        if (isBlank(guardian.getRelationship())) {
            errors.add("The relationship (parentesco) is required.");
        }
        if (isBlank(guardian.getPrimaryPhone())) {
            errors.add("The primary phone is required.");
        } else if (!FormatValidators.isValidPhone(guardian.getPrimaryPhone())) {
            errors.add("The primary phone must be a Colombian mobile (3xx xxx xxxx) "
                    + "or landline (60x xxx xxxx).");
        }
        if (!FormatValidators.isValidOptionalPhone(guardian.getSecondaryPhone())) {
            errors.add("The secondary phone must be a Colombian mobile (3xx xxx xxxx) "
                    + "or landline (60x xxx xxxx).");
        }
        if (guardian.getEmail() != null && !guardian.getEmail().isBlank()
                && !FormatValidators.isValidEmail(guardian.getEmail())) {
            errors.add("The guardian e-mail is not valid.");
        }
        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
