package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.ClubMembershipDao;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.MembershipStatus;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link ClubMembershipDao} for the
 * {@code membresias_club} table.
 */
public class ClubMembershipDaoImpl implements ClubMembershipDao {

    private static final String BASE_SELECT = """
            SELECT m.id_membresia, m.id_estudiante, m.id_club, m.fecha_ingreso,
                   m.estado_membresia, m.fecha_retiro, m.motivo_retiro, m.fecha_creacion,
                   CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante,
                   c.nombre_club
            FROM membresias_club m
            JOIN estudiantes e ON e.id_estudiante = m.id_estudiante
            JOIN clubes c ON c.id_club = m.id_club
            """;

    private static final String INSERT_SQL = """
            INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE membresias_club
            SET estado_membresia = ?, fecha_retiro = ?, motivo_retiro = ?
            WHERE id_membresia = ?
            """;

    private static final String STUDENTS_SQL = """
            SELECT id_estudiante,
                   CONCAT_WS(' ', primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) AS nombre_completo
            FROM estudiantes
            WHERE estado = 'Activo'
            ORDER BY primer_apellido, primer_nombre
            """;

    @Override
    public int save(ClubMembership membership) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, membership.getStudentId());
            statement.setInt(2, membership.getClubId());
            statement.setDate(3, Date.valueOf(membership.getJoinDate()));
            statement.setString(4, membership.getMembershipStatus().getLabel());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public ClubMembership findById(int membershipId) throws SQLException {
        String sql = BASE_SELECT + " WHERE m.id_membresia = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, membershipId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<ClubMembership> findByClub(int clubId) throws SQLException {
        return queryList(BASE_SELECT + " WHERE m.id_club = ? ORDER BY m.fecha_ingreso DESC", clubId);
    }

    @Override
    public List<ClubMembership> findActiveMembersByClub(int clubId) throws SQLException {
        return queryList(BASE_SELECT
                + " WHERE m.id_club = ? AND m.estado_membresia = 'Activa'"
                + " ORDER BY m.fecha_ingreso", clubId);
    }

    @Override
    public boolean activeMembershipExists(int studentId, int clubId) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM membresias_club
                WHERE id_estudiante = ? AND id_club = ? AND estado_membresia = 'Activa'
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, clubId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public int countActiveMembers(int clubId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clubId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean update(ClubMembership membership) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, membership.getMembershipStatus().getLabel());
            if (membership.getWithdrawalDate() == null) {
                statement.setNull(2, java.sql.Types.DATE);
            } else {
                statement.setDate(2, Date.valueOf(membership.getWithdrawalDate()));
            }
            statement.setString(3, membership.getWithdrawalReason());
            statement.setInt(4, membership.getMembershipId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int membershipId) throws SQLException {
        String sql = "DELETE FROM membresias_club WHERE id_membresia = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, membershipId);
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

    private List<ClubMembership> queryList(String sql, int clubId) throws SQLException {
        List<ClubMembership> memberships = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clubId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    memberships.add(mapRow(resultSet));
                }
            }
        }
        return memberships;
    }

    private ClubMembership mapRow(ResultSet resultSet) throws SQLException {
        ClubMembership membership = new ClubMembership();
        membership.setMembershipId(resultSet.getInt("id_membresia"));
        membership.setStudentId(resultSet.getInt("id_estudiante"));
        membership.setClubId(resultSet.getInt("id_club"));
        Date joinDate = resultSet.getDate("fecha_ingreso");
        membership.setJoinDate(joinDate == null ? null : joinDate.toLocalDate());
        membership.setMembershipStatus(
                MembershipStatus.fromLabel(resultSet.getString("estado_membresia")));
        Date withdrawalDate = resultSet.getDate("fecha_retiro");
        membership.setWithdrawalDate(withdrawalDate == null ? null : withdrawalDate.toLocalDate());
        membership.setWithdrawalReason(resultSet.getString("motivo_retiro"));
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        membership.setCreationDate(creation == null ? null : creation.toLocalDateTime());
        membership.setStudentFullName(resultSet.getString("nombre_estudiante"));
        membership.setClubName(resultSet.getString("nombre_club"));
        return membership;
    }
}
