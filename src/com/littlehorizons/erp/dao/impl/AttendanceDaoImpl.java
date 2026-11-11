package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.AttendanceStatus;
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
import java.util.Objects;

/**
 * JDBC implementation of {@link AttendanceDao} for the {@code asistencias} table.
 */
public class AttendanceDaoImpl implements AttendanceDao {

    private static final String SELECT_BASE = """
            SELECT id_asistencia, id_estudiante, id_materia, fecha_asistencia,
                   estado_asistencia, observaciones, registrado_por, fecha_creacion
            FROM asistencias
            """;

    @Override
    public int save(Attendance attendance) throws SQLException {
        String sql = """
                INSERT INTO asistencias
                    (id_estudiante, id_materia, fecha_asistencia, estado_asistencia,
                     observaciones, registrado_por)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, attendance.getStudentId());
            statement.setInt(2, attendance.getSubjectId());
            statement.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
            statement.setString(4, attendance.getStatus().getLabel());
            statement.setString(5, attendance.getObservations());
            if (attendance.getRegisteredBy() != null) {
                statement.setInt(6, attendance.getRegisteredBy());
            } else {
                statement.setNull(6, java.sql.Types.INTEGER);
            }
            statement.executeUpdate();
            return generateId(statement);
        }
    }

    @Override
    public Attendance findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_asistencia = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Attendance> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY fecha_asistencia DESC, id_estudiante";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    @Override
    public List<Attendance> findByStudent(int studentId) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_estudiante = ? ORDER BY fecha_asistencia DESC";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public List<Attendance> findBySubjectAndDate(int subjectId, LocalDate date) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_materia = ? AND fecha_asistencia = ? ORDER BY id_estudiante";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, subjectId);
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public List<Attendance> findBySubjectAndYear(int subjectId, int academicYear) throws SQLException {
        String sql = SELECT_BASE
                + " WHERE id_materia = ? AND YEAR(fecha_asistencia) = ? ORDER BY fecha_asistencia";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, subjectId);
            statement.setInt(2, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public List<Attendance> findByStudentAndYear(int studentId, int academicYear) throws SQLException {
        String sql = SELECT_BASE
                + " WHERE id_estudiante = ? AND YEAR(fecha_asistencia) = ? ORDER BY fecha_asistencia";
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
    public List<Attendance> findByYear(int academicYear) throws SQLException {
        String sql = SELECT_BASE + " WHERE YEAR(fecha_asistencia) = ? ORDER BY fecha_asistencia, id_estudiante";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public boolean update(Attendance attendance) throws SQLException {
        String sql = """
                UPDATE asistencias
                SET id_estudiante = ?, id_materia = ?, fecha_asistencia = ?, estado_asistencia = ?,
                    observaciones = ?, registrado_por = ?
                WHERE id_asistencia = ?
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, attendance.getStudentId());
            statement.setInt(2, attendance.getSubjectId());
            statement.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
            statement.setString(4, attendance.getStatus().getLabel());
            statement.setString(5, attendance.getObservations());
            if (attendance.getRegisteredBy() != null) {
                statement.setInt(6, attendance.getRegisteredBy());
            } else {
                statement.setNull(6, java.sql.Types.INTEGER);
            }
            statement.setInt(7, attendance.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM asistencias WHERE id_asistencia = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsFor(int studentId, int subjectId, LocalDate date, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM asistencias WHERE id_estudiante = ? AND id_materia = ? AND fecha_asistencia = ?"
                : """
                  SELECT COUNT(*) FROM asistencias
                  WHERE id_estudiante = ? AND id_materia = ? AND fecha_asistencia = ? AND id_asistencia <> ?
                  """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, subjectId);
            statement.setDate(3, Date.valueOf(date));
            if (excludeId != null) {
                statement.setInt(4, excludeId);
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
        LoggerUtil.logError("AttendanceDaoImpl.save", new SQLException("No generated key returned"));
        return 0;
    }

    private List<Attendance> mapList(ResultSet resultSet) throws SQLException {
        List<Attendance> attendances = new ArrayList<>();
        while (resultSet.next()) {
            attendances.add(mapRow(resultSet));
        }
        return attendances;
    }

    private Attendance mapRow(ResultSet resultSet) throws SQLException {
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        LocalDateTime creationDate = creation != null ? creation.toLocalDateTime() : null;
        Date attendance = resultSet.getDate("fecha_asistencia");
        LocalDate attendanceDate = attendance != null ? attendance.toLocalDate() : LocalDate.now();
        int registeredBy = resultSet.getInt("registrado_por");
        Integer registeredByValue = resultSet.wasNull() ? null : registeredBy;
        return new Attendance(
                resultSet.getInt("id_asistencia"),
                resultSet.getInt("id_estudiante"),
                resultSet.getInt("id_materia"),
                attendanceDate,
                AttendanceStatus.fromLabel(resultSet.getString("estado_asistencia")),
                resultSet.getString("observaciones"),
                registeredByValue,
                creationDate);
    }
}
