package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Teacher of the school, mapped from the {@code profesores} table.
 *
 * <p>Spanish column names are mapped to English fields:</p>
 * <ul>
 *   <li>{@code id_profesor} → id</li>
 *   <li>{@code tipo_documento} → documentType</li>
 *   <li>{@code numero_documento} → documentNumber</li>
 *   <li>{@code primer_nombre} / {@code segundo_nombre} → firstName / secondName</li>
 *   <li>{@code primer_apellido} / {@code segundo_apellido} → lastName / secondLastName</li>
 *   <li>{@code correo_electronico} → email</li>
 *   <li>{@code telefono} → phone</li>
 *   <li>{@code especializacion} → specialization</li>
 *   <li>{@code fecha_contratacion} → hireDate</li>
 *   <li>{@code estado} → status</li>
 *   <li>{@code fecha_creacion} / {@code fecha_actualizacion} → createdAt / updatedAt</li>
 * </ul>
 */
public class Teacher {

    private int id;
    private TeacherDocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String secondName;
    private String lastName;
    private String secondLastName;
    private String email;
    private String phone;
    private String specialization;
    private LocalDate hireDate;
    private TeacherStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Default constructor required by the project conventions. */
    public Teacher() {
    }

    /** Full constructor with every mapped column. */
    public Teacher(int id, TeacherDocumentType documentType, String documentNumber,
                   String firstName, String secondName, String lastName, String secondLastName,
                   String email, String phone, String specialization, LocalDate hireDate,
                   TeacherStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.secondName = secondName;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.email = email;
        this.phone = phone;
        this.specialization = specialization;
        this.hireDate = hireDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Builds the complete display name (all name parts, skipping empty ones). */
    public String getFullName() {
        StringBuilder name = new StringBuilder(firstName == null ? "" : firstName);
        appendIfPresent(name, secondName);
        appendIfPresent(name, lastName);
        appendIfPresent(name, secondLastName);
        return name.toString().trim();
    }

    private void appendIfPresent(StringBuilder builder, String part) {
        if (part != null && !part.isBlank()) {
            builder.append(' ').append(part.trim());
        }
    }

    /** Formats the identity label, e.g. {@code CC-1012345678}. */
    public String getIdentityLabel() {
        String type = documentType == null ? "" : documentType.getValue();
        return type + "-" + documentNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TeacherDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(TeacherDocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSecondLastName() {
        return secondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public TeacherStatus getStatus() {
        return status;
    }

    public void setStatus(TeacherStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Teacher{id=" + id
                + ", document='" + getIdentityLabel() + '\''
                + ", name='" + getFullName() + '\''
                + ", email='" + email + '\''
                + ", status=" + (status == null ? null : status.getValue())
                + '}';
    }
}
