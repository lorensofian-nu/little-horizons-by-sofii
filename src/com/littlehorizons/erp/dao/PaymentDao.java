package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.GuardianSummary;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.StudentSummary;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Data access contract for payments ({@code pagos}). */
public interface PaymentDao {

    /** Inserts a payment and returns its generated id. */
    int save(Payment payment) throws SQLException;

    Payment findById(int id) throws SQLException;

    List<Payment> findAll() throws SQLException;

    /** All payments of one student (most recent first). */
    List<Payment> findByStudent(int studentId) throws SQLException;

    /** All payments of every student linked to the guardian via {@code estudiante_acudiente}. */
    List<Payment> findByGuardian(int guardianId) throws SQLException;

    /** Newest payment date of a student in the given academic year, or null. */
    LocalDate findLastPaymentDate(int studentId, int academicYear) throws SQLException;

    /** Newest payment date per student id for the given academic year (used by the delinquency report). */
    Map<Integer, LocalDate> findLastPaymentDatesByYear(int academicYear) throws SQLException;

    /** Next sequential receipt number, e.g. {@code REC-2026-000001}. */
    String nextReceiptNumber(int academicYear) throws SQLException;

    // ------------------------------------------------------------------
    // Read-only helpers shared with other modules (kept dependency-free).
    // ------------------------------------------------------------------

    /** Active students as {@code id + full name}, ordered by last name. */
    List<StudentSummary> listActiveStudents() throws SQLException;

    /** All guardians as {@code id + full name + document}, ordered by last name. */
    List<GuardianSummary> listGuardians() throws SQLException;

    /** Guardians linked to one student via {@code estudiante_acudiente} (principal first). */
    List<GuardianSummary> findGuardiansByStudent(int studentId) throws SQLException;

    /** Loads a single guardian by id, or null. */
    GuardianSummary findGuardianById(int guardianId) throws SQLException;

    /** Loads a single active student by id, or null. */
    StudentSummary findStudentById(int studentId) throws SQLException;
}
