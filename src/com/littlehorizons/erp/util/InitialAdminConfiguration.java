package com.littlehorizons.erp.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Loads first-admin settings without exposing credentials in logs. */
public final class InitialAdminConfiguration {

    private static final String CONFIG_FILE = "db.properties";

    private InitialAdminConfiguration() {
        // Utility class must not be instantiated.
    }

    public static String username() {
        Properties properties = loadProperties();
        return firstNonBlank(System.getenv("ERP_INITIAL_ADMIN_USERNAME"),
                properties.getProperty("app.initialAdminUsername"), "admin");
    }

    public static char[] password() {
        Properties properties = loadProperties();
        String configured = firstNonBlank(System.getenv("ERP_INITIAL_ADMIN_PASSWORD"),
                properties.getProperty("app.initialAdminPassword"), "");
        return configured.toCharArray();
    }

    private static String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return fallback;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream resource = InitialAdminConfiguration.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (resource != null) {
                properties.load(resource);
                return properties;
            }
        } catch (IOException ignored) {
            return properties;
        }

        try (InputStream file = new FileInputStream(CONFIG_FILE)) {
            properties.load(file);
        } catch (IOException ignored) {
            // Missing local configuration is handled by the caller.
        }
        return properties;
    }
}
