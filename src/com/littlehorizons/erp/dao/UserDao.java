package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AuthenticatedUser;

import java.sql.SQLException;

/** Authentication persistence contract. */
public interface UserDao {

    AuthenticatedUser findActiveUser(String username) throws SQLException;

    String findPasswordHash(String username) throws SQLException;

    void updateLastAccess(String username) throws SQLException;

    void updatePassword(String username, String passwordHash) throws SQLException;

    int countUsers() throws SQLException;

    void createUser(String username, String passwordHash, String role) throws SQLException;

    /**
     * Returns {@code true} while the account is temporarily locked by
     * repeated failed logins.
     */
    boolean isAccountLocked(String username) throws SQLException;

    /**
     * Records a failed login for an existing account. When the configured
     * threshold is reached the account is locked for the configured number
     * of minutes. Returns the new failed-attempt count (0 for unknown users).
     */
    int registerFailedLogin(String username, int lockThreshold, int lockMinutes)
            throws SQLException;

    /** Clears the failed-attempt counter after a successful login. */
    void resetFailedLogins(String username) throws SQLException;
}
