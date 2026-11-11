package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.StudentGuardianDao;
import com.littlehorizons.erp.model.StudentGuardianLink;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link StudentGuardianDao} for the
 * {@code estudiante_acudiente} link table.
 */
public class StudentGuardianDaoImpl implements StudentGuardianDao {

    private static final String SQL_ASSIGN =
            "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, "
            + "es_contacto_emergencia) VALUES (?, ?, ?, ?)";

    private static final String SQL_REMOVE =
            "DELETE FROM estudiante_acudiente WHERE id_estudiante = ? AND id_acudiente = ?";

    private static final String SQL_EXISTS =
            "SELECT 1 FROM estudiante_acudiente WHERE id_estudiante = ? AND id_acudiente = ?";

    private static final String SQL_GUARDIANS_OF_STUDENT =
            "SELECT ea.id_estudiante, ea.id_acudiente, ea.es_principal, ea.es_contacto_emergencia, "
            + "ea.fecha_creacion, a.primer_nombre, a.segundo_nombre, a.primer_apellido, "
            + "a.segundo_apellido, a.parentesco "
            + "FROM estudiante_acudiente ea JOIN acudientes a ON ea.id_acudiente = a.id_acudiente "
            + "WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC, a.primer_apellido";

    private static final String SQL_STUDENTS_OF_GUARDIAN =
            "SELECT ea.id_estudiante, ea.id_acudiente, ea.es_principal, ea.es_contacto_emergencia, "
            + "ea.fecha_creacion, e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido "
            + "FROM estudiante_acudiente ea JOIN estudiantes e ON ea.id_estudiante = e.id_estudiante "
            + "WHERE ea.id_acudiente = ? ORDER BY e.primer_apellido, e.primer_nombre";

    @Override
    public boolean assign(StudentGuardianLink link) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_ASSIGN)) {
            statement.setInt(1, link.getStudentId());
            statement.setInt(2, link.getGuardianId());
            statement.setBoolean(3, link.isPrimaryContact());
            statement.setBoolean(4, link.isEmergencyContact());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeLink(int studentId, int guardianId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_REMOVE)) {
            statement.setInt(1, studentId);
            statement.setInt(2, guardianId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsLink(int studentId, int guardianId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_EXISTS)) {
            statement.setInt(1, studentId);
            statement.setInt(2, guardianId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public List<StudentGuardianLink> findGuardiansOfStudent(int studentId) throws SQLException {
        List<StudentGuardianLink> links = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_GUARDIANS_OF_STUDENT)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    StudentGuardianLink link = mapLink(resultSet);
                    link.setGuardianName(buildGuardianName(resultSet));
                    link.setGuardianRelationship(resultSet.getString("parentesco"));
                    links.add(link);
                }
            }
        }
        return links;
    }

    @Override
    public List<StudentGuardianLink> findStudentsOfGuardian(int guardianId) throws SQLException {
        List<StudentGuardianLink> links = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_STUDENTS_OF_GUARDIAN)) {
            statement.setInt(1, guardianId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    StudentGuardianLink link = mapLink(resultSet);
                    link.setStudentName(buildStudentName(resultSet));
                    links.add(link);
                }
            }
        }
        return links;
    }

    @Override
    public int countGuardiansForStudent(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM estudiante_acudiente WHERE id_estudiante = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    private StudentGuardianLink mapLink(ResultSet resultSet) throws SQLException {
        StudentGuardianLink link = new StudentGuardianLink();
        link.setStudentId(resultSet.getInt("id_estudiante"));
        link.setGuardianId(resultSet.getInt("id_acudiente"));
        link.setPrimaryContact(resultSet.getBoolean("es_principal"));
        link.setEmergencyContact(resultSet.getBoolean("es_contacto_emergencia"));
        Timestamp createdAt = resultSet.getTimestamp("fecha_creacion");
        link.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return link;
    }

    /** Guardian name from the joined acudientes columns. */
    private String buildGuardianName(ResultSet resultSet) throws SQLException {
        return joinName(resultSet.getString("primer_nombre"), resultSet.getString("segundo_nombre"),
                resultSet.getString("primer_apellido"), resultSet.getString("segundo_apellido"));
    }

    /** Student name from the joined estudiantes columns. */
    private String buildStudentName(ResultSet resultSet) throws SQLException {
        return joinName(resultSet.getString("primer_nombre"), resultSet.getString("segundo_nombre"),
                resultSet.getString("primer_apellido"), resultSet.getString("segundo_apellido"));
    }

    private String joinName(String first, String second, String third, String fourth) {
        StringBuilder name = new StringBuilder();
        for (String part : new String[]{first, second, third, fourth}) {
            if (part != null && !part.isBlank()) {
                if (name.length() > 0) {
                    name.append(' ');
                }
                name.append(part);
            }
        }
        return name.toString();
    }
}
