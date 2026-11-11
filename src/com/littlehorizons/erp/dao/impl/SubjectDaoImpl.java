package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.model.GradeSummary;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link SubjectDao} for the {@code materias} table.
 * All statements use PreparedStatement and try-with-resources.
 */
public class SubjectDaoImpl implements SubjectDao {

    private static final String SELECT_BASE = """
            SELECT id_materia, codigo_materia, nombre_materia, descripcion,
                   id_grado, horas_semanales, esta_activa, fecha_creacion
            FROM materias
            """;

    @Override
    public int save(Subject subject) throws SQLException {
        String sql = """
                INSERT INTO materias
                    (codigo_materia, nombre_materia, descripcion, id_grado,
                     horas_semanales, esta_activa)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, subject.getCode());
            statement.setString(2, subject.getName());
            statement.setString(3, subject.getDescription());
            statement.setInt(4, subject.getGradeId());
            statement.setInt(5, subject.getWeeklyHours());
            statement.setBoolean(6, subject.isActive());
            statement.executeUpdate();
            return generateId(statement);
        }
    }

    @Override
    public Subject findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_materia = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Subject> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY id_grado, codigo_materia";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    @Override
    public List<Subject> findActive() throws SQLException {
        String sql = SELECT_BASE + " WHERE esta_activa = TRUE ORDER BY id_grado, codigo_materia";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    @Override
    public List<Subject> findByGrade(int gradeId) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_grado = ? ORDER BY codigo_materia";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, gradeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public Subject findByCode(String code) throws SQLException {
        String sql = SELECT_BASE + " WHERE codigo_materia = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public boolean update(Subject subject) throws SQLException {
        String sql = """
                UPDATE materias
                SET codigo_materia = ?, nombre_materia = ?, descripcion = ?,
                    id_grado = ?, horas_semanales = ?, esta_activa = ?
                WHERE id_materia = ?
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, subject.getCode());
            statement.setString(2, subject.getName());
            statement.setString(3, subject.getDescription());
            statement.setInt(4, subject.getGradeId());
            statement.setInt(5, subject.getWeeklyHours());
            statement.setBoolean(6, subject.isActive());
            statement.setInt(7, subject.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM materias WHERE id_materia = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsByCode(String code, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM materias WHERE codigo_materia = ?"
                : "SELECT COUNT(*) FROM materias WHERE codigo_materia = ? AND id_materia <> ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            if (excludeId != null) {
                statement.setInt(2, excludeId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public List<GradeSummary> listGrades() throws SQLException {
        String sql = "SELECT id_grado, nombre_grado, nivel_numerico FROM grados ORDER BY nivel_numerico";
        List<GradeSummary> grades = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                grades.add(new GradeSummary(
                        resultSet.getInt("id_grado"),
                        resultSet.getString("nombre_grado"),
                        resultSet.getInt("nivel_numerico")));
            }
            return grades;
        }
    }

    /** Extracts the auto-generated key of the last insert. */
    private int generateId(Statement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        LoggerUtil.logError("SubjectDaoImpl.save", new SQLException("No generated key returned"));
        return 0;
    }

    private List<Subject> mapList(ResultSet resultSet) throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        while (resultSet.next()) {
            subjects.add(mapRow(resultSet));
        }
        return subjects;
    }

    private Subject mapRow(ResultSet resultSet) throws SQLException {
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        LocalDateTime creationDate = creation != null ? creation.toLocalDateTime() : null;
        return new Subject(
                resultSet.getInt("id_materia"),
                resultSet.getString("codigo_materia"),
                resultSet.getString("nombre_materia"),
                resultSet.getString("descripcion"),
                resultSet.getInt("id_grado"),
                resultSet.getInt("horas_semanales"),
                resultSet.getBoolean("esta_activa"),
                creationDate);
    }

    /** Optional-style lookup kept package-visible for future use. */
    Optional<Subject> findByCodeAsOptional(String code) throws SQLException {
        return Optional.ofNullable(findByCode(code));
    }
}
