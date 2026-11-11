package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.EventParticipationDao;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.ParticipationRole;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link EventParticipationDao} for the
 * {@code participantes_evento} table.
 */
public class EventParticipationDaoImpl implements EventParticipationDao {

    private static final String BASE_SELECT = """
            SELECT pe.id_participacion, pe.id_evento, pe.id_estudiante, pe.rol_participacion,
                   pe.posicion_obtenida, pe.observaciones, pe.fecha_creacion,
                   e.nombre_evento,
                   CONCAT_WS(' ', est.primer_nombre, est.segundo_nombre, est.primer_apellido, est.segundo_apellido) AS nombre_estudiante
            FROM participantes_evento pe
            JOIN eventos e ON e.id_evento = pe.id_evento
            JOIN estudiantes est ON est.id_estudiante = pe.id_estudiante
            """;

    private static final String INSERT_SQL = """
            INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion,
                                              posicion_obtenida, observaciones)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE participantes_evento
            SET rol_participacion = ?, posicion_obtenida = ?, observaciones = ?
            WHERE id_participacion = ?
            """;

    private static final String STUDENTS_SQL = """
            SELECT id_estudiante,
                   CONCAT_WS(' ', primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) AS nombre_completo
            FROM estudiantes
            WHERE estado = 'Activo'
            ORDER BY primer_apellido, primer_nombre
            """;

    @Override
    public int save(EventParticipation participation) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, participation.getEventId());
            statement.setInt(2, participation.getStudentId());
            statement.setString(3, participation.getParticipationRole().getLabel());
            statement.setString(4, participation.getPositionObtained());
            statement.setString(5, participation.getObservations());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public EventParticipation findById(int participationId) throws SQLException {
        String sql = BASE_SELECT + " WHERE pe.id_participacion = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, participationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<EventParticipation> findByEvent(int eventId) throws SQLException {
        List<EventParticipation> participations = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE pe.id_evento = ? ORDER BY est.primer_apellido";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    participations.add(mapRow(resultSet));
                }
            }
        }
        return participations;
    }

    @Override
    public List<EventParticipation> findByStudent(int studentId) throws SQLException {
        List<EventParticipation> participations = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE pe.id_estudiante = ? ORDER BY e.fecha_evento DESC";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    participations.add(mapRow(resultSet));
                }
            }
        }
        return participations;
    }

    @Override
    public List<EventParticipation> findRanked() throws SQLException {
        return queryList(BASE_SELECT
                + " WHERE pe.posicion_obtenida IS NOT NULL ORDER BY est.primer_apellido");
    }

    @Override
    public boolean participationExists(int eventId, int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM participantes_evento WHERE id_evento = ? AND id_estudiante = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            statement.setInt(2, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean update(EventParticipation participation) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, participation.getParticipationRole().getLabel());
            statement.setString(2, participation.getPositionObtained());
            statement.setString(3, participation.getObservations());
            statement.setInt(4, participation.getParticipationId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int participationId) throws SQLException {
        String sql = "DELETE FROM participantes_evento WHERE id_participacion = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, participationId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public List<StudentSummary> listActiveStudents() throws SQLException {
        List<StudentSummary> students = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(STUDENTS_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                students.add(new StudentSummary(resultSet.getInt("id_estudiante"),
                        resultSet.getString("nombre_completo")));
            }
        }
        return students;
    }

    private List<EventParticipation> queryList(String sql) throws SQLException {
        List<EventParticipation> participations = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                participations.add(mapRow(resultSet));
            }
        }
        return participations;
    }

    private EventParticipation mapRow(ResultSet resultSet) throws SQLException {
        EventParticipation participation = new EventParticipation();
        participation.setParticipationId(resultSet.getInt("id_participacion"));
        participation.setEventId(resultSet.getInt("id_evento"));
        participation.setStudentId(resultSet.getInt("id_estudiante"));
        participation.setParticipationRole(
                ParticipationRole.fromLabel(resultSet.getString("rol_participacion")));
        participation.setPositionObtained(resultSet.getString("posicion_obtenida"));
        participation.setObservations(resultSet.getString("observaciones"));
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        participation.setCreationDate(creation == null ? null : creation.toLocalDateTime());
        participation.setEventName(resultSet.getString("nombre_evento"));
        participation.setStudentFullName(resultSet.getString("nombre_estudiante"));
        return participation;
    }
}
