package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.TeacherAssignmentDao;
import com.littlehorizons.erp.model.SubjectSummary;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link TeacherAssignmentDao} for the
 * {@code asignaciones_profesor} table. Listing queries JOIN with
 * {@code profesores} and {@code materias} to show friendly descriptions.
 *
 * <p>Known limitation (per project instruction): the DDL has no schedule
 * columns, so class day/time cannot be persisted. Overlap checking is left to
 * the operator, who sees the teacher's existing assignments in the console
 * before confirming a new one.</p>
 */
public class TeacherAssignmentDaoImpl implements TeacherAssignmentDao {

    private static final String SELECT_JOINED =
            "SELECT a.id_asignacion, a.id_profesor, a.id_materia, a.año_academico, a.seccion, "
                    + "a.fecha_creacion, "
                    + "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, "
                    + "p.segundo_apellido) AS nombre_profesor, "
                    + "m.codigo_materia, m.nombre_materia "
                    + "FROM asignaciones_profesor a "
                    + "JOIN profesores p ON p.id_profesor = a.id_profesor "
                    + "JOIN materias m ON m.id_materia = a.id_materia";

    private final Connection connection;

    /** Builds the DAO with the shared database connection. */
    public TeacherAssignmentDaoImpl() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not obtain the database connection.", exception);
        }
    }

    @Override
    public int save(TeacherAssignment assignment) throws SQLException {
        String sql = "INSERT INTO asignaciones_profesor "
                + "(id_profesor, id_materia, año_academico, seccion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, assignment.getTeacherId());
            statement.setInt(2, assignment.getSubjectId());
            statement.setInt(3, assignment.getAcademicYear());
            statement.setString(4, assignment.getSection());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    assignment.setId(id);
                    return id;
                }
            }
            throw new SQLException("Insert into asignaciones_profesor did not return a generated key.");
        }
    }

    @Override
    public Optional<TeacherAssignment> findById(int id) throws SQLException {
        String sql = SELECT_JOINED + " WHERE a.id_asignacion = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public List<TeacherAssignment> findAll() throws SQLException {
        String sql = SELECT_JOINED + " ORDER BY p.primer_apellido, a.año_academico, m.nombre_materia";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<TeacherAssignment> assignments = new ArrayList<>();
            while (resultSet.next()) {
                assignments.add(mapRow(resultSet));
            }
            return assignments;
        }
    }

    @Override
    public List<TeacherAssignment> assignmentsByTeacher(int teacherId) throws SQLException {
        String sql = SELECT_JOINED
                + " WHERE a.id_profesor = ? ORDER BY a.año_academico, m.nombre_materia";
        return queryJoined(sql, statement -> statement.setInt(1, teacherId));
    }

    @Override
    public List<TeacherAssignment> assignmentsBySubjectAndYear(int subjectId, int academicYear)
            throws SQLException {
        String sql = SELECT_JOINED
                + " WHERE a.id_materia = ? AND a.año_academico = ? ORDER BY a.seccion";
        return queryJoined(sql, statement -> {
            statement.setInt(1, subjectId);
            statement.setInt(2, academicYear);
        });
    }

    @Override
    public boolean existsAssignment(int teacherId, int subjectId, int academicYear, String section)
            throws SQLException {
        String sql = "SELECT 1 FROM asignaciones_profesor "
                + "WHERE id_profesor = ? AND id_materia = ? AND año_academico = ? "
                + "AND seccion = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            statement.setInt(2, subjectId);
            statement.setInt(3, academicYear);
            statement.setString(4, section);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM asignaciones_profesor WHERE id_asignacion = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public List<SubjectSummary> listSubjects() throws SQLException {
        String sql = "SELECT id_materia, codigo_materia, nombre_materia, id_grado, "
                + "horas_semanales, esta_activa FROM materias ORDER BY nombre_materia";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<SubjectSummary> subjects = new ArrayList<>();
            while (resultSet.next()) {
                subjects.add(new SubjectSummary(
                        resultSet.getInt("id_materia"),
                        resultSet.getString("codigo_materia"),
                        resultSet.getString("nombre_materia"),
                        resultSet.getInt("id_grado"),
                        resultSet.getInt("horas_semanales"),
                        resultSet.getBoolean("esta_activa")));
            }
            return subjects;
        }
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    private List<TeacherAssignment> queryJoined(String sql, StatementBinder binder)
            throws SQLException {
        List<TeacherAssignment> assignments = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assignments.add(mapRow(resultSet));
                }
            }
        }
        return assignments;
    }

    private TeacherAssignment mapRow(ResultSet resultSet) throws SQLException {
        TeacherAssignment assignment = new TeacherAssignment(
                resultSet.getInt("id_asignacion"),
                resultSet.getInt("id_profesor"),
                resultSet.getInt("id_materia"),
                resultSet.getInt("año_academico"),
                resultSet.getString("seccion"),
                toLocalDateTime(resultSet.getTimestamp("fecha_creacion")));
        assignment.setTeacherFullName(resultSet.getString("nombre_profesor"));
        assignment.setSubjectCode(resultSet.getString("codigo_materia"));
        assignment.setSubjectName(resultSet.getString("nombre_materia"));
        return assignment;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
