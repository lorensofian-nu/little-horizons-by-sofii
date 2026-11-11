package com.littlehorizons.erp.model;

/**
 * Lightweight read-only view of a teacher (ID + full name) used by the
 * extracurricular module to pick supervisors/organizers without touching
 * other modules.
 */
public class TeacherSummary {

    private final int teacherId;
    private final String fullName;

    public TeacherSummary(int teacherId, String fullName) {
        this.teacherId = teacherId;
        this.fullName = fullName;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return teacherId + ". " + fullName;
    }
}
