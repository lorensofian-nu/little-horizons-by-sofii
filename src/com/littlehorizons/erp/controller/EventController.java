package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.EventDao;
import com.littlehorizons.erp.dao.impl.EventDaoImpl;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.EventType;
import com.littlehorizons.erp.model.TeacherSummary;
import com.littlehorizons.erp.util.AppConstants;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the events catalog: CRUD plus activation and deletion.
 */
public class EventController {

    private final EventDao eventDao = new EventDaoImpl();

    public void showMenu() {
        while (true) {
            ConsoleUtils.printHeader("EVENTS MANAGEMENT");
            System.out.println(" 1. Create event");
            System.out.println(" 2. List events");
            System.out.println(" 3. Search event by ID");
            System.out.println(" 4. Update event");
            System.out.println(" 5. Activate / deactivate event");
            System.out.println(" 6. Delete event");
            System.out.println(" 0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 6);
            if (option == 0) {
                return;
            }
            try {
                switch (option) {
                    case 1 -> createEvent();
                    case 2 -> listEvents();
                    case 3 -> findEvent();
                    case 4 -> updateEvent();
                    case 5 -> toggleEventActive();
                    case 6 -> deleteEvent();
                    default -> {
                    }
                }
            } catch (SQLException exception) {
                ConsoleUtils.printError("Database error: " + exception.getMessage());
                LoggerUtil.logError("EventController option " + option, exception);
            }
            ConsoleUtils.pause();
        }
    }

    private void createEvent() throws SQLException {
        ConsoleUtils.printHeader("CREATE EVENT");
        Event event = new Event();
        event.setEventName(ConsoleUtils.readRequiredText("Event name: "));
        if (eventDao.eventNameExists(event.getEventName(), 0)) {
            ConsoleUtils.printError("An event with that name already exists.");
            return;
        }
        event.setDescription(ConsoleUtils.readOptionalText("Description (empty = none): ", null));
        List<String> typeLabels = Arrays.stream(EventType.values())
                .map(EventType::getLabel).toList();
        event.setEventType(EventType.fromLabel(
                ConsoleUtils.readOption("Event type", typeLabels)));
        event.setEventDate(readEventDate("Event date"));
        event.setPlace(ConsoleUtils.readOptionalText("Place (empty = none): ", null));
        event.setHasCompetition(ConsoleUtils.confirm("Does the event include a competition?"));
        event.setOrganizerId(pickOrganizerId("(0 = no organizer)"));
        event.setActive(ConsoleUtils.confirm("Is the event active from the start?"));
        int generatedId = eventDao.save(event);
        ConsoleUtils.printSuccess("Event created with ID " + generatedId + ".");
    }

    private void listEvents() throws SQLException {
        ConsoleUtils.printHeader("EVENTS CATALOG");
        List<Event> events = eventDao.findAll();
        if (events.isEmpty()) {
            System.out.println("No events registered yet.");
            return;
        }
        System.out.printf("%-4s %-34.34s %-12s %-11s %-14.14s %-11s %-7s%n",
                "ID", "Name", "Date", "Type", "Place", "Competition", "Active");
        events.forEach(event -> System.out.printf("%-4d %-34.34s %-12s %-11s %-14.14s %-11s %-7s%n",
                event.getEventId(),
                event.getEventName(),
                event.getEventDate(),
                event.getEventType().getLabel(),
                event.getPlace() == null ? "-" : event.getPlace(),
                event.isHasCompetition() ? "Yes" : "No",
                event.isActive() ? "Yes" : "No"));
        System.out.println("Total events: " + events.size());
    }

    private void findEvent() throws SQLException {
        ConsoleUtils.printHeader("SEARCH EVENT");
        int eventId = ConsoleUtils.readInt("Event ID", 1, Integer.MAX_VALUE, 0);
        if (eventId == 0) {
            return;
        }
        Event event = eventDao.findById(eventId);
        if (event == null) {
            ConsoleUtils.printError("No event found with ID " + eventId + ".");
            return;
        }
        System.out.println("  ID:           " + event.getEventId());
        System.out.println("  Name:         " + event.getEventName());
        System.out.println("  Description:  " + (event.getDescription() == null ? "-" : event.getDescription()));
        System.out.println("  Type:         " + event.getEventType().getLabel());
        System.out.println("  Date:         " + event.getEventDate());
        System.out.println("  Place:        " + (event.getPlace() == null ? "-" : event.getPlace()));
        System.out.println("  Competition:  " + (event.isHasCompetition() ? "Yes" : "No"));
        System.out.println("  Organizer:    " + (event.getOrganizerName() == null ? "-" : event.getOrganizerName()));
        System.out.println("  Active:       " + (event.isActive() ? "Yes" : "No"));
    }

    private void updateEvent() throws SQLException {
        ConsoleUtils.printHeader("UPDATE EVENT");
        int eventId = ConsoleUtils.readInt("Event ID", 1, Integer.MAX_VALUE, 0);
        if (eventId == 0) {
            return;
        }
        Event event = eventDao.findById(eventId);
        if (event == null) {
            ConsoleUtils.printError("No event found with ID " + eventId + ".");
            return;
        }
        String name = ConsoleUtils.readOptionalText(
                "Event name (empty = keep \"" + event.getEventName() + "\"): ", event.getEventName());
        if (!name.equals(event.getEventName()) && eventDao.eventNameExists(name, eventId)) {
            ConsoleUtils.printError("An event with that name already exists.");
            return;
        }
        event.setEventName(name);
        event.setDescription(ConsoleUtils.readOptionalText(
                "Description (empty = keep current): ", event.getDescription()));
        List<String> typeLabels = Arrays.stream(EventType.values())
                .map(EventType::getLabel).toList();
        String typeLabel = ConsoleUtils.readOption("Event type", typeLabels);
        event.setEventType(EventType.fromLabel(typeLabel));
        event.setEventDate(readEventDate("Event date"));
        event.setPlace(ConsoleUtils.readOptionalText(
                "Place (empty = keep current): ", event.getPlace()));
        event.setHasCompetition(ConsoleUtils.confirm("Does the event include a competition?"));
        Integer organizerId = pickOrganizerId("(0 = keep current)");
        if (organizerId != null) {
            event.setOrganizerId(organizerId);
        }
        eventDao.update(event);
        ConsoleUtils.printSuccess("Event updated.");
    }

    private void toggleEventActive() throws SQLException {
        ConsoleUtils.printHeader("ACTIVATE / DEACTIVATE EVENT");
        int eventId = ConsoleUtils.readInt("Event ID", 1, Integer.MAX_VALUE, 0);
        if (eventId == 0) {
            return;
        }
        Event event = eventDao.findById(eventId);
        if (event == null) {
            ConsoleUtils.printError("No event found with ID " + eventId + ".");
            return;
        }
        event.setActive(!event.isActive());
        eventDao.update(event);
        ConsoleUtils.printSuccess("Event \"" + event.getEventName() + "\" is now "
                + (event.isActive() ? "active" : "inactive") + ".");
    }

    private void deleteEvent() throws SQLException {
        ConsoleUtils.printHeader("DELETE EVENT");
        int eventId = ConsoleUtils.readInt("Event ID", 1, Integer.MAX_VALUE, 0);
        if (eventId == 0) {
            return;
        }
        Event event = eventDao.findById(eventId);
        if (event == null) {
            ConsoleUtils.printError("No event found with ID " + eventId + ".");
            return;
        }
        System.out.println("Deleting \"" + event.getEventName()
                + "\" also removes all its participant records.");
        if (!ConsoleUtils.confirm("Are you sure you want to delete this event?")) {
            System.out.println("Operation cancelled.");
            return;
        }
        eventDao.delete(eventId);
        ConsoleUtils.printSuccess("Event deleted.");
    }

    /** Reads an event date and validates it against the academic year limits. */
    private LocalDate readEventDate(String prompt) {
        while (true) {
            LocalDate date = ConsoleUtils.readDate(prompt);
            int year = date.getYear();
            if (year >= AppConstants.MIN_YEAR && year <= AppConstants.MAX_YEAR) {
                return date;
            }
            System.out.println("  [!] The year must be between "
                    + AppConstants.MIN_YEAR + " and " + AppConstants.MAX_YEAR + ".");
        }
    }

    /**
     * Shows the active teachers and asks for one by ID.
     *
     * @param zeroHint hint shown for the value 0 ("no organizer" or "keep current")
     * @return the chosen teacher ID, or null when the user picks 0
     */
    private Integer pickOrganizerId(String zeroHint) throws SQLException {
        List<TeacherSummary> teachers = eventDao.listActiveTeachers();
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
