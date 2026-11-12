package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.UserDao;
import com.littlehorizons.erp.model.AuthenticatedUser;
import com.littlehorizons.erp.model.UserRole;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** JDBC authentication DAO. Password hashes are selected only when needed. */
public class UserDaoImpl implements UserDao {

    @Override
    public AuthenticatedUser findActiveUser(String username) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, rol, debe_cambiar_password FROM usuarios "
                + "WHERE nombre_usuario = ? AND activo = TRUE";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new AuthenticatedUser(resultSet.getInt("id_usuario"),
                        resultSet.getString("nombre_usuario"),
                        UserRole.fromDatabaseValue(resultSet.getString("rol")),
                        resultSet.getBoolean("debe_cambiar_password"));
            }
        }
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
        String sql = "UPDATE usuarios SET hash_password = ?, debe_cambiar_password = FALSE "
                + "WHERE nombre_usuario = ?";
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
                + "VALUES (?, ?, ?, TRUE, FALSE)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, role);
            statement.executeUpdate();
        }
    }
}
