package com.littlehorizons.erp.model;

/**
 * Lightweight read-only summary of a student used by the academic module to
 * pick a student by id without importing the students module classes.
 *
 * <p>Named with the academic prefix because {@code StudentSummary} is already
 * used by the extracurricular module.</p>
 *
 * @param id       student id (id_estudiante)
 * @param fullName full name joined from first/second names and surnames
 * @param gradeId  grade of the active enrollment (id_grado) used to filter subjects
 */
public record AcademicStudentSummary(int id, String fullName, int gradeId) {

    @Override
    public String toString() {
        return "id " + id + " - " + fullName;
    }
}
