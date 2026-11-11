package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A loan of a resource to a student, mapped from table {@code prestamos_estudiante}.
 */
public class StudentLoan {

    private int id;
    private int resourceId;
    private int studentId;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate realReturnDate;
    private LoanState state;
    private String observations;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

    public StudentLoan() {
        // No-arg constructor required by the project conventions.
    }

    public StudentLoan(int id, int resourceId, int studentId, LocalDate loanDate,
                       LocalDate expectedReturnDate, LocalDate realReturnDate, LoanState state,
                       String observations, LocalDateTime creationDate, LocalDateTime lastUpdateDate) {
        this.id = id;
        this.resourceId = resourceId;
        this.studentId = studentId;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.realReturnDate = realReturnDate;
        this.state = state;
        this.observations = observations;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String toString() {
        return "StudentLoan{id=" + id + ", resourceId=" + resourceId + ", studentId=" + studentId
                + ", state=" + state + "}";
    }
}
