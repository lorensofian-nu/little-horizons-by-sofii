package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.dao.impl.ClubDaoImpl;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.TeacherSummary;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the clubs catalog: CRUD plus activation and deletion.
 */
public class ClubController {

    private final ClubDao clubDao = new ClubDaoImpl();

    public void showMenu() {
        while (true) {
            ConsoleUtils.printHeader("CLUBS MANAGEMENT");
            System.out.println(" 1. Create club");
            System.out.println(" 2. List clubs");
            System.out.println(" 3. Search club by ID");
            System.out.println(" 4. Update club");
            System.out.println(" 5. Activate / deactivate club");
            System.out.println(" 6. Delete club");
            System.out.println(" 0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 6);
            if (option == 0) {
                return;
            }
            try {
                switch (option) {
                    case 1 -> createClub();
                    case 2 -> listClubs();
                    case 3 -> findClub();
                    case 4 -> updateClub();
                    case 5 -> toggleClubActive();
                    case 6 -> deleteClub();
                    default -> {
                    }
                }
            } catch (SQLException exception) {
                ConsoleUtils.printError("Database error: " + exception.getMessage());
                LoggerUtil.logError("ClubController option " + option, exception);
            }
            ConsoleUtils.pause();
        }
    }

    private void createClub() throws SQLException {
        ConsoleUtils.printHeader("CREATE CLUB");
        Club club = new Club();
        club.setClubName(ConsoleUtils.readRequiredText("Club name: "));
        if (clubDao.clubNameExists(club.getClubName(), 0)) {
            ConsoleUtils.printError("A club with that name already exists.");
            return;
        }
        club.setDescription(ConsoleUtils.readOptionalText("Description (empty = none): ", null));
        club.setSupervisingTeacherId(pickTeacherId("(0 = no teacher)"));
        int capacity = ConsoleUtils.readInt("Maximum capacity", 1, 500, 0);
        club.setMaxCapacity(capacity == 0 ? 30 : capacity);
        club.setMeetingSchedule(ConsoleUtils.readOptionalText("Meeting schedule (empty = none): ", null));
        club.setActive(ConsoleUtils.confirm("Is the club active from the start?"));
        int generatedId = clubDao.save(club);
        ConsoleUtils.printSuccess("Club created with ID " + generatedId + ".");
    }

    private void listClubs() throws SQLException {
        ConsoleUtils.printHeader("CLUBS CATALOG");
        List<Club> clubs = clubDao.findAll();
        if (clubs.isEmpty()) {
            System.out.println("No clubs registered yet.");
            return;
        }
        System.out.printf("%-4s %-30.30s %-22.22s %-9s %-9s%n",
                "ID", "Name", "Teacher", "Capacity", "Active");
        clubs.forEach(club -> System.out.printf("%-4d %-30.30s %-22.22s %-9s %-9s%n",
                club.getClubId(),
                club.getClubName(),
                club.getSupervisingTeacherName() == null ? "-" : club.getSupervisingTeacherName(),
                club.getMaxCapacity() == null ? 30 : club.getMaxCapacity(),
                club.isActive() ? "Yes" : "No"));
        System.out.println("Total clubs: " + clubs.size());
    }

    private void findClub() throws SQLException {
        ConsoleUtils.printHeader("SEARCH CLUB");
        int clubId = ConsoleUtils.readInt("Club ID", 1, Integer.MAX_VALUE, 0);
        if (clubId == 0) {
            return;
        }
        Club club = clubDao.findById(clubId);
        if (club == null) {
            ConsoleUtils.printError("No club found with ID " + clubId + ".");
            return;
        }
        System.out.println("  ID:          " + club.getClubId());
        System.out.println("  Name:        " + club.getClubName());
        System.out.println("  Description: " + (club.getDescription() == null ? "-" : club.getDescription()));
        System.out.println("  Teacher:     " + (club.getSupervisingTeacherName() == null
                ? "-" : club.getSupervisingTeacherName()));
        System.out.println("  Capacity:    " + (club.getMaxCapacity() == null ? 30 : club.getMaxCapacity()));
        System.out.println("  Schedule:    " + (club.getMeetingSchedule() == null ? "-" : club.getMeetingSchedule()));
        System.out.println("  Active:      " + (club.isActive() ? "Yes" : "No"));
    }

    private void updateClub() throws SQLException {
        ConsoleUtils.printHeader("UPDATE CLUB");
        int clubId = ConsoleUtils.readInt("Club ID", 1, Integer.MAX_VALUE, 0);
        if (clubId == 0) {
            return;
        }
        Club club = clubDao.findById(clubId);
        if (club == null) {
            ConsoleUtils.printError("No club found with ID " + clubId + ".");
            return;
        }
        String name = ConsoleUtils.readOptionalText(
                "Club name (empty = keep \"" + club.getClubName() + "\"): ", club.getClubName());
        if (!name.equals(club.getClubName()) && clubDao.clubNameExists(name, clubId)) {
            ConsoleUtils.printError("A club with that name already exists.");
            return;
        }
        club.setClubName(name);
        club.setDescription(ConsoleUtils.readOptionalText(
                "Description (empty = keep current): ", club.getDescription()));
        Integer teacherId = pickTeacherId("(0 = keep current)");
        if (teacherId != null) {
            club.setSupervisingTeacherId(teacherId);
        }
        int capacity = ConsoleUtils.readInt("Maximum capacity", 1, 500,
                club.getMaxCapacity() == null ? 30 : club.getMaxCapacity());
        club.setMaxCapacity(capacity);
        club.setMeetingSchedule(ConsoleUtils.readOptionalText(
                "Meeting schedule (empty = keep current): ", club.getMeetingSchedule()));
        clubDao.update(club);
        ConsoleUtils.printSuccess("Club updated.");
    }

    private void toggleClubActive() throws SQLException {
        ConsoleUtils.printHeader("ACTIVATE / DEACTIVATE CLUB");
        int clubId = ConsoleUtils.readInt("Club ID", 1, Integer.MAX_VALUE, 0);
        if (clubId == 0) {
            return;
        }
        Club club = clubDao.findById(clubId);
        if (club == null) {
            ConsoleUtils.printError("No club found with ID " + clubId + ".");
            return;
        }
        club.setActive(!club.isActive());
        clubDao.update(club);
        ConsoleUtils.printSuccess("Club \"" + club.getClubName() + "\" is now "
                + (club.isActive() ? "active" : "inactive") + ".");
    }

    private void deleteClub() throws SQLException {
        ConsoleUtils.printHeader("DELETE CLUB");
        int clubId = ConsoleUtils.readInt("Club ID", 1, Integer.MAX_VALUE, 0);
        if (clubId == 0) {
            return;
        }
        Club club = clubDao.findById(clubId);
        if (club == null) {
            ConsoleUtils.printError("No club found with ID " + clubId + ".");
            return;
        }
        System.out.println("Deleting \"" + club.getClubName()
                + "\" also removes all its memberships.");
        if (!ConsoleUtils.confirm("Are you sure you want to delete this club?")) {
            System.out.println("Operation cancelled.");
            return;
        }
        clubDao.delete(clubId);
        ConsoleUtils.printSuccess("Club deleted.");
    }

    /**
     * Shows the active teachers and asks for one by ID.
     *
     * @param zeroHint hint shown for the value 0 ("no teacher" or "keep current")
     * @return the chosen teacher ID, or null when the user picks 0
     */
    private Integer pickTeacherId(String zeroHint) throws SQLException {
        List<TeacherSummary> teachers = clubDao.listActiveTeachers();
        if (teachers.isEmpty()) {
            ConsoleUtils.printError("No active teachers available.");
            return null;
        }
        System.out.println("Available teachers:");
        teachers.forEach(teacher -> System.out.printf("  %4d. %s%n",
                teacher.getTeacherId(), teacher.getFullName()));
        while (true) {
            int teacherId = ConsoleUtils.readInt("Teacher ID " + zeroHint + ": ",
                    1, Integer.MAX_VALUE, 0);
            if (teacherId == 0) {
                return null;
            }
            boolean valid = teachers.stream().anyMatch(t -> t.getTeacherId() == teacherId);
            if (valid) {
                return teacherId;
            }
            ConsoleUtils.printError("That ID is not in the list.");
        }
    }
}
