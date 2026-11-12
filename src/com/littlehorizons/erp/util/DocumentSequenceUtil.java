package com.littlehorizons.erp.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Allocates document numbers safely inside the caller's transaction. */
public final class DocumentSequenceUtil {

    private DocumentSequenceUtil() {
    }

    public static String next(Connection connection, String prefix, String type)
            throws SQLException {
        String sequenceName = prefix + type;
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT IGNORE INTO secuencias_documento "
                        + "(nombre_secuencia, siguiente_valor) VALUES (?, 1)")) {
            statement.setString(1, sequenceName);
            statement.executeUpdate();
        }
        int value;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT siguiente_valor FROM secuencias_documento "
                        + "WHERE nombre_secuencia = ? FOR UPDATE")) {
            statement.setString(1, sequenceName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Sequence row was not created: " + sequenceName);
                }
                value = resultSet.getInt(1);
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE secuencias_documento SET siguiente_valor = siguiente_valor + 1 "
                        + "WHERE nombre_secuencia = ?")) {
            statement.setString(1, sequenceName);
            statement.executeUpdate();
        }
        return prefix + String.format("%06d", value);
    }
}
