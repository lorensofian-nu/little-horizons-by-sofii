package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.dao.impl.ClubDaoImpl;
import com.littlehorizons.erp.dao.impl.ClubMembershipDaoImpl;
import com.littlehorizons.erp.dao.ClubMembershipDao;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.MembershipStatus;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for club memberships: enrollment with capacity validation,
 * member listing and withdrawal.
 */
public class ClubMembershipController {

    private final ClubMembershipDao membershipDao = new ClubMembershipDaoImpl();
    private final ClubDao clubDao = new ClubDaoImpl();

    public void showMenu() {
        while (true) {
            ConsoleUtils.printHeader("CLUB MEMBERSHIPS");
            System.out.println(" 1. Enroll student in a club");
            System.out.println(" 2. List members of a club");
            System.out.println(" 3. Withdraw member");
            System.out.println(" 0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 3);
            if (option == 0) {
                return;
            }
            try {
                switch (option) {
                    case 1 -> enrollStudent();
                    case 2 -> listClubMembers();
                    case 3 -> withdrawMember();
                    default -> {
                    }
                }
            } catch (SQLException exception) {
                ConsoleUtils.printError("Database error: " + exception.getMessage());
                LoggerUtil.logError("ClubMembershipController option " + option, exception);
            }
            ConsoleUtils.pause();
        }
    }

    private void enrollStudent() throws SQLException {
        ConsoleUtils.printHeader("ENROLL STUDENT IN CLUB");
        Club club = pickActiveClub();
        if (club == null) {
            return;
        }
        int capacity = club.getMaxCapacity() == null ? 30 : club.getMaxCapacity();
        int activeMembers = membershipDao.countActiveMembers(club.getClubId());
        System.out.println("Current occupancy: " + activeMembers + "/" + capacity);

        StudentSummary student = pickStudent("Student ID (0 = cancel): ");
        if (student == null) {
            return;
        }
        // Business rule: never exceed the club capacity (checked BEFORE saving).
        if (activeMembers >= capacity) {
            ConsoleUtils.printError("The club \"" + club.getClubName() + "\" is full ("
                    + activeMembers + "/" + capacity + " members). Enrollment rejected.");
            return;
        }
        if (membershipDao.activeMembershipExists(student.getStudentId(), club.getClubId())) {
            ConsoleUtils.printError(student.getFullName() + " is already an active member of this club.");
            return;
        }
        LocalDate joinDate = ConsoleUtils.readOptionalDate("Join date", LocalDate.now());
        ClubMembership membership = new ClubMembership();
        membership.setStudentId(student.getStudentId());
        membership.setClubId(club.getClubId());
        membership.setJoinDate(joinDate);
        membership.setMembershipStatus(MembershipStatus.ACTIVE);
        int generatedId = membershipDao.save(membership);
        ConsoleUtils.printSuccess(student.getFullName() + " enrolled in \""
                + club.getClubName() + "\" (membership " + generatedId + "). Occupancy now "
                + (activeMembers + 1) + "/" + capacity + ".");
    }

    private void listClubMembers() throws SQLException {
        ConsoleUtils.printHeader("CLUB MEMBERS");
        Club club = pickAnyClub();
        if (club == null) {
            return;
        }
        List<ClubMembership> activeMembers = membershipDao.findActiveMembersByClub(club.getClubId());
        int capacity = club.getMaxCapacity() == null ? 30 : club.getMaxCapacity();
        System.out.println("Club: " + club.getClubName());
        System.out.println("Occupancy: " + activeMembers.size() + "/" + capacity
                + " (" + Math.max(capacity - activeMembers.size(), 0) + " positions left)");
        if (activeMembers.isEmpty()) {
            System.out.println("No active members in this club.");
        } else {
            System.out.printf("%-8s %-34.34s %-12s %-10s%n", "Member", "Student", "Joined", "Status");
            activeMembers.forEach(member -> System.out.printf("%-8d %-34.34s %-12s %-10s%n",
                    member.getMembershipId(),
                    member.getStudentFullName(),
                    member.getJoinDate(),
                    member.getMembershipStatus().getLabel()));
        }
        List<ClubMembership> withdrawn = membershipDao.findByClub(club.getClubId()).stream()
                .filter(member -> member.getMembershipStatus() == MembershipStatus.WITHDRAWN)
                .toList();
        if (!withdrawn.isEmpty()) {
            System.out.println();
            System.out.println("Withdrawn members: " + withdrawn.size());
            withdrawn.forEach(member -> System.out.printf("  %d. %s (left on %s)%n",
                    member.getMembershipId(), member.getStudentFullName(), member.getWithdrawalDate()));
        }
    }

    private void withdrawMember() throws SQLException {
        ConsoleUtils.printHeader("WITHDRAW MEMBER");
        Club club = pickAnyClub();
        if (club == null) {
            return;
        }
        List<ClubMembership> activeMembers = membershipDao.findActiveMembersByClub(club.getClubId());
        if (activeMembers.isEmpty()) {
            ConsoleUtils.printError("This club has no active members.");
            return;
        }
        System.out.println("Active members of \"" + club.getClubName() + "\":");
        activeMembers.forEach(member -> System.out.printf("  %4d. %s%n",
                member.getMembershipId(), member.getStudentFullName()));

        int membershipId = ConsoleUtils.readInt("Membership ID (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
        if (membershipId == 0) {
            return;
        }
        ClubMembership membership = membershipDao.findById(membershipId);
        if (membership == null || membership.getClubId() != club.getClubId()
                || membership.getMembershipStatus() != MembershipStatus.ACTIVE) {
            ConsoleUtils.printError("That membership is not an active member of this club.");
            return;
        }
        LocalDate withdrawalDate = ConsoleUtils.readOptionalDate("Withdrawal date", LocalDate.now());
        String reason = ConsoleUtils.readRequiredText("Withdrawal reason: ");
        membership.setMembershipStatus(MembershipStatus.WITHDRAWN);
        membership.setWithdrawalDate(withdrawalDate);
        membership.setWithdrawalReason(reason);
        membershipDao.update(membership);
        ConsoleUtils.printSuccess(membership.getStudentFullName() + " withdrawn from \""
                + club.getClubName() + "\".");
    }

    /** Lists the active clubs and asks the user to pick one by ID. */
    private Club pickActiveClub() throws SQLException {
        List<Club> clubs = clubDao.findActive();
        if (clubs.isEmpty()) {
            ConsoleUtils.printError("No active clubs available.");
            return null;
        }
        System.out.println("Active clubs:");
        Map<Integer, Integer> occupancy = new java.util.HashMap<>();
        for (Club item : clubs) {
            occupancy.put(item.getClubId(), membershipDao.countActiveMembers(item.getClubId()));
        }
        clubs.forEach(club -> System.out.printf("  %4d. %-30.30s (%d/%d members)%n",
                club.getClubId(), club.getClubName(),
                occupancy.getOrDefault(club.getClubId(), 0),
                club.getMaxCapacity() == null ? 30 : club.getMaxCapacity()));
        while (true) {
            int clubId = ConsoleUtils.readInt("Club ID (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
            if (clubId == 0) {
                return null;
            }
            Club match = clubs.stream().filter(club -> club.getClubId() == clubId)
                    .findFirst().orElse(null);
            if (match != null) {
                return match;
            }
            ConsoleUtils.printError("That ID is not in the list.");
        }
    }

    /** Lists every club (active or not) and asks the user to pick one by ID. */
    private Club pickAnyClub() throws SQLException {
        List<Club> clubs = clubDao.findAll();
        if (clubs.isEmpty()) {
            ConsoleUtils.printError("No clubs registered yet.");
            return null;
        }
        System.out.println("Clubs:");
        clubs.forEach(club -> System.out.printf("  %4d. %-30.30s %s%n",
                club.getClubId(), club.getClubName(), club.isActive() ? "[active]" : "[inactive]"));
        while (true) {
            int clubId = ConsoleUtils.readInt("Club ID (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
            if (clubId == 0) {
                return null;
            }
            Club match = clubs.stream().filter(club -> club.getClubId() == clubId)
                    .findFirst().orElse(null);
            if (match != null) {
                return match;
            }
            ConsoleUtils.printError("That ID is not in the list.");
        }
    }

    /** Lists the active students and asks the user to pick one by ID. */
    private StudentSummary pickStudent(String prompt) throws SQLException {
        List<StudentSummary> students = membershipDao.listActiveStudents();
        if (students.isEmpty()) {
            ConsoleUtils.printError("No active students available.");
            return null;
        }
        System.out.println("Active students:");
        students.forEach(student -> System.out.printf("  %4d. %s%n",
                student.getStudentId(), student.getFullName()));
        while (true) {
            int studentId = ConsoleUtils.readInt(prompt, 1, Integer.MAX_VALUE, 0);
            if (studentId == 0) {
                return null;
            }
            StudentSummary match = students.stream()
                    .filter(student -> student.getStudentId() == studentId)
                    .findFirst().orElse(null);
            if (match != null) {
                return match;
            }
            ConsoleUtils.printError("That ID is not in the list.");
        }
    }
}
