package com.littlehorizons.erp.model;

/**
 * Document types accepted for a teacher, matching the MySQL ENUM values of the
 * {@code profesores.tipo_documento} column ('CC', 'CE', 'Pasaporte').
 */
public enum TeacherDocumentType {

    /** Cédula de ciudadanía. */
    CC("CC"),

    /** Cédula de extranjería. */
    CE("CE"),

    /** Passport. */
    PASAPORTE("Pasaporte");

    /** Exact value stored in the database column. */
    private final String value;

    TeacherDocumentType(String value) {
        this.value = value;
    }

    /** Returns the exact database string for this document type. */
    public String getValue() {
        return value;
    }

    /** Returns the list of database values, ready for console prompts. */
    public static java.util.List<String> valuesAsStrings() {
        return java.util.Arrays.stream(values())
                .map(TeacherDocumentType::getValue)
                .toList();
    }

    /** Maps a database value to the enum; throws if the value is unknown. */
    public static TeacherDocumentType fromValue(String value) {
        for (TeacherDocumentType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown document type: " + value);
    }
}
