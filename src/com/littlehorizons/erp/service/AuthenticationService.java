package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.UserDao;
import com.littlehorizons.erp.dao.impl.UserDaoImpl;
import com.littlehorizons.erp.model.AuthenticatedUser;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.PasswordHasher;

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
     * Creates the first administrator only when the database is empty and an
     * administrator password was supplied out of band through the environment.
     * No bootstrap credential is stored in the repository or printed to logs.
     */
    public boolean bootstrapInitialAdministrator() throws SQLException {
        if (userDao.countUsers() > 0) {
            return false;
        }
        String configuredPassword = System.getenv("ERP_INITIAL_ADMIN_PASSWORD");
        if (configuredPassword == null || configuredPassword.length() < 12) {
            return false;
        }
        String username = System.getenv().getOrDefault("ERP_INITIAL_ADMIN_USERNAME", "admin");
        if (username.isBlank() || username.length() > 80) {
            return false;
        }
        char[] password = configuredPassword.toCharArray();
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
