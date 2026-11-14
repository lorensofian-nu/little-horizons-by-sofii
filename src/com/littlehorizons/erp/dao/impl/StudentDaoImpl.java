package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.DocumentType;
import com.littlehorizons.erp.model.Gender;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentStatus;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.model.PageRequest;
import com.littlehorizons.erp.model.PageResult;
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
 * JDBC implementation of {@link StudentDao} for the {@code estudiantes} table.
 */
public class StudentDaoImpl implements StudentDao {

    private static final String SQL_INSERT =
            "INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
            + "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, "
            + "correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado, "
            + "id_acudiente_principal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE estudiantes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, "
            + "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, fecha_nacimiento = ?, "
            + "genero = ?, direccion = ?, telefono = ?, correo_electronico = ?, tipo_sangre = ?, "
            + "alergias = ?, condiciones_medicas = ?, fecha_ingreso = ?, estado = ?, "
            + "id_acudiente_principal = ? "
            + "WHERE id_estudiante = ?";

    private static final String SQL_SELECT_BASE =
            "SELECT id_estudiante, tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
            + "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, "
            + "correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado, "
            + "id_acudiente_principal, fecha_creacion, fecha_actualizacion FROM estudiantes ";

    @Override
    public int save(Student student) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {
            fillInsertParameters(statement, student);
            statement.executeUpdate();
            return readGeneratedKey(statement);
        }
    }

    @Override
    public Student findById(int id) throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE id_estudiante = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Student> findAll() throws SQLException {
        String sql = SQL_SELECT_BASE + "ORDER BY primer_apellido, primer_nombre, id_estudiante";
        List<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                students.add(mapRow(resultSet));
            }
        }
        return students;
    }

    @Override
    public Student findByDocumentNumber(String documentNumber) throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE numero_documento = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, documentNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Student> searchByNameOrDocument(String term) throws SQLException {
        String sql = SQL_SELECT_BASE
                + "WHERE CONCAT(primer_nombre, ' ', IFNULL(segundo_nombre, ''), ' ', primer_apellido, ' ', "
                + "IFNULL(segundo_apellido, '')) LIKE ? OR numero_documento LIKE ? "
                + "ORDER BY primer_apellido, primer_nombre";
        String pattern = "%" + term + "%";
        List<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    students.add(mapRow(resultSet));
                }
            }
        }
        return students;
    }

    @Override
    public PageResult<Student> findPage(PageRequest request) throws SQLException {
        String sql = SQL_SELECT_BASE + "ORDER BY primer_apellido, primer_nombre, id_estudiante "
                + "LIMIT ? OFFSET ?";
        return readPage(sql, request);
    }

    @Override
    public PageResult<Student> searchPage(String term, PageRequest request) throws SQLException {
        String sql = SQL_SELECT_BASE
                + "WHERE CONCAT(primer_nombre, ' ', IFNULL(segundo_nombre, ''), ' ', primer_apellido, ' ', "
                + "IFNULL(segundo_apellido, '')) LIKE ? OR numero_documento LIKE ? "
                + "ORDER BY primer_apellido, primer_nombre, id_estudiante LIMIT ? OFFSET ?";
        String pattern = "%" + term + "%";
        List<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setInt(3, request.pageSize() + 1);
            statement.setInt(4, request.offset());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    students.add(mapRow(resultSet));
                }
            }
        }
        return page(students, request);
    }

    private PageResult<Student> readPage(String sql, PageRequest request) throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, request.pageSize() + 1);
            statement.setInt(2, request.offset());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    students.add(mapRow(resultSet));
                }
            }
        }
        return page(students, request);
    }

    private PageResult<Student> page(List<Student> students, PageRequest request) {
        boolean hasNext = students.size() > request.pageSize();
        if (hasNext) {
            students.remove(students.size() - 1);
        }
        return new PageResult<>(students, request, hasNext);
    }

    @Override
    public boolean update(Student student) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            fillInsertParameters(statement, student);
            statement.setInt(18, student.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id_estudiante = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM estudiantes";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    @Override
    public List<StudentSummary> findActiveSummaries() throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE estado = 'Activo' ORDER BY primer_apellido, primer_nombre";
        List<StudentSummary> summaries = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Student student = mapRow(resultSet);
                summaries.add(new StudentSummary(student.getId(), student.getFullName()));
            }
        }
        return summaries;
    }

    private void fillInsertParameters(PreparedStatement statement, Student student) throws SQLException {
        statement.setString(1, student.getDocumentType() == null
                ? DocumentType.TI.toString() : student.getDocumentType().toString());
        statement.setString(2, student.getDocumentNumber());
        statement.setString(3, student.getFirstName());
        setNullableString(statement, 4, student.getSecondName());
        statement.setString(5, student.getLastName());
        setNullableString(statement, 6, student.getSecondLastName());
        setDate(statement, 7, student.getBirthDate());
        statement.setString(8, student.getGender() == null
                ? Gender.MALE.toString() : student.getGender().toString());
        setNullableString(statement, 9, student.getAddress());
        setNullableString(statement, 10, student.getPhone());
        setNullableString(statement, 11, student.getEmail());
        setNullableString(statement, 12, student.getBloodType());
        setNullableString(statement, 13, student.getAllergies());
        setNullableString(statement, 14, student.getMedicalConditions());
        setDate(statement, 15, student.getAdmissionDate());
        statement.setString(16, student.getStatus() == null
                ? StudentStatus.ACTIVE.toString() : student.getStatus().toString());
        setNullableInt(statement, 17, student.getPrimaryGuardianId());
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setNullableString(PreparedStatement statement, int index, String value)
            throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
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

    private Student mapRow(ResultSet resultSet) throws SQLException {
        Student student = new Student();
        student.setId(resultSet.getInt("id_estudiante"));
        student.setDocumentType(DocumentType.fromDb(resultSet.getString("tipo_documento")));
        student.setDocumentNumber(resultSet.getString("numero_documento"));
        student.setFirstName(resultSet.getString("primer_nombre"));
        student.setSecondName(resultSet.getString("segundo_nombre"));
        student.setLastName(resultSet.getString("primer_apellido"));
        student.setSecondLastName(resultSet.getString("segundo_apellido"));
        Date birthDate = resultSet.getDate("fecha_nacimiento");
        student.setBirthDate(birthDate == null ? null : birthDate.toLocalDate());
        student.setGender(Gender.fromDb(resultSet.getString("genero")));
        student.setAddress(resultSet.getString("direccion"));
        student.setPhone(resultSet.getString("telefono"));
        student.setEmail(resultSet.getString("correo_electronico"));
        student.setBloodType(resultSet.getString("tipo_sangre"));
        student.setAllergies(resultSet.getString("alergias"));
        student.setMedicalConditions(resultSet.getString("condiciones_medicas"));
        Date admissionDate = resultSet.getDate("fecha_ingreso");
        student.setAdmissionDate(admissionDate == null ? null : admissionDate.toLocalDate());
        student.setStatus(StudentStatus.fromDb(resultSet.getString("estado")));
        int primaryGuardianId = resultSet.getInt("id_acudiente_principal");
        student.setPrimaryGuardianId(resultSet.wasNull() ? null : primaryGuardianId);
        Timestamp createdAt = resultSet.getTimestamp("fecha_creacion");
        student.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        Timestamp updatedAt = resultSet.getTimestamp("fecha_actualizacion");
        student.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        return student;
    }
}
