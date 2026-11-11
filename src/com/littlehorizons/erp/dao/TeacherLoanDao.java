package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.LoanSummary;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.model.TeacherSummary;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Data access contract for the {@code prestamos_profesor} table. */
public interface TeacherLoanDao {

    /** Inserts a loan and returns its generated id. */
    int save(TeacherLoan loan) throws SQLException;

    TeacherLoan findById(int id) throws SQLException;

    List<TeacherLoan> findAll() throws SQLException;

    boolean update(TeacherLoan loan) throws SQLException;

    boolean delete(int id) throws SQLException;

    /**
     * Atomically inserts the loan and marks the related resource as 'Prestado'.
     * Rolls back and throws if the resource is no longer 'Disponible'.
     */
    int saveAndLoanResource(TeacherLoan loan) throws SQLException;

    /**
     * Atomically registers the return of an ACTIVE loan: sets the real return
     * date, marks the loan 'Devuelto' and puts the resource back to 'Disponible'.
     * Returns false when the loan does not exist or is not active.
     */
    boolean registerReturn(int loanId, LocalDate realReturnDate, String observations) throws SQLException;

    /** Marks every active loan past its expected return date as 'Vencido'. */
    int markOverdue(LocalDate today) throws SQLException;

    /** True when the resource currently has an active (not returned) loan. */
    boolean hasActiveLoanForResource(int resourceId) throws SQLException;

    /** Id + full name of every active teacher, for the borrower picker. */
    List<TeacherSummary> listActiveTeachers() throws SQLException;

    List<LoanSummary> findActiveSummaries() throws SQLException;

    List<LoanSummary> findOverdueSummaries(LocalDate today) throws SQLException;

    /** All loans (active and historical) of one teacher. */
    List<LoanSummary> findSummariesByTeacher(int teacherId) throws SQLException;
}
