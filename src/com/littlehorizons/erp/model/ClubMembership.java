package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Club membership, mapped from the {@code membresias_club} table.
 */
public class ClubMembership {

    private int membershipId;
    private int studentId;
    private int clubId;
    private LocalDate joinDate;
    private MembershipStatus membershipStatus;
    private LocalDate withdrawalDate;
    private String withdrawalReason;
    private LocalDateTime creationDate;

    /** Read-only display fields filled by DAO joins. */
    private String studentFullName;
    private String clubName;

    public ClubMembership() {
        // No-arg constructor.
    }

    public ClubMembership(int membershipId, int studentId, int clubId, LocalDate joinDate,
                          MembershipStatus membershipStatus, LocalDate withdrawalDate,
                          String withdrawalReason, LocalDateTime creationDate,
                          String studentFullName, String clubName) {
        this.membershipId = membershipId;
        this.studentId = studentId;
        this.clubId = clubId;
        this.joinDate = joinDate;
        this.membershipStatus = membershipStatus;
        this.withdrawalDate = withdrawalDate;
        this.withdrawalReason = withdrawalReason;
        this.creationDate = creationDate;
        this.studentFullName = studentFullName;
        this.clubName = clubName;
    }

    public int getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(int membershipId) {
        this.membershipId = membershipId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public MembershipStatus getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(MembershipStatus membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getWithdrawalReason() {
        return withdrawalReason;
    }

    public void setWithdrawalReason(String withdrawalReason) {
        this.withdrawalReason = withdrawalReason;
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

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    @Override
    public String toString() {
        return "ClubMembership{" + membershipId + ", student=" + studentId + ", club=" + clubId
                + ", " + (membershipStatus == null ? "?" : membershipStatus.getLabel()) + '}';
    }
}
