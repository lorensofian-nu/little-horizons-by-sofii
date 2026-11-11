package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.model.EnrollmentPeriod;
import com.littlehorizons.erp.model.AcademicStudentSummary;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.SubjectEnrollmentStatus;
import com.littlehorizons.erp.model.AcademicTeacherSummary;
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

/**
 * JDBC implementation of {@link SubjectEnrollmentDao} for the
 * {@code inscripciones_materia} table, including the lightweight
 * student/teacher lookup helpers.
 */
public class SubjectEnrollmentDaoImpl implements SubjectEnrollmentDao {

    private static final String SELECT_BASE = """
            SELECT id_inscripcion, id_estudiante, id_materia, año_academico,
                   periodo_academico, seccion, fecha_inscripcion, estado, fecha_creacion
            FROM inscripciones_materia
            """;

    @Override
    public int save(SubjectEnrollment enrollment) throws SQLException {
        String sql = """
                INSERT INTO inscripciones_materia
                    (id_estudiante, id_materia, año_academico, periodo_academico,
                     seccion, fecha_inscripcion, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, enrollment.getStudentId());
            statement.setInt(2, enrollment.getSubjectId());
            statement.setInt(3, enrollment.getAcademicYear());
            statement.setString(4, enrollment.getPeriod().getLabel());
            statement.setString(5, enrollment.getSection());
            statement.setDate(6, Date.valueOf(enrollment.getEnrollmentDate()));
            statement.setString(7, enrollment.getStatus().getLabel());
            statement.executeUpdate();
            return generateId(statement);
        }
    }

    @Override
    public SubjectEnrollment findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_inscripcion = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<SubjectEnrollment> findAll() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY año_academico DESC, id_inscripcion";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    @Override
    public List<SubjectEnrollment> findByStudent(int studentId) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_estudiante = ? ORDER BY año_academico, periodo_academico";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public List<SubjectEnrollment> findByStudentAndYear(int studentId, int academicYear) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_estudiante = ? AND año_academico = ? ORDER BY periodo_academico";
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
    public List<SubjectEnrollment> findBySubjectAndYear(int subjectId, int academicYear) throws SQLException {
        String sql = SELECT_BASE + " WHERE id_materia = ? AND año_academico = ? ORDER BY id_inscripcion";
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
    public List<SubjectEnrollment> findByYearAndPeriod(int academicYear, EnrollmentPeriod period) throws SQLException {
        String sql = SELECT_BASE + " WHERE año_academico = ? AND periodo_academico = ? ORDER BY id_inscripcion";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, academicYear);
            statement.setString(2, period.getLabel());
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    @Override
    public boolean update(SubjectEnrollment enrollment) throws SQLException {
        String sql = """
                UPDATE inscripciones_materia
                SET id_estudiante = ?, id_materia = ?, año_academico = ?, periodo_academico = ?,
                    seccion = ?, fecha_inscripcion = ?, estado = ?
                WHERE id_inscripcion = ?
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, enrollment.getStudentId());
            statement.setInt(2, enrollment.getSubjectId());
            statement.setInt(3, enrollment.getAcademicYear());
            statement.setString(4, enrollment.getPeriod().getLabel());
            statement.setString(5, enrollment.getSection());
            statement.setDate(6, Date.valueOf(enrollment.getEnrollmentDate()));
            statement.setString(7, enrollment.getStatus().getLabel());
            statement.setInt(8, enrollment.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM inscripciones_materia WHERE id_inscripcion = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsFor(int studentId, int subjectId, int academicYear,
                             EnrollmentPeriod period, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? """
                  SELECT COUNT(*) FROM inscripciones_materia
                  WHERE id_estudiante = ? AND id_materia = ? AND año_academico = ? AND periodo_academico = ?
                  """
                : """
                  SELECT COUNT(*) FROM inscripciones_materia
                  WHERE id_estudiante = ? AND id_materia = ? AND año_academico = ? AND periodo_academico = ?
                  AND id_inscripcion <> ?
                  """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, subjectId);
            statement.setInt(3, academicYear);
            statement.setString(4, period.getLabel());
            if (excludeId != null) {
                statement.setInt(5, excludeId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public List<AcademicStudentSummary> listActiveStudents(int academicYear) throws SQLException {
        String sql = """
                SELECT DISTINCT e.id_estudiante,
                       CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre,
                                 e.primer_apellido, e.segundo_apellido) AS nombre_completo,
                       m.id_grado
                FROM estudiantes e
                JOIN matriculas m ON m.id_estudiante = e.id_estudiante
                WHERE m.año_academico = ? AND m.estado_matricula = 'Activa'
                ORDER BY nombre_completo
                """;
        List<AcademicStudentSummary> students = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    students.add(new AcademicStudentSummary(
                            resultSet.getInt("id_estudiante"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getInt("id_grado")));
                }
            }
            return students;
        }
    }

    @Override
    public List<AcademicTeacherSummary> listActiveTeachers() throws SQLException {
        String sql = """
                SELECT p.id_profesor,
                       CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre,
                                 p.primer_apellido, p.segundo_apellido) AS nombre_completo,
                       p.especializacion
                FROM profesores p
                WHERE p.estado = 'Activo'
                ORDER BY nombre_completo
                """;
        List<AcademicTeacherSummary> teachers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                teachers.add(new AcademicTeacherSummary(
                        resultSet.getInt("id_profesor"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("especializacion")));
            }
            return teachers;
        }
    }

    @Override
    public boolean hasActiveEnrollment(int studentId, int academicYear) throws SQLException {
        String sql = """
                SELECT EXISTS(
                    SELECT 1 FROM matriculas
                    WHERE id_estudiante = ? AND año_academico = ? AND estado_matricula = 'Activa'
                ) AS activo
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean("activo");
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
        LoggerUtil.logError("SubjectEnrollmentDaoImpl.save", new SQLException("No generated key returned"));
        return 0;
    }

    private List<SubjectEnrollment> mapList(ResultSet resultSet) throws SQLException {
        List<SubjectEnrollment> enrollments = new ArrayList<>();
        while (resultSet.next()) {
            enrollments.add(mapRow(resultSet));
        }
        return enrollments;
    }

    private SubjectEnrollment mapRow(ResultSet resultSet) throws SQLException {
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        LocalDateTime creationDate = creation != null ? creation.toLocalDateTime() : null;
        Date enrollment = resultSet.getDate("fecha_inscripcion");
        LocalDate enrollmentDate = enrollment != null ? enrollment.toLocalDate() : LocalDate.now();
        return new SubjectEnrollment(
                resultSet.getInt("id_inscripcion"),
                resultSet.getInt("id_estudiante"),
                resultSet.getInt("id_materia"),
                resultSet.getInt("año_academico"),
                EnrollmentPeriod.fromLabel(resultSet.getString("periodo_academico")),
                resultSet.getString("seccion"),
                enrollmentDate,
                SubjectEnrollmentStatus.fromLabel(resultSet.getString("estado")),
                creationDate);
    }
}
