package com.littlehorizons.erp.model;

import java.time.LocalDateTime;

/**
 * Subject of the academic offer, mapped from table {@code materias}.
 *
 * <p>Spanish DB columns map to English fields:
 * id_materia → id, codigo_materia → code, nombre_materia → name,
 * descripcion → description, id_grado → gradeId,
 * horas_semanales → weeklyHours, esta_activa → active,
 * fecha_creacion → creationDate.</p>
 */
public class Subject {

    private int id;
    private String code;
    private String name;
    private String description;
    private int gradeId;
    private int weeklyHours;
    private boolean active;
    private LocalDateTime creationDate;

    /** Default constructor. */
    public Subject() {
    }

    /** Full constructor (without audit date). */
    public Subject(int id, String code, String name, String description,
                   int gradeId, int weeklyHours, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.gradeId = gradeId;
        this.weeklyHours = weeklyHours;
        this.active = active;
    }

    /** Complete constructor including the audit date. */
    public Subject(int id, String code, String name, String description,
                   int gradeId, int weeklyHours, boolean active, LocalDateTime creationDate) {
        this(id, code, name, description, gradeId, weeklyHours, active);
        this.creationDate = creationDate;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Subject{id=" + id + ", code='" + code + "', name='" + name
                + "', gradeId=" + gradeId + ", weeklyHours=" + weeklyHours
                + ", active=" + active + "}";
    }
}
