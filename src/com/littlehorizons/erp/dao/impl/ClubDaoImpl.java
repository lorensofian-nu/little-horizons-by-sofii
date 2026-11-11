package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.TeacherSummary;
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
 * JDBC implementation of {@link ClubDao} for the {@code clubes} table.
 */
public class ClubDaoImpl implements ClubDao {

    private static final String BASE_SELECT = """
            SELECT c.id_club, c.nombre_club, c.descripcion, c.id_profesor_supervisor,
                   c.capacidad_maxima, c.horario_reunion, c.esta_activo,
                   c.fecha_creacion, c.fecha_actualizacion,
                   CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido) AS nombre_profesor
            FROM clubes c
            LEFT JOIN profesores p ON p.id_profesor = c.id_profesor_supervisor
            """;

    private static final String INSERT_SQL = """
            INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor,
                                capacidad_maxima, horario_reunion, esta_activo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE clubes
            SET nombre_club = ?, descripcion = ?, id_profesor_supervisor = ?,
                capacidad_maxima = ?, horario_reunion = ?, esta_activo = ?
            WHERE id_club = ?
            """;

    private static final String TEACHERS_SQL = """
            SELECT id_profesor,
                   CONCAT_WS(' ', primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) AS nombre_completo
            FROM profesores
            WHERE estado = 'Activo'
            ORDER BY primer_apellido, primer_nombre
            """;

    @Override
    public int save(Club club) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, club.getClubName());
            statement.setString(2, club.getDescription());
            setNullableInt(statement, 3, club.getSupervisingTeacherId());
            statement.setInt(4, club.getMaxCapacity() == null ? 30 : club.getMaxCapacity());
            statement.setString(5, club.getMeetingSchedule());
            statement.setBoolean(6, club.isActive());
            statement.executeUpdate();
            return fetchGeneratedId(statement);
        }
    }

    @Override
    public Club findById(int clubId) throws SQLException {
        String sql = BASE_SELECT + " WHERE c.id_club = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clubId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Club> findAll() throws SQLException {
        return queryList(BASE_SELECT + " ORDER BY c.nombre_club");
    }

    @Override
    public List<Club> findActive() throws SQLException {
        return queryList(BASE_SELECT + " WHERE c.esta_activo = TRUE ORDER BY c.nombre_club");
    }

    @Override
    public boolean update(Club club) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, club.getClubName());
            statement.setString(2, club.getDescription());
            setNullableInt(statement, 3, club.getSupervisingTeacherId());
            statement.setInt(4, club.getMaxCapacity() == null ? 30 : club.getMaxCapacity());
            statement.setString(5, club.getMeetingSchedule());
            statement.setBoolean(6, club.isActive());
            statement.setInt(7, club.getClubId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int clubId) throws SQLException {
        String sql = "DELETE FROM clubes WHERE id_club = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clubId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean clubNameExists(String clubName, int excludeClubId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clubes WHERE nombre_club = ? AND id_club <> ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, clubName);
            statement.setInt(2, excludeClubId);
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

    private List<Club> queryList(String sql) throws SQLException {
        List<Club> clubs = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                clubs.add(mapRow(resultSet));
            }
        }
        return clubs;
    }

    private Club mapRow(ResultSet resultSet) throws SQLException {
        Club club = new Club();
        club.setClubId(resultSet.getInt("id_club"));
        club.setClubName(resultSet.getString("nombre_club"));
        club.setDescription(resultSet.getString("descripcion"));
        int teacherId = resultSet.getInt("id_profesor_supervisor");
        club.setSupervisingTeacherId(resultSet.wasNull() ? null : teacherId);
        int capacity = resultSet.getInt("capacidad_maxima");
        club.setMaxCapacity(resultSet.wasNull() ? null : capacity);
        club.setMeetingSchedule(resultSet.getString("horario_reunion"));
        club.setActive(resultSet.getBoolean("esta_activo"));
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        club.setCreationDate(creation == null ? null : creation.toLocalDateTime());
        Timestamp update = resultSet.getTimestamp("fecha_actualizacion");
        club.setUpdateDate(update == null ? null : update.toLocalDateTime());
        club.setSupervisingTeacherName(resultSet.getString("nombre_profesor"));
        return club;
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private int fetchGeneratedId(Statement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            return keys.next() ? keys.getInt(1) : 0;
        }
    }
}
