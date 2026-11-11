package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A registered payment, mapped from the {@code pagos} table.
 * {@code conceptName} is a read-only display field filled by JOIN queries.
 */
public class Payment {

    private int id;
    private int studentId;
    private Integer guardianId;
    private int conceptId;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private PaymentMethod method;
    private String receiptNumber;
    private Integer academicYear;
    private PaymentPeriod period;
    private String notes;
    private LocalDateTime creationDate;

    /** Display-only field (name of the related payment concept). */
    private String conceptName;

    public Payment() {
    }

    public Payment(int id, int studentId, Integer guardianId, int conceptId, BigDecimal amountPaid,
                   LocalDate paymentDate, PaymentMethod method, String receiptNumber,
                   Integer academicYear, PaymentPeriod period, String notes, LocalDateTime creationDate) {
        this.id = id;
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.conceptId = conceptId;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.method = method;
        this.receiptNumber = receiptNumber;
        this.academicYear = academicYear;
        this.period = period;
        this.notes = notes;
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Integer getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(Integer guardianId) {
        this.guardianId = guardianId;
    }

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public PaymentPeriod getPeriod() {
        return period;
    }

    public void setPeriod(PaymentPeriod period) {
        this.period = period;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    @Override
    public String toString() {
        return String.format("Payment{id=%d, student=%d, concept=%d, amount=%s, date=%s, method=%s, receipt='%s'}",
                id, studentId, conceptId, amountPaid, paymentDate, method, receiptNumber);
    }
}
