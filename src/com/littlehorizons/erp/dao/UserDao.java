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
}
