package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.EventDao;
import com.littlehorizons.erp.dao.EventParticipationDao;
import com.littlehorizons.erp.dao.impl.EventDaoImpl;
import com.littlehorizons.erp.dao.impl.EventParticipationDaoImpl;
import com.littlehorizons.erp.model.CompetitionPosition;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.ParticipationRole;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for event participants: registration with role/position rules,
 * participant listings and Stream-based reports.
 */
public class EventParticipationController {

    private final EventParticipationDao participationDao = new EventParticipationDaoImpl();
    private final EventDao eventDao = new EventDaoImpl();

    public void showMenu() {
        while (true) {
            ConsoleUtils.printHeader("EVENT PARTICIPANTS AND RESULTS");
            System.out.println(" 1. Register participant");
            System.out.println(" 2. List participants of an event");
            System.out.println(" 3. Update participant result");
            System.out.println(" 4. Remove participant");
            System.out.println(" 5. Participation report per student");
            System.out.println(" 6. Top ranked students");
            System.out.println(" 0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 6);
            if (option == 0) {
                return;
            }
            try {
                switch (option) {
                    case 1 -> registerParticipant();
                    case 2 -> listEventParticipants();
                    case 3 -> updateParticipantResult();
                    case 4 -> removeParticipant();
                    case 5 -> participationReportByStudent();
                    case 6 -> topRankedStudents();
                    default -> {
                    }
                }
            } catch (SQLException exception) {
                ConsoleUtils.printError("Database error: " + exception.getMessage());
                LoggerUtil.logError("EventParticipationController option " + option, exception);
            }
            ConsoleUtils.pause();
        }
    }

    private void registerParticipant() throws SQLException {
        ConsoleUtils.printHeader("REGISTER PARTICIPANT");
        Event event = pickActiveEvent();
        if (event == null) {
            return;
        }
        StudentSummary student = pickStudent();
        if (student == null) {
            return;
        }
        if (participationDao.participationExists(event.getEventId(), student.getStudentId())) {
            ConsoleUtils.printError(student.getFullName()
                    + " is already registered for this event (only one participation per event).");
            return;
        }
        EventParticipation participation = new EventParticipation();
        participation.setEventId(event.getEventId());
        participation.setStudentId(student.getStudentId());
        participation.setParticipationRole(readRole());

        // Business rule: in competitive events the position must be one of the
        // allowed values; non-competitive events store no position.
        if (event.isHasCompetition()) {
            participation.setPositionObtained(readPosition());
        }
        participation.setObservations(
                ConsoleUtils.readOptionalText("Observations (empty = none): ", null));
        int generatedId = participationDao.save(participation);
        ConsoleUtils.printSuccess(student.getFullName() + " registered in \""
                + event.getEventName() + "\" as "
                + participation.getParticipationRole().getLabel()
                + (participation.getPositionObtained() == null
                ? "" : " (" + participation.getPositionObtained() + ")")
                + " (participation " + generatedId + ").");
    }

    private void listEventParticipants() throws SQLException {
        ConsoleUtils.printHeader("EVENT PARTICIPANTS");
        Event event = pickAnyEvent();
        if (event == null) {
            return;
        }
        List<EventParticipation> participants = participationDao.findByEvent(event.getEventId());
        System.out.println("Event: " + event.getEventName()
                + (event.isHasCompetition() ? " [with competition]" : ""));
        if (participants.isEmpty()) {
            System.out.println("No participants registered for this event.");
            return;
        }
        System.out.printf("%-6s %-34.34s %-14s %-12s%n",
                "Part.", "Student", "Role", "Position");
        participants.forEach(participant -> System.out.printf("%-6d %-34.34s %-14s %-12s%n",
                participant.getParticipationId(),
                participant.getStudentFullName(),
                participant.getParticipationRole().getLabel(),
                participant.getPositionObtained() == null ? "-" : participant.getPositionObtained()));

        Map<String, Long> rolesSummary = participants.stream()
                .collect(Collectors.groupingBy(
                        participant -> participant.getParticipationRole().getLabel(),
                        LinkedHashMap::new,
                        Collectors.counting()));
        System.out.println();
        System.out.println("Summary by role: " + rolesSummary.entrySet().stream()
                .map(entry -> entry.getValue() + "x " + entry.getKey())
                .collect(Collectors.joining(", ")));
        System.out.println("Total participants: " + participants.size());
    }

    private void updateParticipantResult() throws SQLException {
        ConsoleUtils.printHeader("UPDATE PARTICIPANT RESULT");
        Event event = pickAnyEvent();
        if (event == null) {
            return;
        }
        List<EventParticipation> participants = participationDao.findByEvent(event.getEventId());
        if (participants.isEmpty()) {
            ConsoleUtils.printError("No participants registered for this event.");
            return;
        }
        System.out.println("Participants of \"" + event.getEventName() + "\":");
        participants.forEach(participant -> System.out.printf("  %4d. %s (%s)%n",
                participant.getParticipationId(), participant.getStudentFullName(),
                participant.getParticipationRole().getLabel()));

        int participationId = ConsoleUtils.readInt("Participation ID (0 = cancel): ",
                1, Integer.MAX_VALUE, 0);
        if (participationId == 0) {
            return;
        }
        EventParticipation participation = participationDao.findById(participationId);
        if (participation == null || participation.getEventId() != event.getEventId()) {
            ConsoleUtils.printError("That participation does not belong to this event.");
            return;
        }
        participation.setParticipationRole(readRole());
        if (event.isHasCompetition()) {
            participation.setPositionObtained(readPosition());
        } else {
            participation.setPositionObtained(null);
        }
        participation.setObservations(ConsoleUtils.readOptionalText(
                "Observations (empty = keep current): ", participation.getObservations()));
        participationDao.update(participation);
        ConsoleUtils.printSuccess("Participation updated for "
                + participation.getStudentFullName() + ".");
    }

    private void removeParticipant() throws SQLException {
        ConsoleUtils.printHeader("REMOVE PARTICIPANT");
        Event event = pickAnyEvent();
        if (event == null) {
            return;
        }
        List<EventParticipation> participants = participationDao.findByEvent(event.getEventId());
        if (participants.isEmpty()) {
            ConsoleUtils.printError("No participants registered for this event.");
            return;
        }
        System.out.println("Participants of \"" + event.getEventName() + "\":");
        participants.forEach(participant -> System.out.printf("  %4d. %s%n",
                participant.getParticipationId(), participant.getStudentFullName()));

        int participationId = ConsoleUtils.readInt("Participation ID (0 = cancel): ",
                1, Integer.MAX_VALUE, 0);
        if (participationId == 0) {
            return;
        }
        EventParticipation participation = participationDao.findById(participationId);
        if (participation == null || participation.getEventId() != event.getEventId()) {
            ConsoleUtils.printError("That participation does not belong to this event.");
            return;
        }
        if (!ConsoleUtils.confirm("Remove " + participation.getStudentFullName()
                + " from this event?")) {
            System.out.println("Operation cancelled.");
            return;
        }
        participationDao.delete(participationId);
        ConsoleUtils.printSuccess("Participation removed.");
    }

    /** Report: every participation of one student with position statistics. */
    private void participationReportByStudent() throws SQLException {
        ConsoleUtils.printHeader("PARTICIPATION REPORT PER STUDENT");
        StudentSummary student = pickStudent();
        if (student == null) {
            return;
        }
        List<EventParticipation> participations =
                participationDao.findByStudent(student.getStudentId());
        if (participations.isEmpty()) {
            System.out.println(student.getFullName() + " has no event participations yet.");
            return;
        }
        System.out.println("Student: " + student.getFullName());
        System.out.printf("%-34.34s %-14s %-12s%n", "Event", "Role", "Position");
        participations.forEach(participation -> System.out.printf("%-34.34s %-14s %-12s%n",
                participation.getEventName(),
                participation.getParticipationRole().getLabel(),
                participation.getPositionObtained() == null ? "-" : participation.getPositionObtained()));

        System.out.println();
        System.out.println("Total participations: " + participations.size());
        Map<String, Long> positionsSummary = participations.stream()
                .filter(participation -> participation.getPositionObtained() != null)
                .collect(Collectors.groupingBy(EventParticipation::getPositionObtained,
                        Collectors.counting()));
        if (positionsSummary.isEmpty()) {
            System.out.println("No positions obtained yet.");
        } else {
            positionsSummary.forEach((position, count) ->
                    System.out.println("  " + position + ": " + count));
        }
    }

    /** Report: students with ranked results, sorted by number of results. */
    private void topRankedStudents() throws SQLException {
        ConsoleUtils.printHeader("TOP RANKED STUDENTS");
        List<EventParticipation> ranked = participationDao.findRanked();
        if (ranked.isEmpty()) {
            System.out.println("No ranked results registered yet.");
            return;
        }

        Map<String, Long> totalsByStudent = ranked.stream()
                .collect(Collectors.groupingBy(EventParticipation::getStudentFullName,
                        Collectors.counting()));
        Map<String, Map<String, Long>> positionsByStudent = ranked.stream()
                .collect(Collectors.groupingBy(EventParticipation::getStudentFullName,
                        Collectors.groupingBy(EventParticipation::getPositionObtained,
                                Collectors.counting())));

        System.out.println("Students with positions obtained (best first):");
        totalsByStudent.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    String breakdown = positionsByStudent.get(entry.getKey()).entrySet().stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .map(position -> position.getValue() + "x " + position.getKey())
                            .collect(Collectors.joining(", "));
                    System.out.printf("  %-34.34s %2d result(s) | %s%n",
                            entry.getKey(), entry.getValue(), breakdown);
                });
    }

    /** Reads a participation role from the allowed DB ENUM values. */
    private ParticipationRole readRole() {
        List<String> roleLabels = Arrays.stream(ParticipationRole.values())
                .map(ParticipationRole::getLabel).toList();
        return ParticipationRole.fromLabel(
                ConsoleUtils.readOption("Participation role", roleLabels));
    }

    /**
     * Reads the position obtained. Only the four allowed values are accepted,
     * satisfying the competition position business rule.
     */
    private String readPosition() {
        List<String> positionLabels = Arrays.stream(CompetitionPosition.values())
                .map(CompetitionPosition::getLabel).toList();
        return CompetitionPosition.fromLabel(
                ConsoleUtils.readOption("Position obtained", positionLabels)).getLabel();
    }

    /** Lists the active events and asks the user to pick one by ID. */
    private Event pickActiveEvent() throws SQLException {
        List<Event> events = eventDao.findActive();
        if (events.isEmpty()) {
            ConsoleUtils.printError("No active events available.");
            return null;
        }
        System.out.println("Active events:");
        events.forEach(event -> System.out.printf("  %4d. %-34.34s %s %s%n",
                event.getEventId(), event.getEventName(), event.getEventDate(),
                event.isHasCompetition() ? "[competition]" : ""));
        while (true) {
            int eventId = ConsoleUtils.readInt("Event ID (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
            if (eventId == 0) {
                return null;
            }
            Event match = events.stream().filter(event -> event.getEventId() == eventId)
                    .findFirst().orElse(null);
            if (match != null) {
                return match;
            }
            ConsoleUtils.printError("That ID is not in the list.");
        }
    }

    /** Lists every event (active or not) and asks the user to pick one by ID. */
    private Event pickAnyEvent() throws SQLException {
        List<Event> events = eventDao.findAll();
        if (events.isEmpty()) {
            ConsoleUtils.printError("No events registered yet.");
            return null;
        }
        System.out.println("Events:");
        events.forEach(event -> System.out.printf("  %4d. %-34.34s %s %s%n",
                event.getEventId(), event.getEventName(), event.getEventDate(),
                event.isActive() ? "[active]" : "[inactive]"));
        while (true) {
            int eventId = ConsoleUtils.readInt("Event ID (0 = cancel): ", 1, Integer.MAX_VALUE, 0);
            if (eventId == 0) {
                return null;
            }
            Event match = events.stream().filter(event -> event.getEventId() == eventId)
                    .findFirst().orElse(null);
            if (match != null) {
                return match;
            }
            ConsoleUtils.printError("That ID is not in the list.");
        }
    }

    /** Lists the active students and asks the user to pick one by ID. */
    private StudentSummary pickStudent() throws SQLException {
        List<StudentSummary> students = participationDao.listActiveStudents();
        if (students.isEmpty()) {
            ConsoleUtils.printError("No active students available.");
            return null;
        }
        System.out.println("Active students:");
        students.forEach(student -> System.out.printf("  %4d. %s%n",
                student.getStudentId(), student.getFullName()));
        while (true) {
            int studentId = ConsoleUtils.readInt("Student ID (0 = cancel): ",
                    1, Integer.MAX_VALUE, 0);
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
