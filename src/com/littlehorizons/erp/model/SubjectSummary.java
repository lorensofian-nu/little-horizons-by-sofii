package com.littlehorizons.erp.model;

/**
 * Read-only summary of a row of the {@code materias} table, used by the
 * teachers module to let the operator pick a subject for an assignment
 * without touching the subjects module code.
 */
public class SubjectSummary {

    private int id;
    private String code;
    private String name;
    private int gradeId;
    private int weeklyHours;
    private boolean active;

    /** Default constructor required by the project conventions. */
    public SubjectSummary() {
    }

    /** Full constructor. */
    public SubjectSummary(int id, String code, String name, int gradeId,
                          int weeklyHours, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.gradeId = gradeId;
        this.weeklyHours = weeklyHours;
        this.active = active;
    }

    /** Formats the subject as {@code MAT-01 | Mathematics}. */
    public String toDisplayString() {
        String flag = active ? "Active" : "Inactive";
        return String.format("#%d | %s | %s | Grade %d | %dh/week | %s",
                id, code, name, gradeId, weeklyHours, flag);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "SubjectSummary{id=" + id + ", code='" + code + "', name='" + name + "'}";
    }
}
