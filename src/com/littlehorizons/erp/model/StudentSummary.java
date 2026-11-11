package com.littlehorizons.erp.model;

/**
 * Lightweight read-only view of a student (ID + full name) used by the
 * extracurricular module to pick students without touching other modules.
 */
public class StudentSummary {

    private final int studentId;
    private final String fullName;

    public StudentSummary(int studentId, String fullName) {
        this.studentId = studentId;
        this.fullName = fullName;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return studentId + ". " + fullName;
    }
}
