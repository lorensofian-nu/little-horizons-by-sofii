package com.littlehorizons.erp.model;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Guardian (acudiente) entity mapped from the {@code acudientes} table.
 */
public class Guardian {

    private int id;
    private GuardianDocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String secondName;
    private String lastName;
    private String secondLastName;
    private String relationship;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String address;
    private String occupation;
    private boolean financialResponsible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Guardian() {
        // No-arg constructor.
    }

    public Guardian(int id, GuardianDocumentType documentType, String documentNumber,
                    String firstName, String secondName, String lastName, String secondLastName,
                    String relationship, String primaryPhone, String secondaryPhone,
                    String email, String address, String occupation, boolean financialResponsible,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.secondName = secondName;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.relationship = relationship;
        this.primaryPhone = primaryPhone;
        this.secondaryPhone = secondaryPhone;
        this.email = email;
        this.address = address;
        this.occupation = occupation;
        this.financialResponsible = financialResponsible;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Full name built from the four name columns, skipping empty parts. */
    public String getFullName() {
        return Arrays.stream(new String[]{firstName, secondName, lastName, secondLastName})
                .filter(part -> part != null && !part.isBlank())
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public GuardianDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(GuardianDocumentType documentType) {
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

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public boolean isFinancialResponsible() {
        return financialResponsible;
    }

    public void setFinancialResponsible(boolean financialResponsible) {
        this.financialResponsible = financialResponsible;
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
        return String.format("ID %-4d | %-11s %-15s | %-35s | %-15s | %s",
                id, documentType, documentNumber, getFullName(), relationship,
                financialResponsible ? "Financial responsible" : "Not financial responsible");
    }
}
