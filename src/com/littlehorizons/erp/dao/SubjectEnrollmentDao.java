package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicStudentSummary;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.EnrollmentPeriod;
import com.littlehorizons.erp.model.AcademicTeacherSummary;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code inscripciones_materia} table, plus the
 * lightweight student/teacher lookup helpers that keep the academic module
 * independent from the students and teachers modules.
 */
public interface SubjectEnrollmentDao {

    /** Inserts a new enrollment and fills the generated id. */
    int save(SubjectEnrollment enrollment) throws SQLException;

    SubjectEnrollment findById(int id) throws SQLException;

    List<SubjectEnrollment> findAll() throws SQLException;

    /** Lists all enrollments of one student (all years). */
    List<SubjectEnrollment> findByStudent(int studentId) throws SQLException;

    /** Lists the enrollments of one student in one academic year. */
    List<SubjectEnrollment> findByStudentAndYear(int studentId, int academicYear) throws SQLException;

    /** Lists the enrollments of one subject in one academic year. */
    List<SubjectEnrollment> findBySubjectAndYear(int subjectId, int academicYear) throws SQLException;

    /** Lists the enrollments of one year and period. */
    List<SubjectEnrollment> findByYearAndPeriod(int academicYear, EnrollmentPeriod period) throws SQLException;

    boolean update(SubjectEnrollment enrollment) throws SQLException;

    boolean delete(int id) throws SQLException;

    /**
     * Unique-record check: student + subject + year + period.
     * {@code excludeId} allows the record's own row during updates.
     */
    boolean existsFor(int studentId, int subjectId, int academicYear,
                      EnrollmentPeriod period, Integer excludeId) throws SQLException;

    /**
     * Helper: lightweight list of students with an ACTIVE enrollment
     * (matriculas, estado_matricula = 'Activa') in the given year.
     */
    List<AcademicStudentSummary> listActiveStudents(int academicYear) throws SQLException;

    /** Helper: lightweight list of active teachers (estado = 'Activo'). */
    List<AcademicTeacherSummary> listActiveTeachers() throws SQLException;

    /**
     * Helper: verifies with a simple EXISTS query that the student has an
     * active enrollment (matriculas) for the given academic year.
     */
    boolean hasActiveEnrollment(int studentId, int academicYear) throws SQLException;
}
