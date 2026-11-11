package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Attendance;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Data access contract for the {@code asistencias} table. Attendance
 * patterns and percentages are computed in the controller with the Stream API.
 */
public interface AttendanceDao {

    /** Inserts a new attendance record and fills the generated id. */
    int save(Attendance attendance) throws SQLException;

    Attendance findById(int id) throws SQLException;

    List<Attendance> findAll() throws SQLException;

    /** Attendance history of one student (all subjects and years). */
    List<Attendance> findByStudent(int studentId) throws SQLException;

    /** Attendance of one subject on one date. */
    List<Attendance> findBySubjectAndDate(int subjectId, LocalDate date) throws SQLException;

    /** Attendance of one subject in one academic year. */
    List<Attendance> findBySubjectAndYear(int subjectId, int academicYear) throws SQLException;

    /** Attendance of one student in one academic year. */
    List<Attendance> findByStudentAndYear(int studentId, int academicYear) throws SQLException;

    /** All attendance of one calendar year, used for the reports. */
    List<Attendance> findByYear(int academicYear) throws SQLException;

    boolean update(Attendance attendance) throws SQLException;

    boolean delete(int id) throws SQLException;

    /**
     * Unique-record check: student + subject + date.
     * {@code excludeId} allows the record's own row during updates.
     */
    boolean existsFor(int studentId, int subjectId, LocalDate date, Integer excludeId) throws SQLException;
}
