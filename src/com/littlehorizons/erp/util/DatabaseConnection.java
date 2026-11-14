package com.littlehorizons.erp.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton in charge of creating JDBC connections to the MySQL database.
 *
 * <p>Connection settings are read once from the local {@code db.properties} file
 * or from {@code ERP_DB_*} environment variables. Environment variables have
 * priority, so deployment secrets can remain outside the repository. Missing
 * required settings fail fast with a clear error; no default credentials are
 * ever assumed. Callers obtain a bounded pooled
 * {@link Connection} on each request and must return it using try-with-resources.</p>
 */
public final class DatabaseConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static final String ENV_PREFIX = "ERP_DB_";
    private static final String JDBC_URL_TEMPLATE =
            "jdbc:mysql://%s:%s/%s?useSSL=%s&verifyServerCertificate=%s"
            + "&allowPublicKeyRetrieval=%s&serverTimezone=America/Bogota"
            + "&characterEncoding=UTF-8";

    private static volatile DatabaseConnection instance;

    private final String jdbcUrl;
    private final String user;
    private final String password;
    private final ConnectionPool connectionPool;

    private DatabaseConnection() {
        Properties properties = loadProperties();
        String host = requireProperty(properties, "db.host");
        String port = requireProperty(properties, "db.port");
        String database = requireProperty(properties, "db.name");
        this.user = requireProperty(properties, "db.user");
        this.password = requireProperty(properties, "db.password");
        boolean ssl = booleanProperty(properties, "db.ssl", true);
        boolean verifyCertificate = booleanProperty(properties, "db.verifyServerCertificate", ssl);
        boolean allowPublicKeyRetrieval = booleanProperty(
                properties, "db.allowPublicKeyRetrieval", false);
        this.jdbcUrl = String.format(JDBC_URL_TEMPLATE, host, port, database,
                ssl, verifyCertificate, allowPublicKeyRetrieval);
        int poolSize = integerProperty(properties, "db.poolSize", 10, 1, 50);
        this.connectionPool = new ConnectionPool(
                () -> DriverManager.getConnection(jdbcUrl, user, password), poolSize);
    }

    /**
     * Reads a required configuration key; fails fast with a clear message
     * instead of silently falling back to insecure default credentials.
     */
    private String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "The '" + key + "' setting is missing or empty in db.properties. "
                    + "Check the db.properties file at the project root and set "
                    + "db.host, db.port, db.name and db.user.");
        }
        return value;
    }

    private boolean booleanProperty(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    private int integerProperty(Properties properties, String key, int defaultValue,
                                int min, int max) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed >= min && parsed <= max) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // Fall through to the configuration error below.
        }
        throw new IllegalStateException(
                "The '" + key + "' setting must be between " + min + " and " + max + ".");
    }

    /** Returns the singleton instance using double-checked locking. */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /** Opens a new connection to the database. The caller must close it. */
    public Connection getConnection() throws SQLException {
        return connectionPool.borrow();
    }

    /** Closes idle pooled connections during orderly application shutdown. */
    public void closePool() {
        connectionPool.closeAll();
    }

    /** Utility method used by Main to fail fast with a clear message. */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException exception) {
            LoggerUtil.logError("Database connection test", exception);
            return false;
        }
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConnection.class
                .getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                // Fallback: read the file from the working directory.
                try (InputStream fileInput = new java.io.FileInputStream(CONFIG_FILE)) {
                    properties.load(fileInput);
                } catch (IOException fileException) {
                    LoggerUtil.logError("Could not read " + CONFIG_FILE
                            + " from the working directory", fileException);
                }
            }
        } catch (IOException exception) {
            LoggerUtil.logError("Could not read " + CONFIG_FILE, exception);
        }
        applyEnvironmentOverrides(properties);
        return properties;
    }

    /**
     * Applies environment variables after the local file so deployment secrets
     * can stay outside the repository. For example, {@code db.password} maps
     * to {@code ERP_DB_PASSWORD}.
     */
    private void applyEnvironmentOverrides(Properties properties) {
        String[] keys = {
            "db.host", "db.port", "db.name", "db.user", "db.password",
            "db.ssl", "db.verifyServerCertificate", "db.allowPublicKeyRetrieval",
            "db.poolSize"
        };
        for (String key : keys) {
            String propertyName = key.substring(3).replace('.', '_');
            String environmentKey = ENV_PREFIX + propertyName
                    .replaceAll("([a-z])([A-Z])", "$1_$2")
                    .toUpperCase(java.util.Locale.ROOT);
            String value = System.getenv(environmentKey);
            if (value != null && !value.isBlank()) {
                properties.setProperty(key, value.trim());
            }
        }
    }
}
