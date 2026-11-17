package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.UserDao;
import com.littlehorizons.erp.model.AuthenticatedUser;
import com.littlehorizons.erp.model.UserRole;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.validation.ValidationRules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** JDBC authentication DAO. Password hashes are selected only when needed. */
public class UserDaoImpl implements UserDao {

    @Override
    public AuthenticatedUser findActiveUser(String username) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, rol, debe_cambiar_password, "
                + "password_changed_at FROM usuarios "
                + "WHERE nombre_usuario = ? AND activo = TRUE";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                boolean mustChange = resultSet.getBoolean("debe_cambiar_password")
                        || isPasswordExpired(resultSet.getTimestamp("password_changed_at"));
                return new AuthenticatedUser(resultSet.getInt("id_usuario"),
                        resultSet.getString("nombre_usuario"),
                        UserRole.fromDatabaseValue(resultSet.getString("rol")),
                        mustChange);
            }
        }
    }

    /**
     * A password is expired when it has not been changed within the
     * configured window; missing value (legacy row) also forces a rotation.
     */
    private boolean isPasswordExpired(java.sql.Timestamp changedAt) {
        if (changedAt == null) {
            return true;
        }
        java.time.Duration age = java.time.Duration.between(
                changedAt.toInstant(), java.time.Instant.now());
        return age.toDays() >= ValidationRules.PASSWORD_EXPIRY_DAYS;
    }

    @Override
    public String findPasswordHash(String username) throws SQLException {
        String sql = "SELECT hash_password FROM usuarios "
                + "WHERE nombre_usuario = ? AND activo = TRUE";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        }
    }

    @Override
    public void updateLastAccess(String username) throws SQLException {
        String sql = "UPDATE usuarios SET ultimo_acceso = CURRENT_TIMESTAMP WHERE nombre_usuario = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }

    @Override
    public void updatePassword(String username, String passwordHash) throws SQLException {
        String sql = "UPDATE usuarios SET hash_password = ?, debe_cambiar_password = FALSE, "
                + "password_changed_at = CURRENT_TIMESTAMP WHERE nombre_usuario = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setString(2, username);
            statement.executeUpdate();
        }
    }

    @Override
    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    @Override
    public void createUser(String username, String passwordHash, String role) throws SQLException {
        String sql = "INSERT INTO usuarios "
                + "(nombre_usuario, hash_password, rol, activo, debe_cambiar_password) "
                + "VALUES (?, ?, ?, TRUE, TRUE)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, role);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean isAccountLocked(String username) throws SQLException {
        String sql = "SELECT 1 FROM usuarios "
                + "WHERE nombre_usuario = ? AND bloqueado_hasta > CURRENT_TIMESTAMP";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public int registerFailedLogin(String username, int lockThreshold, int lockMinutes)
            throws SQLException {
        String updateSql = "UPDATE usuarios SET "
                + "intentos_fallidos = intentos_fallidos + 1, "
                + "bloqueado_hasta = CASE WHEN intentos_fallidos + 1 >= ? "
                + "THEN DATE_ADD(CURRENT_TIMESTAMP, INTERVAL ? MINUTE) "
                + "ELSE bloqueado_hasta END "
                + "WHERE nombre_usuario = ?";
        String selectSql = "SELECT intentos_fallidos FROM usuarios WHERE nombre_usuario = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement update = connection.prepareStatement(updateSql);
             PreparedStatement select = connection.prepareStatement(selectSql)) {
            update.setInt(1, lockThreshold);
            update.setInt(2, lockMinutes);
            update.setString(3, username);
            if (update.executeUpdate() == 0) {
                return 0;
            }
            select.setString(1, username);
            try (ResultSet resultSet = select.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    @Override
    public void resetFailedLogins(String username) throws SQLException {
        String sql = "UPDATE usuarios SET intentos_fallidos = 0, bloqueado_hasta = NULL "
                + "WHERE nombre_usuario = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }
}
