package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.EnrollmentStatus;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link EnrollmentDao} for the {@code matriculas} table.
 * Listing queries join {@code estudiantes} and {@code grados} to fill the
 * transient display names.
 */
public class EnrollmentDaoImpl implements EnrollmentDao {

    private static final String SQL_INSERT =
            "INSERT INTO matriculas (id_estudiante, id_grado, año_academico, estado_matricula, "
            + "fecha_matricula, fecha_retiro, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE matriculas SET id_grado = ?, año_academico = ?, estado_matricula = ?, "
            + "fecha_matricula = ?, fecha_retiro = ?, observaciones = ? WHERE id_matricula = ?";

    private static final String SQL_SELECT_BASE =
            "SELECT m.id_matricula, m.id_estudiante, m.id_grado, m.año_academico, m.estado_matricula, "
            + "m.fecha_matricula, m.fecha_retiro, m.observaciones, m.fecha_creacion, "
            + "e.numero_documento, e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido, "
            + "g.nombre_grado "
            + "FROM matriculas m "
            + "JOIN estudiantes e ON m.id_estudiante = e.id_estudiante "
            + "LEFT JOIN grados g ON m.id_grado = g.id_grado ";

    @Override
    public int save(Enrollment enrollment) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {
            fillParameters(statement, enrollment);
            statement.executeUpdate();
            return readGeneratedKey(statement);
        }
    }

    @Override
    public Enrollment findById(int id) throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE m.id_matricula = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Enrollment> findAll() throws SQLException {
        String sql = SQL_SELECT_BASE + "ORDER BY m.año_academico DESC, e.primer_apellido, e.primer_nombre";
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                enrollments.add(mapRow(resultSet));
            }
        }
        return enrollments;
    }

    @Override
    public List<Enrollment> findByStudentId(int studentId) throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE m.id_estudiante = ? ORDER BY m.año_academico DESC";
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollments.add(mapRow(resultSet));
                }
            }
        }
        return enrollments;
    }

    @Override
    public List<Enrollment> findByAcademicYear(int academicYear) throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE m.año_academico = ? ORDER BY e.primer_apellido, e.primer_nombre";
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollments.add(mapRow(resultSet));
                }
            }
        }
        return enrollments;
    }

    @Override
    public boolean existsEnrollment(int studentId, int academicYear) throws SQLException {
        String sql = "SELECT 1 FROM matriculas WHERE id_estudiante = ? AND año_academico = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public boolean update(Enrollment enrollment) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            fillParameters(statement, enrollment);
            statement.setInt(7, enrollment.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM matriculas WHERE id_matricula = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private void fillParameters(PreparedStatement statement, Enrollment enrollment) throws SQLException {
        statement.setInt(1, enrollment.getStudentId());
        statement.setInt(2, enrollment.getGradeId());
        statement.setInt(3, enrollment.getAcademicYear());
        statement.setString(4, enrollment.getStatus() == null
                ? EnrollmentStatus.ACTIVE.toString() : enrollment.getStatus().toString());
        setDate(statement, 5, enrollment.getEnrollmentDate());
        setDate(statement, 6, enrollment.getWithdrawalDate());
        if (enrollment.getObservations() == null || enrollment.getObservations().isBlank()) {
            statement.setNull(7, Types.VARCHAR);
        } else {
            statement.setString(7, enrollment.getObservations());
        }
    }

    private void setDate(PreparedStatement statement, int index, LocalDate date) throws SQLException {
        if (date == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(date));
        }
    }

    private int readGeneratedKey(Statement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            return keys.next() ? keys.getInt(1) : 0;
        }
    }

    private Enrollment mapRow(ResultSet resultSet) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(resultSet.getInt("id_matricula"));
        enrollment.setStudentId(resultSet.getInt("id_estudiante"));
        enrollment.setGradeId(resultSet.getInt("id_grado"));
        enrollment.setAcademicYear(resultSet.getInt("año_academico"));
        enrollment.setStatus(EnrollmentStatus.fromDb(resultSet.getString("estado_matricula")));
        Date enrollmentDate = resultSet.getDate("fecha_matricula");
        enrollment.setEnrollmentDate(enrollmentDate == null ? null : enrollmentDate.toLocalDate());
        Date withdrawalDate = resultSet.getDate("fecha_retiro");
        enrollment.setWithdrawalDate(withdrawalDate == null ? null : withdrawalDate.toLocalDate());
        enrollment.setObservations(resultSet.getString("observaciones"));
        Timestamp createdAt = resultSet.getTimestamp("fecha_creacion");
        enrollment.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        // Transient display names from the joined tables.
        enrollment.setStudentName(joinName(resultSet.getString("primer_nombre"),
                resultSet.getString("segundo_nombre"),
                resultSet.getString("primer_apellido"),
                resultSet.getString("segundo_apellido"))
                + " (" + resultSet.getString("numero_documento") + ")");
        enrollment.setGradeName(resultSet.getString("nombre_grado"));
        return enrollment;
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
