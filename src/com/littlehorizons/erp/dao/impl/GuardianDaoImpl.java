package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.GuardianDocumentType;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link GuardianDao} for the {@code acudientes} table.
 */
public class GuardianDaoImpl implements GuardianDao {

    private static final String SQL_INSERT =
            "INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
            + "primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, "
            + "correo_electronico, direccion, ocupacion, es_responsable_financiero) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE acudientes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, "
            + "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, parentesco = ?, "
            + "telefono_principal = ?, telefono_secundario = ?, correo_electronico = ?, direccion = ?, "
            + "ocupacion = ?, es_responsable_financiero = ? WHERE id_acudiente = ?";

    private static final String SQL_SELECT_BASE =
            "SELECT id_acudiente, tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
            + "primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, "
            + "correo_electronico, direccion, ocupacion, es_responsable_financiero, "
            + "fecha_creacion, fecha_actualizacion FROM acudientes ";

    @Override
    public int save(Guardian guardian) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {
            fillParameters(statement, guardian);
            statement.executeUpdate();
            return readGeneratedKey(statement);
        }
    }

    @Override
    public Guardian findById(int id) throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE id_acudiente = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Guardian> findAll() throws SQLException {
        String sql = SQL_SELECT_BASE + "ORDER BY primer_apellido, primer_nombre, id_acudiente";
        List<Guardian> guardians = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                guardians.add(mapRow(resultSet));
            }
        }
        return guardians;
    }

    @Override
    public Guardian findByDocumentNumber(String documentNumber) throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE numero_documento = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, documentNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Guardian> searchByNameOrDocument(String term) throws SQLException {
        String sql = SQL_SELECT_BASE
                + "WHERE CONCAT(primer_nombre, ' ', IFNULL(segundo_nombre, ''), ' ', primer_apellido, ' ', "
                + "IFNULL(segundo_apellido, '')) LIKE ? OR numero_documento LIKE ? "
                + "ORDER BY primer_apellido, primer_nombre";
        String pattern = "%" + term + "%";
        List<Guardian> guardians = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    guardians.add(mapRow(resultSet));
                }
            }
        }
        return guardians;
    }

    @Override
    public boolean update(Guardian guardian) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            fillParameters(statement, guardian);
            statement.setInt(14, guardian.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM acudientes WHERE id_acudiente = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM acudientes";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private void fillParameters(PreparedStatement statement, Guardian guardian) throws SQLException {
        statement.setString(1, guardian.getDocumentType() == null
                ? GuardianDocumentType.CC.toString() : guardian.getDocumentType().toString());
        statement.setString(2, guardian.getDocumentNumber());
        statement.setString(3, guardian.getFirstName());
        setNullableString(statement, 4, guardian.getSecondName());
        statement.setString(5, guardian.getLastName());
        setNullableString(statement, 6, guardian.getSecondLastName());
        statement.setString(7, guardian.getRelationship());
        statement.setString(8, guardian.getPrimaryPhone());
        setNullableString(statement, 9, guardian.getSecondaryPhone());
        setNullableString(statement, 10, guardian.getEmail());
        setNullableString(statement, 11, guardian.getAddress());
        setNullableString(statement, 12, guardian.getOccupation());
        statement.setBoolean(13, guardian.isFinancialResponsible());
    }

    private void setNullableString(PreparedStatement statement, int index, String value)
            throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private int readGeneratedKey(Statement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            return keys.next() ? keys.getInt(1) : 0;
        }
    }

    private Guardian mapRow(ResultSet resultSet) throws SQLException {
        Guardian guardian = new Guardian();
        guardian.setId(resultSet.getInt("id_acudiente"));
        guardian.setDocumentType(GuardianDocumentType.fromDb(resultSet.getString("tipo_documento")));
        guardian.setDocumentNumber(resultSet.getString("numero_documento"));
        guardian.setFirstName(resultSet.getString("primer_nombre"));
        guardian.setSecondName(resultSet.getString("segundo_nombre"));
        guardian.setLastName(resultSet.getString("primer_apellido"));
        guardian.setSecondLastName(resultSet.getString("segundo_apellido"));
        guardian.setRelationship(resultSet.getString("parentesco"));
        guardian.setPrimaryPhone(resultSet.getString("telefono_principal"));
        guardian.setSecondaryPhone(resultSet.getString("telefono_secundario"));
        guardian.setEmail(resultSet.getString("correo_electronico"));
        guardian.setAddress(resultSet.getString("direccion"));
        guardian.setOccupation(resultSet.getString("ocupacion"));
        guardian.setFinancialResponsible(resultSet.getBoolean("es_responsable_financiero"));
        Timestamp createdAt = resultSet.getTimestamp("fecha_creacion");
        guardian.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        Timestamp updatedAt = resultSet.getTimestamp("fecha_actualizacion");
        guardian.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        return guardian;
    }
}
