package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.TeacherSummary;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code eventos} table.
 */
public interface EventDao {

    /** Inserts an event and returns its generated ID. */
    int save(Event event) throws SQLException;

    Event findById(int eventId) throws SQLException;

    /** Returns every event (active and inactive) ordered by date. */
    List<Event> findAll() throws SQLException;

    /** Returns only the active events ordered by date. */
    List<Event> findActive() throws SQLException;

    /** Updates the editable fields of an event. */
    boolean update(Event event) throws SQLException;

    /** Deletes an event physically (participations cascade). */
    boolean delete(int eventId) throws SQLException;

    /** Checks whether an event name is already taken, ignoring the given ID (0 = none). */
    boolean eventNameExists(String eventName, int excludeEventId) throws SQLException;

    /** Helper listing of active teachers (ID + full name) to pick organizers. */
    List<TeacherSummary> listActiveTeachers() throws SQLException;
}
