package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.LoanSummary;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.StudentSummary;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Data access contract for the {@code prestamos_estudiante} table. */
public interface StudentLoanDao {

    /** Inserts a loan and returns its generated id. */
    int save(StudentLoan loan) throws SQLException;

    StudentLoan findById(int id) throws SQLException;

    List<StudentLoan> findAll() throws SQLException;

    boolean update(StudentLoan loan) throws SQLException;

    boolean delete(int id) throws SQLException;

    /**
     * Atomically inserts the loan and marks the related resource as 'Prestado'.
     * Rolls back and throws if the resource is no longer 'Disponible'.
     */
    int saveAndLoanResource(StudentLoan loan) throws SQLException;

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

    /** Id + full name of every active student, for the borrower picker. */
    List<StudentSummary> listActiveStudents() throws SQLException;

    List<LoanSummary> findActiveSummaries() throws SQLException;

    List<LoanSummary> findOverdueSummaries(LocalDate today) throws SQLException;

    /** All loans (active and historical) of one student. */
    List<LoanSummary> findSummariesByStudent(int studentId) throws SQLException;
}
