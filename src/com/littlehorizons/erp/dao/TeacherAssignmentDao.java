package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.SubjectSummary;
import com.littlehorizons.erp.model.TeacherAssignment;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Data access contract for the {@code asignaciones_profesor} table.
 */
public interface TeacherAssignmentDao {

    /** Inserts a new assignment and returns the generated {@code id_asignacion}. */
    int save(TeacherAssignment assignment) throws SQLException;

    /** Finds an assignment by primary key. */
    Optional<TeacherAssignment> findById(int id) throws SQLException;

    /** Returns every assignment, enriched with teacher and subject data via JOINs. */
    List<TeacherAssignment> findAll() throws SQLException;

    /** Lists every assignment of one teacher, ordered by year and subject. */
    List<TeacherAssignment> assignmentsByTeacher(int teacherId) throws SQLException;

    /** Lists every assignment of one subject in one academic year, ordered by section. */
    List<TeacherAssignment> assignmentsBySubjectAndYear(int subjectId, int academicYear)
            throws SQLException;

    /** Checks the unique key (teacher + subject + year + section) before inserting. */
    boolean existsAssignment(int teacherId, int subjectId, int academicYear, String section)
            throws SQLException;

    boolean hasScheduleConflict(int teacherId, int academicYear, String weekday,
                                LocalTime startTime, LocalTime endTime) throws SQLException;

    /** Hard-deletes an assignment by primary key. */
    boolean delete(int id) throws SQLException;

    /**
     * Read-only helper: lists subjects from {@code materias} so the assignment
     * flow can offer a pick list without depending on the subjects module.
     */
    List<SubjectSummary> listSubjects() throws SQLException;
}
