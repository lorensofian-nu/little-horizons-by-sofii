package com.littlehorizons.erp.model;

import java.time.LocalDateTime;

/**
 * Participation of a student in an event, mapped from the
 * {@code participantes_evento} table.
 */
public class EventParticipation {

    private int participationId;
    private int eventId;
    private int studentId;
    private ParticipationRole participationRole;
    private String positionObtained;
    private String observations;
    private LocalDateTime creationDate;

    /** Read-only display fields filled by DAO joins. */
    private String studentFullName;
    private String eventName;

    public EventParticipation() {
        // No-arg constructor.
    }

    public EventParticipation(int participationId, int eventId, int studentId,
                              ParticipationRole participationRole, String positionObtained,
                              String observations, LocalDateTime creationDate,
                              String studentFullName, String eventName) {
        this.participationId = participationId;
        this.eventId = eventId;
        this.studentId = studentId;
        this.participationRole = participationRole;
        this.positionObtained = positionObtained;
        this.observations = observations;
        this.creationDate = creationDate;
        this.studentFullName = studentFullName;
        this.eventName = eventName;
    }

    public int getParticipationId() {
        return participationId;
    }

    public void setParticipationId(int participationId) {
        this.participationId = participationId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public ParticipationRole getParticipationRole() {
        return participationRole;
    }

    public void setParticipationRole(ParticipationRole participationRole) {
        this.participationRole = participationRole;
    }

    public String getPositionObtained() {
        return positionObtained;
    }

    public void setPositionObtained(String positionObtained) {
        this.positionObtained = positionObtained;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getStudentFullName() {
        return studentFullName;
    }

    public void setStudentFullName(String studentFullName) {
        this.studentFullName = studentFullName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public String toString() {
        return "EventParticipation{" + participationId + ", event=" + eventId + ", student="
                + studentId + ", role="
                + (participationRole == null ? "?" : participationRole.getLabel()) + '}';
    }
}
