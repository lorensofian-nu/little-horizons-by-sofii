package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enrollment entity mapped from the {@code matriculas} table.
 * Student and grade names are transient fields filled by the DAO for listings.
 */
public class Enrollment {

    private int id;
    private int studentId;
    private int gradeId;
    private int academicYear;
    private EnrollmentStatus status;
    private LocalDate enrollmentDate;
    private LocalDate withdrawalDate;
    private String observations;
    private LocalDateTime createdAt;

    /** Transient display fields for listings/reports. */
    private String studentName;
    private String gradeName;

    public Enrollment() {
        // No-arg constructor.
    }

    public Enrollment(int id, int studentId, int gradeId, int academicYear,
                      EnrollmentStatus status, LocalDate enrollmentDate,
                      LocalDate withdrawalDate, String observations, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.gradeId = gradeId;
        this.academicYear = academicYear;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
        this.withdrawalDate = withdrawalDate;
        this.observations = observations;
        this.createdAt = createdAt;
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

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    @Override
    public String toString() {
        return String.format("ID %-4d | Year %-4d | %-35s | %-15s | %-10s | Enrolled %s%s",
                id, academicYear,
                studentName == null ? "Student #" + studentId : studentName,
                gradeName == null ? "Grade #" + gradeId : gradeName,
                status == null ? "-" : status,
                enrollmentDate,
                withdrawalDate != null ? " | Withdrawn " + withdrawalDate : "");
    }
}
