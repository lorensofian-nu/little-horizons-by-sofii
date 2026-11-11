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
 * at the project root. Callers obtain a fresh {@link Connection} on each request and
 * are expected to close it using try-with-resources.</p>
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
        String host = properties.getProperty("db.host", "localhost");
        String port = properties.getProperty("db.port", "3306");
        String database = properties.getProperty("db.name", "erp_little_horizons");
        this.user = properties.getProperty("db.user", "root");
        this.password = properties.getProperty("db.password", "");
        this.jdbcUrl = String.format(JDBC_URL_TEMPLATE, host, port, database);
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
                            + "; using default connection settings", fileException);
                }
            }
        } catch (IOException exception) {
            LoggerUtil.logError("Could not read " + CONFIG_FILE, exception);
        }
        return properties;
    }
}
