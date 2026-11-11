package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Grade (qualification) recorded by a teacher for a student in a subject,
 * mapped from table {@code registros_academicos}.
 *
 * <p>Spanish DB columns map to English fields:
 * id_registro → id, id_estudiante → studentId, id_materia → subjectId,
 * id_profesor → teacherId, año_academico → academicYear,
 * periodo_academico → period, valor_nota → grade, observaciones → observations,
 * fecha_registro → registrationDate, fecha_creacion → creationDate,
 * fecha_actualizacion → updateDate.</p>
 */
public class AcademicRecord {

    private int id;
    private int studentId;
    private int subjectId;
    private int teacherId;
    private int academicYear;
    private Period period;
    private double grade;
    private String observations;
    private LocalDate registrationDate;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    /** Default constructor. */
    public AcademicRecord() {
    }

    /** Full constructor (without id and audit dates). */
    public AcademicRecord(int studentId, int subjectId, int teacherId, int academicYear,
                          Period period, double grade, String observations,
                          LocalDate registrationDate) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.academicYear = academicYear;
        this.period = period;
        this.grade = grade;
        this.observations = observations;
        this.registrationDate = registrationDate;
    }

    /** Complete constructor including id and audit dates. */
    public AcademicRecord(int id, int studentId, int subjectId, int teacherId,
                          int academicYear, Period period, double grade, String observations,
                          LocalDate registrationDate, LocalDateTime creationDate,
                          LocalDateTime updateDate) {
        this(studentId, subjectId, teacherId, academicYear, period, grade,
                observations, registrationDate);
        this.id = id;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
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

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "AcademicRecord{id=" + id + ", studentId=" + studentId
                + ", subjectId=" + subjectId + ", year=" + academicYear
                + ", period=" + period + ", grade=" + grade + "}";
    }
}
