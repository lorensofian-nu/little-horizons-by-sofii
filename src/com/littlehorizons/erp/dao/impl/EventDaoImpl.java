package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.EventDao;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.EventType;
import com.littlehorizons.erp.model.TeacherSummary;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link EventDao} for the {@code eventos} table.
 */
public class EventDaoImpl implements EventDao {

    private static final String BASE_SELECT = """
            SELECT e.id_evento, e.nombre_evento, e.descripcion, e.tipo_evento,
                   e.fecha_evento, e.lugar, e.tiene_competencia, e.id_organizador,
                   e.esta_activo, e.fecha_creacion,
                   CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS nombre_profesor
            FROM eventos e
            LEFT JOIN profesores p ON p.id_profesor = e.id_organizador
            """;

    private static final String INSERT_SQL = """
            INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento,
                                 lugar, tiene_competencia, id_organizador, esta_activo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE eventos
            SET nombre_evento = ?, descripcion = ?, tipo_evento = ?, fecha_evento = ?,
                lugar = ?, tiene_competencia = ?, id_organizador = ?, esta_activo = ?
            WHERE id_evento = ?
            """;

    private static final String TEACHERS_SQL = """
            SELECT id_profesor,
                   CONCAT_WS(' ', primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) AS nombre_completo
            FROM profesores
            WHERE estado = 'Activo'
            ORDER BY primer_apellido, primer_nombre
            """;

    @Override
    public int save(Event event) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, event.getEventName());
            statement.setString(2, event.getDescription());
            statement.setString(3, event.getEventType().getLabel());
            statement.setDate(4, Date.valueOf(event.getEventDate()));
            statement.setString(5, event.getPlace());
            statement.setBoolean(6, event.isHasCompetition());
            setNullableInt(statement, 7, event.getOrganizerId());
            statement.setBoolean(8, event.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public Event findById(int eventId) throws SQLException {
        String sql = BASE_SELECT + " WHERE e.id_evento = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Event> findAll() throws SQLException {
        return queryList(BASE_SELECT + " ORDER BY e.fecha_evento DESC");
    }

    @Override
    public List<Event> findActive() throws SQLException {
        return queryList(BASE_SELECT + " WHERE e.esta_activo = TRUE ORDER BY e.fecha_evento DESC");
    }

    @Override
    public boolean update(Event event) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, event.getEventName());
            statement.setString(2, event.getDescription());
            statement.setString(3, event.getEventType().getLabel());
            statement.setDate(4, Date.valueOf(event.getEventDate()));
            statement.setString(5, event.getPlace());
            statement.setBoolean(6, event.isHasCompetition());
            setNullableInt(statement, 7, event.getOrganizerId());
            statement.setBoolean(8, event.isActive());
            statement.setInt(9, event.getEventId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int eventId) throws SQLException {
        String sql = "DELETE FROM eventos WHERE id_evento = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eventNameExists(String eventName, int excludeEventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM eventos WHERE nombre_evento = ? AND id_evento <> ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventName);
            statement.setInt(2, excludeEventId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public List<TeacherSummary> listActiveTeachers() throws SQLException {
        List<TeacherSummary> teachers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(TEACHERS_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                teachers.add(new TeacherSummary(resultSet.getInt("id_profesor"),
                        resultSet.getString("nombre_completo")));
            }
        }
        return teachers;
    }

    private List<Event> queryList(String sql) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                events.add(mapRow(resultSet));
            }
        }
        return events;
    }

    private Event mapRow(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setEventId(resultSet.getInt("id_evento"));
        event.setEventName(resultSet.getString("nombre_evento"));
        event.setDescription(resultSet.getString("descripcion"));
        event.setEventType(EventType.fromLabel(resultSet.getString("tipo_evento")));
        Date eventDate = resultSet.getDate("fecha_evento");
        event.setEventDate(eventDate == null ? null : eventDate.toLocalDate());
        event.setPlace(resultSet.getString("lugar"));
        event.setHasCompetition(resultSet.getBoolean("tiene_competencia"));
        int organizerId = resultSet.getInt("id_organizador");
        event.setOrganizerId(resultSet.wasNull() ? null : organizerId);
        event.setActive(resultSet.getBoolean("esta_activo"));
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        event.setCreationDate(creation == null ? null : creation.toLocalDateTime());
        event.setOrganizerName(resultSet.getString("nombre_profesor"));
        return event;
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }
}
