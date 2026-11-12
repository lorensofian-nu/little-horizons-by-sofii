package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.StudentSummary;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code membresias_club} table.
 */
public interface ClubMembershipDao {

    /** Inserts a membership and returns its generated ID. */
    int save(ClubMembership membership) throws SQLException;

    /** Inserts only when the club remains under capacity inside one transaction. */
    int saveIfCapacityAvailable(ClubMembership membership) throws SQLException;

    ClubMembership findById(int membershipId) throws SQLException;

    /** Returns every membership of a club, active and withdrawn. */
    List<ClubMembership> findByClub(int clubId) throws SQLException;

    /** Returns only the active memberships of a club. */
    List<ClubMembership> findActiveMembersByClub(int clubId) throws SQLException;

    /** True when the student already has an active membership in the club. */
    boolean activeMembershipExists(int studentId, int clubId) throws SQLException;

    /** Counts the active memberships of a club (used against its max capacity). */
    int countActiveMembers(int clubId) throws SQLException;

    /** Updates a membership (mainly used to record a withdrawal). */
    boolean update(ClubMembership membership) throws SQLException;

    boolean delete(int membershipId) throws SQLException;

    /** Helper listing of active students (ID + full name) to enroll members. */
    List<StudentSummary> listActiveStudents() throws SQLException;
}
