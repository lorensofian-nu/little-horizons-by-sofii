package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Period;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link AcademicRecordDao} for the
 * {@code registros_academicos} table.
 */
public class AcademicRecordDaoImpl implements AcademicRecordDao {

    private static final String SELECT_BASE = """
            SELECT id_registro, id_estudiante, id_materia, id_profesor, año_academico,
                   periodo_academico, valor_nota, observaciones, fecha_registro,
                   fecha_creacion, fecha_actualizacion
            FROM registros_academicos
            """;

    @Override
    public int save(AcademicRecord record) throws SQLException {
        String sql = """
                INSERT INTO registros_academicos
                    (id_estudiante, id_materia, id_profesor, año_academico,
                     periodo_academico, valor_nota, observaciones, fecha_registro)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, record.getStudentId());
            statement.setInt(2, record.getSubjectId());
            statement.setInt(3, record.getTeacherId());
            statement.setInt(4, record.getAcademicYear());
            statement.setString(5, record.getPeriod().getLabel());
            statement.setDouble(6, record.getGrade());
            statement.setString(7, record.getObservations());
            statement.setDate(8, Date.valueOf(record.getRegistrationDate()));
            statement.executeUpdate();
            return generateId(statement);
        }
    }

    @Override
    public AcademicRecord findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_registro = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<AcademicRecord> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY año_academico, id_estudiante, id_materia";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    @Override
    public List<AcademicRecord> findByStudent(int studentId) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_estudiante = ? ORDER BY año_academico, id_materia, periodo_academico";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public List<AcademicRecord> findByStudentAndYear(int studentId, int academicYear) throws SQLException {
        String sql = SELECT_BASE
                + " WHERE id_estudiante = ? AND año_academico = ? ORDER BY id_materia, periodo_academico";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public List<AcademicRecord> findByStudentYearPeriod(int studentId, int academicYear, Period period)
            throws SQLException {
        if (period == null) {
            return findByStudentAndYear(studentId, academicYear);
        }
        String sql = SELECT_BASE
                + " WHERE id_estudiante = ? AND año_academico = ? AND periodo_academico = ?"
                + " ORDER BY id_materia";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, academicYear);
            statement.setString(3, period.getLabel());
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public List<AcademicRecord> findBySubjectYear(int subjectId, int academicYear, Period period)
            throws SQLException {
        String sql = period == null
                ? SELECT_BASE + " WHERE id_materia = ? AND año_academico = ? ORDER BY id_estudiante"
                : SELECT_BASE
                  + " WHERE id_materia = ? AND año_academico = ? AND periodo_academico = ? ORDER BY id_estudiante";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, subjectId);
            statement.setInt(2, academicYear);
            if (period != null) {
                statement.setString(3, period.getLabel());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public List<AcademicRecord> findByTeacher(int teacherId) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_profesor = ? ORDER BY año_academico, id_estudiante";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public List<AcademicRecord> findByYear(int academicYear) throws SQLException {
        String sql = SELECT_BASE + " WHERE año_academico = ? ORDER BY id_materia, id_estudiante";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public boolean update(AcademicRecord record) throws SQLException {
        String sql = """
                UPDATE registros_academicos
                SET id_estudiante = ?, id_materia = ?, id_profesor = ?, año_academico = ?,
                    periodo_academico = ?, valor_nota = ?, observaciones = ?, fecha_registro = ?
                WHERE id_registro = ?
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, record.getStudentId());
            statement.setInt(2, record.getSubjectId());
            statement.setInt(3, record.getTeacherId());
            statement.setInt(4, record.getAcademicYear());
            statement.setString(5, record.getPeriod().getLabel());
            statement.setDouble(6, record.getGrade());
            statement.setString(7, record.getObservations());
            statement.setDate(8, Date.valueOf(record.getRegistrationDate()));
            statement.setInt(9, record.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM registros_academicos WHERE id_registro = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsFor(int studentId, int subjectId, int academicYear, Period period, Integer excludeId)
            throws SQLException {
        String sql = excludeId == null
                ? """
                  SELECT COUNT(*) FROM registros_academicos
                  WHERE id_estudiante = ? AND id_materia = ? AND año_academico = ? AND periodo_academico = ?
                  """
                : """
                  SELECT COUNT(*) FROM registros_academicos
                  WHERE id_estudiante = ? AND id_materia = ? AND año_academico = ? AND periodo_academico = ?
                  AND id_registro <> ?
                  """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, subjectId);
            statement.setInt(3, academicYear);
            statement.setString(4, period.getLabel());
            if (excludeId != null) {
                statement.setInt(5, excludeId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    /** Extracts the auto-generated key of the last insert. */
    private int generateId(Statement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        LoggerUtil.logError("AcademicRecordDaoImpl.save", new SQLException("No generated key returned"));
        return 0;
    }

    private List<AcademicRecord> mapList(ResultSet resultSet) throws SQLException {
        List<AcademicRecord> records = new ArrayList<>();
        while (resultSet.next()) {
            records.add(mapRow(resultSet));
        }
        return records;
    }

    private AcademicRecord mapRow(ResultSet resultSet) throws SQLException {
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        LocalDateTime creationDate = creation != null ? creation.toLocalDateTime() : null;
        Timestamp update = resultSet.getTimestamp("fecha_actualizacion");
        LocalDateTime updateDate = update != null ? update.toLocalDateTime() : null;
        Date registration = resultSet.getDate("fecha_registro");
        LocalDate registrationDate = registration != null ? registration.toLocalDate() : LocalDate.now();
        return new AcademicRecord(
                resultSet.getInt("id_registro"),
                resultSet.getInt("id_estudiante"),
                resultSet.getInt("id_materia"),
                resultSet.getInt("id_profesor"),
                resultSet.getInt("año_academico"),
                Period.fromLabel(resultSet.getString("periodo_academico")),
                resultSet.getDouble("valor_nota"),
                resultSet.getString("observaciones"),
                registrationDate,
                creationDate,
                updateDate);
    }
}
