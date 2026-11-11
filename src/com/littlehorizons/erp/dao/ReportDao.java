package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AttendanceRecord;
import com.littlehorizons.erp.model.ClubOccupancyReport;
import com.littlehorizons.erp.model.EventParticipationRecord;
import com.littlehorizons.erp.model.GradeRecord;
import com.littlehorizons.erp.model.LastPaymentRecord;
import com.littlehorizons.erp.model.LoanOverdueReport;
import com.littlehorizons.erp.model.PaymentRecord;
import com.littlehorizons.erp.model.ReportRow;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Read-only data access for the reports and statistics module.
 *
 * <p>Every method executes SELECT statements only, all of them through
 * {@link java.sql.PreparedStatement}. Aggregation of raw rows into report
 * holders is done by the controller using the Stream API.</p>
 */
public interface ReportDao {

    // ---------- Dashboard ----------

    /** COUNT of students with estado = 'Activo'. */
    int countActiveStudents() throws SQLException;

    /** COUNT of teachers with estado = 'Activo'. */
    int countActiveTeachers() throws SQLException;

    /** COUNT of subjects with esta_activa = TRUE. */
    int countActiveSubjects() throws SQLException;

    /** COUNT of clubs with esta_activo = TRUE. */
    int countActiveClubs() throws SQLException;

    /** SUM of monto_pagado for payments made during the given year. */
    double totalPaymentsInYear(int year) throws SQLException;

    /** Number of resources per estado_recurso (label = state, value = count). */
    List<ReportRow> countResourcesByState() throws SQLException;

    // ---------- Academic ----------

    /** All grade records (calificaciones) registered for the academic year. */
    List<GradeRecord> findGradesByYear(int year) throws SQLException;

    // ---------- Attendance ----------

    /** All attendance records with fecha_asistencia inside the given year. */
    List<AttendanceRecord> findAttendanceByYear(int year) throws SQLException;

    // ---------- Financial ----------

    /** All payments with fecha_pago inside the given year. */
    List<PaymentRecord> findPaymentsByYear(int year) throws SQLException;

    /** Last payment date of every active student (null when never paid). */
    List<LastPaymentRecord> findLastPaymentDates() throws SQLException;

    // ---------- Extracurricular ----------

    /** Occupancy of every active club: memberships vs maximum capacity. */
    List<ClubOccupancyReport> findClubOccupancy() throws SQLException;

    /** Event participations for events celebrated during the given year. */
    List<EventParticipationRecord> findEventParticipations(int year)
            throws SQLException;

    // ---------- Resources ----------

    /** Number of loans (students + professors) per estado_prestamo. */
    List<ReportRow> countLoansByState() throws SQLException;

    /**
     * Loans still 'Activo' whose fecha_devolucion_esperada is before the given
     * date, from both the student and the professor loan tables.
     */
    List<LoanOverdueReport> findOverdueLoans(LocalDate referenceDate)
            throws SQLException;

    /** Number of resources per category and estado_recurso. */
    List<ReportRow> countInventoryByCategoryAndState() throws SQLException;
}
