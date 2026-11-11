package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Extracurricular event, mapped from the {@code eventos} table.
 */
public class Event {

    private int eventId;
    private String eventName;
    private String description;
    private EventType eventType;
    private LocalDate eventDate;
    private String place;
    private boolean hasCompetition;
    private Integer organizerId;
    private boolean active;
    private LocalDateTime creationDate;

    /** Read-only display field filled by DAO joins. */
    private String organizerName;

    public Event() {
        // No-arg constructor.
    }

    public Event(int eventId, String eventName, String description, EventType eventType,
                 LocalDate eventDate, String place, boolean hasCompetition, Integer organizerId,
                 boolean active, LocalDateTime creationDate, String organizerName) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.place = place;
        this.hasCompetition = hasCompetition;
        this.organizerId = organizerId;
        this.active = active;
        this.creationDate = creationDate;
        this.organizerName = organizerName;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public boolean isHasCompetition() {
        return hasCompetition;
    }

    public void setHasCompetition(boolean hasCompetition) {
        this.hasCompetition = hasCompetition;
    }

    public Integer getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Integer organizerId) {
        this.organizerId = organizerId;
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

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    @Override
    public String toString() {
        return "Event{" + eventId + ", " + eventName + ", " + eventDate
                + (hasCompetition ? ", competition" : "") + (active ? ", active" : ", inactive") + '}';
    }
}
