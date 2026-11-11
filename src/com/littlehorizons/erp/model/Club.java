package com.littlehorizons.erp.model;

import java.time.LocalDateTime;

/**
 * Extracurricular club, mapped from the {@code clubes} table.
 */
public class Club {

    private int clubId;
    private String clubName;
    private String description;
    private Integer supervisingTeacherId;
    private Integer maxCapacity;
    private String meetingSchedule;
    private boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    /** Read-only display field filled by DAO joins. */
    private String supervisingTeacherName;

    public Club() {
        // No-arg constructor.
    }

    public Club(int clubId, String clubName, String description, Integer supervisingTeacherId,
                Integer maxCapacity, String meetingSchedule, boolean active,
                LocalDateTime creationDate, LocalDateTime updateDate, String supervisingTeacherName) {
        this.clubId = clubId;
        this.clubName = clubName;
        this.description = description;
        this.supervisingTeacherId = supervisingTeacherId;
        this.maxCapacity = maxCapacity;
        this.meetingSchedule = meetingSchedule;
        this.active = active;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
        this.supervisingTeacherName = supervisingTeacherName;
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSupervisingTeacherId() {
        return supervisingTeacherId;
    }

    public void setSupervisingTeacherId(Integer supervisingTeacherId) {
        this.supervisingTeacherId = supervisingTeacherId;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getMeetingSchedule() {
        return meetingSchedule;
    }

    public void setMeetingSchedule(String meetingSchedule) {
        this.meetingSchedule = meetingSchedule;
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

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public String getSupervisingTeacherName() {
        return supervisingTeacherName;
    }

    public void setSupervisingTeacherName(String supervisingTeacherName) {
        this.supervisingTeacherName = supervisingTeacherName;
    }

    @Override
    public String toString() {
        return "Club{" + clubId + ", " + clubName + ", capacity=" + maxCapacity
                + (active ? ", active" : ", inactive") + '}';
    }
}
