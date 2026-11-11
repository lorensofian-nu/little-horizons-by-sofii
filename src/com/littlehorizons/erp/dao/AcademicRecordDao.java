package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Period;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code registros_academicos} table.
 * Averages and reports are computed in the controller with the Stream API
 * over the lists returned here.
 */
public interface AcademicRecordDao {

    /** Inserts a new grade record and fills the generated id. */
    int save(AcademicRecord record) throws SQLException;

    AcademicRecord findById(int id) throws SQLException;

    List<AcademicRecord> findAll() throws SQLException;

    /** Full academic history of one student (all years). */
    List<AcademicRecord> findByStudent(int studentId) throws SQLException;

    /** Records of one student in one academic year. */
    List<AcademicRecord> findByStudentAndYear(int studentId, int academicYear) throws SQLException;

    /** Records of one student for one year and period (period may be null = all). */
    List<AcademicRecord> findByStudentYearPeriod(int studentId, int academicYear, Period period) throws SQLException;

    /** Course records of one subject in one year (period may be null = all). */
    List<AcademicRecord> findBySubjectYear(int subjectId, int academicYear, Period period) throws SQLException;

    /** Records registered by one teacher. */
    List<AcademicRecord> findByTeacher(int teacherId) throws SQLException;

    /** Records of one academic year. */
    List<AcademicRecord> findByYear(int academicYear) throws SQLException;

    boolean update(AcademicRecord record) throws SQLException;

    boolean delete(int id) throws SQLException;

    /**
     * Unique-record check: student + subject + year + period.
     * {@code excludeId} allows the record's own row during updates.
     */
    boolean existsFor(int studentId, int subjectId, int academicYear,
                      Period period, Integer excludeId) throws SQLException;
}
