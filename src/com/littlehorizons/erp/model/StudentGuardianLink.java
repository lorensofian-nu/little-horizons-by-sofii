package com.littlehorizons.erp.model;

import java.time.LocalDateTime;

/**
 * Link row between a student and a guardian, mapped from the
 * {@code estudiante_acudiente} table. The display names are optional
 * transient fields filled by the DAO when listing, purely for reports.
 */
public class StudentGuardianLink {

    private int studentId;
    private int guardianId;
    private boolean primaryContact;
    private boolean emergencyContact;
    private LocalDateTime createdAt;

    /** Transient: guardian full name (filled when listing guardians of a student). */
    private String guardianName;
    private String guardianRelationship;

    /** Transient: student full name (filled when listing students of a guardian). */
    private String studentName;

    public StudentGuardianLink() {
        // No-arg constructor.
    }

    public StudentGuardianLink(int studentId, int guardianId,
                               boolean primaryContact, boolean emergencyContact,
                               LocalDateTime createdAt) {
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.primaryContact = primaryContact;
        this.emergencyContact = emergencyContact;
        this.createdAt = createdAt;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(boolean primaryContact) {
        this.primaryContact = primaryContact;
    }

    public boolean isEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(boolean emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianRelationship() {
        return guardianRelationship;
    }

    public void setGuardianRelationship(String guardianRelationship) {
        this.guardianRelationship = guardianRelationship;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String toString() {
        if (guardianName != null) {
            return String.format("Guardian ID %-4d | %-35s | %-15s | %s | %s",
                    guardianId, guardianName, guardianRelationship == null ? "-" : guardianRelationship,
                    primaryContact ? "Primary" : "Secondary",
                    emergencyContact ? "Emergency contact" : "No emergency contact");
        }
        return String.format("Student ID %-4d | %-35s | %s | %s",
                studentId, studentName == null ? "-" : studentName,
                primaryContact ? "Primary" : "Secondary",
                emergencyContact ? "Emergency contact" : "No emergency contact");
    }
}
