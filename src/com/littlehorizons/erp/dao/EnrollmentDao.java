package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Enrollment;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code matriculas} table.
 */
public interface EnrollmentDao {

    /** Inserts an enrollment and returns the generated key ({@code id_matricula}). */
    int save(Enrollment enrollment) throws SQLException;

    Enrollment findById(int id) throws SQLException;

    /** All enrollments, with student and grade names filled for display. */
    List<Enrollment> findAll() throws SQLException;

    List<Enrollment> findByStudentId(int studentId) throws SQLException;

    List<Enrollment> findByAcademicYear(int academicYear) throws SQLException;

    /** True when the student already has an enrollment for the academic year (UNIQUE rule). */
    boolean existsEnrollment(int studentId, int academicYear) throws SQLException;

    boolean update(Enrollment enrollment) throws SQLException;

    boolean deleteById(int id) throws SQLException;
}
