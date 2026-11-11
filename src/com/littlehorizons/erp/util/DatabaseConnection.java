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
 * <p>Connection settings are read once from the {@code db.properties} file located
 * at the project root. Missing required settings fail fast with a clear error;
 * no default credentials are ever assumed. Callers obtain a fresh {@link Connection}
 * on each request and are expected to close it using try-with-resources.</p>
 */
public final class DatabaseConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static final String JDBC_URL_TEMPLATE =
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Bogota&characterEncoding=UTF-8";

    private static volatile DatabaseConnection instance;

    private final String jdbcUrl;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        Properties properties = loadProperties();
        String host = requireProperty(properties, "db.host");
        String port = requireProperty(properties, "db.port");
        String database = requireProperty(properties, "db.name");
        this.user = requireProperty(properties, "db.user");
        // The password may legitimately be empty for a local development MySQL user.
        this.password = properties.getProperty("db.password", "");
        this.jdbcUrl = String.format(JDBC_URL_TEMPLATE, host, port, database);
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
        return DriverManager.getConnection(jdbcUrl, user, password);
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
        return properties;
    }
}
