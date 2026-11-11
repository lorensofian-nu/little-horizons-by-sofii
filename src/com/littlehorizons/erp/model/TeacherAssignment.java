package com.littlehorizons.erp.model;

import java.time.LocalDateTime;

/**
 * Assignment of a teacher to a subject for an academic year and section,
 * mapped from the {@code asignaciones_profesor} table.
 *
 * <p>The {@code teacherFullName}, {@code subjectCode} and {@code subjectName}
 * fields are display-only: they are filled by the DAO queries through JOINs
 * with {@code profesores} and {@code materias} and are not columns of the
 * assignment table itself.</p>
 */
public class TeacherAssignment {

    private int id;
    private int teacherId;
    private int subjectId;
    private int academicYear;
    private String section;
    private LocalDateTime createdAt;

    /** Display-only fields filled by JOIN queries. */
    private String teacherFullName;
    private String subjectCode;
    private String subjectName;

    /** Default constructor required by the project conventions. */
    public TeacherAssignment() {
    }

    /** Full constructor with the persisted columns. */
    public TeacherAssignment(int id, int teacherId, int subjectId, int academicYear,
                             String section, LocalDateTime createdAt) {
        this.id = id;
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.section = section;
        this.createdAt = createdAt;
    }

    /** Creates a new (not yet persisted) assignment from user-provided data. */
    public static TeacherAssignment newAssignment(int teacherId, int subjectId,
                                                  int academicYear, String section) {
        return new TeacherAssignment(0, teacherId, subjectId, academicYear, section, null);
    }

    /** Short human-readable description used in listings. */
    public String toDisplayString() {
        return String.format("Year %d | Section %s | %s (%s)",
                academicYear,
                section == null ? "-" : section,
                subjectName == null ? ("Subject #" + subjectId) : subjectName,
                subjectCode == null ? "?" : subjectCode);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTeacherFullName() {
        return teacherFullName;
    }

    public void setTeacherFullName(String teacherFullName) {
        this.teacherFullName = teacherFullName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    @Override
    public String toString() {
        return "TeacherAssignment{id=" + id
                + ", teacherId=" + teacherId
                + ", subjectId=" + subjectId
                + ", year=" + academicYear
                + ", section='" + section + '\''
                + '}';
    }
}
