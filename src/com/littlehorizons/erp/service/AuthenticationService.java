package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.UserDao;
import com.littlehorizons.erp.dao.impl.UserDaoImpl;
import com.littlehorizons.erp.model.AuthenticatedUser;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.PasswordHasher;
import com.littlehorizons.erp.util.InitialAdminConfiguration;
import com.littlehorizons.erp.validation.PasswordPolicy;

import java.sql.SQLException;
import java.util.Optional;

/** Authenticates users and records successful access without logging secrets. */
public class AuthenticationService {

    /** Failed attempts that trigger a temporary account lock. */
    public static final int LOCKOUT_THRESHOLD = 5;
    /** Duration of the temporary lock, in minutes. */
    public static final int LOCKOUT_MINUTES = 15;

    /**
     * Decoy hash verified against when the username does not exist, so the
     * response time of unknown users matches that of real users and cannot
     * be used to enumerate accounts.
     */
    private static final String DECOY_HASH = PasswordHasher.hash(
            "decoy-password-never-matches".toCharArray());

    private final UserDao userDao;

    public AuthenticationService() {
        this(new UserDaoImpl());
    }

    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public AuthenticatedUser authenticate(String username, char[] password) throws SQLException {
        if (userDao.isAccountLocked(username)) {
            LoggerUtil.logInfo("Rejected login attempt (account temporarily locked)");
            return null;
        }
        String hash = userDao.findPasswordHash(username);
        // Verify against a decoy hash for unknown users so the timing does
        // not reveal whether the account exists.
        boolean verified = PasswordHasher.verify(password, hash != null ? hash : DECOY_HASH);
        if (hash == null || !verified) {
            userDao.registerFailedLogin(username, LOCKOUT_THRESHOLD, LOCKOUT_MINUTES);
            LoggerUtil.logInfo("Rejected login attempt");
            return null;
        }
        AuthenticatedUser user = userDao.findActiveUser(username);
        if (user != null) {
            userDao.resetFailedLogins(username);
            userDao.updateLastAccess(username);
            LoggerUtil.logInfo("Successful login");
        }
        return user;
    }

    /**
     * Returns {@code true} while the account is temporarily locked after
     * repeated failed logins; the caller can explain the state to the user.
     */
    public boolean isAccountLocked(String username) throws SQLException {
        return userDao.isAccountLocked(username);
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
        Optional<String> violation = PasswordPolicy.violation(password, username);
        if (violation.isPresent()) {
            LoggerUtil.logInfo("Initial administrator bootstrap rejected: weak password");
            System.out.println("  [!] The initial administrator password was rejected: "
                    + violation.get() + ".");
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
