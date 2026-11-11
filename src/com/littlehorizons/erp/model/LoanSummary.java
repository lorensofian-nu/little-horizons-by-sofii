package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Read-only view of a loan enriched with the resource and borrower names, used
 * by listing and reporting screens for both students and teachers.
 */
public class LoanSummary {

    private int id;
    private String resourceCode;
    private String resourceName;
    private String borrowerName;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate realReturnDate;
    private LoanState state;
    private String observations;

    public LoanSummary() {
        // No-arg constructor required by the project conventions.
    }

    public LoanSummary(int id, String resourceCode, String resourceName, String borrowerName,
                       LocalDate loanDate, LocalDate expectedReturnDate, LocalDate realReturnDate,
                       LoanState state, String observations) {
        this.id = id;
        this.resourceCode = resourceCode;
        this.resourceName = resourceName;
        this.borrowerName = borrowerName;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.realReturnDate = realReturnDate;
        this.state = state;
        this.observations = observations;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getRealReturnDate() {
        return realReturnDate;
    }

    public void setRealReturnDate(LocalDate realReturnDate) {
        this.realReturnDate = realReturnDate;
    }

    public LoanState getState() {
        return state;
    }

    public void setState(LoanState state) {
        this.state = state;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    @Override
    public String toString() {
        return "LoanSummary{id=" + id + ", resource=" + resourceCode + ", borrower='" + borrowerName
                + "', state=" + state + "}";
    }
}
