package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enrollment of a student in a subject for an academic year and period,
 * mapped from table {@code inscripciones_materia}.
 *
 * <p>Spanish DB columns map to English fields:
 * id_inscripcion → id, id_estudiante → studentId, id_materia → subjectId,
 * año_academico → academicYear, periodo_academico → period,
 * seccion → section, fecha_inscripcion → enrollmentDate,
 * estado → status, fecha_creacion → creationDate.</p>
 */
public class SubjectEnrollment {

    private int id;
    private int studentId;
    private int subjectId;
    private int academicYear;
    private EnrollmentPeriod period;
    private String section;
    private LocalDate enrollmentDate;
    private SubjectEnrollmentStatus status;
    private LocalDateTime creationDate;

    /** Default constructor. */
    public SubjectEnrollment() {
    }

    /** Full constructor (without audit date and id). */
    public SubjectEnrollment(int studentId, int subjectId, int academicYear,
                             EnrollmentPeriod period, String section,
                             LocalDate enrollmentDate, SubjectEnrollmentStatus status) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.period = period;
        this.section = section;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
    }

    /** Complete constructor including id and the audit date. */
    public SubjectEnrollment(int id, int studentId, int subjectId, int academicYear,
                             EnrollmentPeriod period, String section,
                             LocalDate enrollmentDate, SubjectEnrollmentStatus status,
                             LocalDateTime creationDate) {
        this(studentId, subjectId, academicYear, period, section, enrollmentDate, status);
        this.id = id;
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

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public EnrollmentPeriod getPeriod() {
        return period;
    }

    public void setPeriod(EnrollmentPeriod period) {
        this.period = period;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public SubjectEnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(SubjectEnrollmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "SubjectEnrollment{id=" + id + ", studentId=" + studentId
                + ", subjectId=" + subjectId + ", year=" + academicYear
                + ", period=" + period + ", status=" + status + "}";
    }
}
