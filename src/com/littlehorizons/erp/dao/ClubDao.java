package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.TeacherSummary;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code clubes} table.
 */
public interface ClubDao {

    /** Inserts a club and returns its generated ID. */
    int save(Club club) throws SQLException;

    Club findById(int clubId) throws SQLException;

    /** Returns every club (active and inactive) ordered by name. */
    List<Club> findAll() throws SQLException;

    /** Returns only the active clubs ordered by name. */
    List<Club> findActive() throws SQLException;

    /** Updates the editable fields of a club. */
    boolean update(Club club) throws SQLException;

    /** Deletes a club physically (memberships cascade). */
    boolean delete(int clubId) throws SQLException;

    /** Checks whether a club name is already taken, ignoring the given ID (0 = none). */
    boolean clubNameExists(String clubName, int excludeClubId) throws SQLException;

    /** Helper listing of active teachers (ID + full name) to pick supervisors. */
    List<TeacherSummary> listActiveTeachers() throws SQLException;
}
