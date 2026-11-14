package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.UserDao;
import com.littlehorizons.erp.dao.impl.UserDaoImpl;
import com.littlehorizons.erp.model.AuthenticatedUser;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.PasswordHasher;
import com.littlehorizons.erp.util.InitialAdminConfiguration;

import java.sql.SQLException;

/** Authenticates users and records successful access without logging secrets. */
public class AuthenticationService {

    private final UserDao userDao;

    public AuthenticationService() {
        this(new UserDaoImpl());
    }

    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public AuthenticatedUser authenticate(String username, char[] password) throws SQLException {
        String hash = userDao.findPasswordHash(username);
        if (hash == null || !PasswordHasher.verify(password, hash)) {
            LoggerUtil.logInfo("Rejected login attempt");
            return null;
        }
        AuthenticatedUser user = userDao.findActiveUser(username);
        if (user != null) {
            userDao.updateLastAccess(username);
            LoggerUtil.logInfo("Successful login");
        }
        return user;
    }

    public void changePassword(String username, char[] newPassword) throws SQLException {
        userDao.updatePassword(username, PasswordHasher.hash(newPassword));
    }

    /**
     * Creates the first administrator only when the database is empty and a
     * valid bootstrap password is configured through the environment or the
     * local, untracked db.properties file.
     */
    public boolean bootstrapInitialAdministrator() throws SQLException {
        if (userDao.countUsers() > 0) {
            return false;
        }
        char[] password = InitialAdminConfiguration.password();
        if (password.length < 12) {
            java.util.Arrays.fill(password, '\0');
            return false;
        }
        String username = InitialAdminConfiguration.username();
        if (username.isBlank() || username.length() > 80) {
            java.util.Arrays.fill(password, '\0');
            return false;
        }
        try {
            userDao.createUser(username, PasswordHasher.hash(password), "Administrador");
            return true;
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    public boolean hasUsers() throws SQLException {
        return userDao.countUsers() > 0;
    }
}
