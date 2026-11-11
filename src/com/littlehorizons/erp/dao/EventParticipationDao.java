package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.StudentSummary;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code participantes_evento} table.
 */
public interface EventParticipationDao {

    /** Inserts a participation and returns its generated ID. */
    int save(EventParticipation participation) throws SQLException;

    EventParticipation findById(int participationId) throws SQLException;

    /** Returns every participation registered for an event. */
    List<EventParticipation> findByEvent(int eventId) throws SQLException;

    /** Returns every participation of a student across events. */
    List<EventParticipation> findByStudent(int studentId) throws SQLException;

    /** Returns every participation that has a position obtained (ranked results). */
    List<EventParticipation> findRanked() throws SQLException;

    /** True when the student is already registered for the event (unique rule). */
    boolean participationExists(int eventId, int studentId) throws SQLException;

    /** Updates role, position and observations of a participation. */
    boolean update(EventParticipation participation) throws SQLException;

    boolean delete(int participationId) throws SQLException;

    /** Helper listing of active students (ID + full name) to pick participants. */
    List<StudentSummary> listActiveStudents() throws SQLException;
}
