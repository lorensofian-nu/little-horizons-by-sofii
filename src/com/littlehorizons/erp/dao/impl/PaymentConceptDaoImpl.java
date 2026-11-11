package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.PaymentConceptDao;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of {@link PaymentConceptDao}. Opens a fresh connection per operation. */
public class PaymentConceptDaoImpl implements PaymentConceptDao {

    private static final String SELECT_BASE =
            "SELECT id_concepto, codigo_concepto, nombre_concepto, descripcion, monto_base, "
            + "es_recurrente, esta_activo, fecha_creacion, fecha_actualizacion FROM conceptos_pago ";

    public PaymentConceptDaoImpl() {
        // Connection is obtained per operation; no-op constructor kept for the no-arg chain.
    }

    @Override
    public int save(PaymentConcept concept) throws SQLException {
        String sql = "INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, "
                + "monto_base, es_recurrente, esta_activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, concept.getConceptCode());
            statement.setString(2, concept.getConceptName());
            statement.setString(3, concept.getDescription());
            statement.setBigDecimal(4, concept.getBaseAmount());
            statement.setBoolean(5, concept.isRecurring());
            statement.setBoolean(6, concept.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    concept.setId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    @Override
    public PaymentConcept findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_concepto = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public PaymentConcept findByCode(String conceptCode) throws SQLException {
        String sql = SELECT_BASE + "WHERE codigo_concepto = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, conceptCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<PaymentConcept> findAll() throws SQLException {
        return queryList(SELECT_BASE + "ORDER BY codigo_concepto");
    }

    @Override
    public List<PaymentConcept> findAllActive() throws SQLException {
        return queryList(SELECT_BASE + "WHERE esta_activo = TRUE ORDER BY codigo_concepto");
    }

    @Override
    public boolean update(PaymentConcept concept) throws SQLException {
        String sql = "UPDATE conceptos_pago SET codigo_concepto = ?, nombre_concepto = ?, descripcion = ?, "
                + "monto_base = ?, es_recurrente = ?, esta_activo = ? WHERE id_concepto = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, concept.getConceptCode());
            statement.setString(2, concept.getConceptName());
            statement.setString(3, concept.getDescription());
            statement.setBigDecimal(4, concept.getBaseAmount());
            statement.setBoolean(5, concept.isRecurring());
            statement.setBoolean(6, concept.isActive());
            statement.setInt(7, concept.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM conceptos_pago WHERE id_concepto = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private List<PaymentConcept> queryList(String sql) throws SQLException {
        List<PaymentConcept> concepts = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                concepts.add(mapRow(resultSet));
            }
        }
        return concepts;
    }

    private PaymentConcept mapRow(ResultSet resultSet) throws SQLException {
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        Timestamp update = resultSet.getTimestamp("fecha_actualizacion");
        return new PaymentConcept(
                resultSet.getInt("id_concepto"),
                resultSet.getString("codigo_concepto"),
                resultSet.getString("nombre_concepto"),
                resultSet.getString("descripcion"),
                resultSet.getBigDecimal("monto_base"),
                resultSet.getBoolean("es_recurrente"),
                resultSet.getBoolean("esta_activo"),
                creation == null ? null : creation.toLocalDateTime(),
                update == null ? null : update.toLocalDateTime());
    }
}
