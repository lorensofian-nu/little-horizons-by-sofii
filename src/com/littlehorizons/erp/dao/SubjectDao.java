package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.GradeSummary;
import com.littlehorizons.erp.model.Subject;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code materias} table, plus read-only catalog
 * helpers for grades (grados) used by the academic module.
 */
public interface SubjectDao {

    /** Inserts a new subject and fills the generated id. */
    int save(Subject subject) throws SQLException;

    Subject findById(int id) throws SQLException;

    /** Lists all subjects ordered by grade level and code. */
    List<Subject> findAll() throws SQLException;

    /** Lists only active subjects. */
    List<Subject> findActive() throws SQLException;

    /** Lists subjects of one grade. */
    List<Subject> findByGrade(int gradeId) throws SQLException;

    /** Finds a subject by its unique code (codigo_materia). */
    Subject findByCode(String code) throws SQLException;

    boolean update(Subject subject) throws SQLException;

    boolean delete(int id) throws SQLException;

    /**
     * Checks whether the code is already taken. {@code excludeId} allows
     * re-using the subject's own code during an update (pass null for inserts).
     */
    boolean existsByCode(String code, Integer excludeId) throws SQLException;

    /** Catalog helper: lists the grade levels (grados) ordered by level. */
    List<GradeSummary> listGrades() throws SQLException;
}
