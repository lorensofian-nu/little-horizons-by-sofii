package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily attendance of a student in a subject, mapped from table {@code asistencias}.
 *
 * <p>Spanish DB columns map to English fields:
 * id_asistencia → id, id_estudiante → studentId, id_materia → subjectId,
 * fecha_asistencia → attendanceDate, estado_asistencia → status,
 * observaciones → observations, registrado_por → registeredBy,
 * fecha_creacion → creationDate.</p>
 */
public class Attendance {

    private int id;
    private int studentId;
    private int subjectId;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String observations;
    private Integer registeredBy;
    private LocalDateTime creationDate;

    /** Default constructor. */
    public Attendance() {
    }

    /** Full constructor (without id and audit date). */
    public Attendance(int studentId, int subjectId, LocalDate attendanceDate,
                      AttendanceStatus status, String observations, Integer registeredBy) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.observations = observations;
        this.registeredBy = registeredBy;
    }

    /** Complete constructor including id and the audit date. */
    public Attendance(int id, int studentId, int subjectId, LocalDate attendanceDate,
                      AttendanceStatus status, String observations, Integer registeredBy,
                      LocalDateTime creationDate) {
        this(studentId, subjectId, attendanceDate, status, observations, registeredBy);
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

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Integer getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(Integer registeredBy) {
        this.registeredBy = registeredBy;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Attendance{id=" + id + ", studentId=" + studentId
                + ", subjectId=" + subjectId + ", date=" + attendanceDate
                + ", status=" + status + "}";
    }
}
