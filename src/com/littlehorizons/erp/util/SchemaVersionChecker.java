package com.littlehorizons.erp.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Startup check that the database schema is at the required migration level.
 *
 * <p>Reads the {@code schema_migrations} table that the SQL scripts maintain
 * and fails fast with clear instructions when required versions are missing,
 * instead of surfacing obscure SQL errors later during normal operations.</p>
 */
public final class SchemaVersionChecker {

    /** Migration versions the application requires to work correctly. */
    private static final Set<String> REQUIRED_VERSIONS = Set.of(
            "V001", "V002", "V003", "V004", "V005", "V006");

    private SchemaVersionChecker() {
        // Utility class must not be instantiated.
    }

    /**
     * Returns the migration versions recorded in the database that are
     * missing from the required set; empty when the schema is current.
     *
     * @throws SQLException when the {@code schema_migrations} table does not
     *     exist or cannot be read; callers must treat that as an outdated
     *     schema too.
     */
    public static Set<String> missingVersions() throws SQLException {
        Set<String> applied = new LinkedHashSet<>();
        String sql = "SELECT version FROM schema_migrations";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                applied.add(resultSet.getString(1));
            }
        }
        Set<String> missing = new LinkedHashSet<>(REQUIRED_VERSIONS);
        missing.removeAll(applied);
        return missing;
    }

    /** Human-readable sentence listing the missing migrations, or empty text. */
    public static String missingVersionsMessage(Set<String> missing) {
        StringBuilder message = new StringBuilder(
                "The database schema is outdated. Missing migrations: ");
        message.append(String.join(", ", missing));
        message.append(". Run the scripts in docs/migrations/ in ascending order.");
        return message.toString();
    }
}
