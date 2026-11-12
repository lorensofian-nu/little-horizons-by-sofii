package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherDocumentType;
import com.littlehorizons.erp.model.TeacherStatus;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link TeacherDao} for the {@code profesores} table.
 * All statements use {@link PreparedStatement} and try-with-resources.
 */
public class TeacherDaoImpl implements TeacherDao {

    private static final String SELECT_COLUMNS =
            "id_profesor, tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                    + "primer_apellido, segundo_apellido, correo_electronico, telefono, "
                    + "especializacion, fecha_contratacion, estado, fecha_creacion, fecha_actualizacion";

    @Override
    public int save(Teacher teacher) throws SQLException {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, "
                + "segundo_nombre, primer_apellido, segundo_apellido, correo_electronico, telefono, "
                + "especializacion, fecha_contratacion, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, teacher.getDocumentType().getValue());
            statement.setString(2, teacher.getDocumentNumber());
            statement.setString(3, teacher.getFirstName());
            statement.setString(4, teacher.getSecondName());
            statement.setString(5, teacher.getLastName());
            statement.setString(6, teacher.getSecondLastName());
            statement.setString(7, teacher.getEmail());
            statement.setString(8, teacher.getPhone());
            statement.setString(9, teacher.getSpecialization());
            statement.setDate(10, java.sql.Date.valueOf(teacher.getHireDate()));
            statement.setString(11, teacher.getStatus().getValue());
            statement.executeUpdate();
            return readGeneratedKey(statement);
        }
    }

    @Override
    public Optional<Teacher> findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM profesores WHERE id_profesor = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Teacher> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM profesores "
                + "ORDER BY primer_apellido, primer_nombre";
        return queryList(sql, statement -> { });
    }

    @Override
    public List<Teacher> listActiveTeachers() throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM profesores "
                + "WHERE estado = 'Activo' ORDER BY primer_apellido, primer_nombre";
        return queryList(sql, statement -> { });
    }

    @Override
    public boolean update(Teacher teacher) throws SQLException {
        String sql = "UPDATE profesores SET tipo_documento = ?, numero_documento = ?, "
                + "primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, "
                + "correo_electronico = ?, telefono = ?, especializacion = ?, "
                + "fecha_contratacion = ?, estado = ? WHERE id_profesor = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, teacher.getDocumentType().getValue());
            statement.setString(2, teacher.getDocumentNumber());
            statement.setString(3, teacher.getFirstName());
            statement.setString(4, teacher.getSecondName());
            statement.setString(5, teacher.getLastName());
            statement.setString(6, teacher.getSecondLastName());
            statement.setString(7, teacher.getEmail());
            statement.setString(8, teacher.getPhone());
            statement.setString(9, teacher.getSpecialization());
            statement.setDate(10, java.sql.Date.valueOf(teacher.getHireDate()));
            statement.setString(11, teacher.getStatus().getValue());
            statement.setInt(12, teacher.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int teacherId, TeacherStatus status) throws SQLException {
        String sql = "UPDATE profesores SET estado = ? WHERE id_profesor = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.getValue());
            statement.setInt(2, teacherId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM profesores WHERE id_profesor = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Teacher> findByDocument(String documentNumber) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM profesores WHERE numero_documento = ?";
        return querySingle(sql, documentNumber);
    }

    @Override
    public Optional<Teacher> findByEmail(String email) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM profesores WHERE correo_electronico = ?";
        return querySingle(sql, email);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    private List<Teacher> queryList(String sql, StatementBinder binder) throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    teachers.add(mapRow(resultSet));
                }
            }
        }
        return teachers;
    }

    private Optional<Teacher> querySingle(String sql, String parameter) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    private int readGeneratedKey(Statement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("Insert into profesores did not return a generated key.");
    }

    private Teacher mapRow(ResultSet resultSet) throws SQLException {
        TeacherDocumentType documentType =
                TeacherDocumentType.fromValue(resultSet.getString("tipo_documento"));
        TeacherStatus status = TeacherStatus.fromValue(resultSet.getString("estado"));
        LocalDate hireDate = resultSet.getDate("fecha_contratacion").toLocalDate();
        Teacher teacher = new Teacher(
                resultSet.getInt("id_profesor"),
                documentType,
                resultSet.getString("numero_documento"),
                resultSet.getString("primer_nombre"),
                resultSet.getString("segundo_nombre"),
                resultSet.getString("primer_apellido"),
                resultSet.getString("segundo_apellido"),
                resultSet.getString("correo_electronico"),
                resultSet.getString("telefono"),
                resultSet.getString("especializacion"),
                hireDate,
                status,
                toLocalDateTime(resultSet.getTimestamp("fecha_creacion")),
                toLocalDateTime(resultSet.getTimestamp("fecha_actualizacion")));
        return teacher;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
