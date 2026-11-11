package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentSummary;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code estudiantes} table.
 */
public interface StudentDao {

    /** Inserts a student and returns the generated key ({@code id_estudiante}). */
    int save(Student student) throws SQLException;

    Student findById(int id) throws SQLException;

    List<Student> findAll() throws SQLException;

    Student findByDocumentNumber(String documentNumber) throws SQLException;

    /** Search by full name fragments or document number. */
    List<Student> searchByNameOrDocument(String term) throws SQLException;

    boolean update(Student student) throws SQLException;

    boolean deleteById(int id) throws SQLException;

    int count() throws SQLException;

    /** Lightweight list of active students (id + full name) for other flows. */
    List<StudentSummary> findActiveSummaries() throws SQLException;
}
