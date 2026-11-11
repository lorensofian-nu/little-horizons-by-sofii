package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.StudentGuardianLink;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code estudiante_acudiente} link table.
 */
public interface StudentGuardianDao {

    /** Links a guardian to a student. Fails if the pair already exists (composite PK). */
    boolean assign(StudentGuardianLink link) throws SQLException;

    /** Removes the link between a student and a guardian. */
    boolean removeLink(int studentId, int guardianId) throws SQLException;

    boolean existsLink(int studentId, int guardianId) throws SQLException;

    /** Guardians linked to a student, with guardian names filled for display. */
    List<StudentGuardianLink> findGuardiansOfStudent(int studentId) throws SQLException;

    /** Students linked to a guardian, with student names filled for display. */
    List<StudentGuardianLink> findStudentsOfGuardian(int guardianId) throws SQLException;

    /** How many guardians are linked to the given student. */
    int countGuardiansForStudent(int studentId) throws SQLException;
}
