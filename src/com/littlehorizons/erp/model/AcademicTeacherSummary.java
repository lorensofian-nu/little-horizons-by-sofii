package com.littlehorizons.erp.model;

/**
 * Lightweight read-only summary of a teacher used by the academic module to
 * pick the teacher who registers grades or attendance, without importing the
 * teachers module classes.
 *
 * <p>Named with the academic prefix because {@code TeacherSummary} is already
 * used by the extracurricular module.</p>
 *
 * @param id             teacher id (id_profesor)
 * @param fullName       full name joined from first/second names and surnames
 * @param specialization area of specialization (especializacion), may be null
 */
public record AcademicTeacherSummary(int id, String fullName, String specialization) {

    @Override
    public String toString() {
        return "id " + id + " - " + fullName;
    }
}
