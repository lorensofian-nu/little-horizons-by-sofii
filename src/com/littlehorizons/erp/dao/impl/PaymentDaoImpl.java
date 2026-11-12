package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.model.GuardianSummary;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentMethod;
import com.littlehorizons.erp.model.PaymentPeriod;
import com.littlehorizons.erp.model.StudentSummary;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.DocumentSequenceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** JDBC implementation of {@link PaymentDao}. Opens a fresh connection per operation. */
public class PaymentDaoImpl implements PaymentDao {

    /** Student full name assembled from the four name columns. */
    private static final String STUDENT_NAME_EXPR =
            "TRIM(CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido))";

    /** Guardian full name assembled from the four name columns. */
    private static final String GUARDIAN_NAME_EXPR =
            "TRIM(CONCAT_WS(' ', a.primer_nombre, a.segundo_nombre, a.primer_apellido, a.segundo_apellido))";

    public PaymentDaoImpl() {
        // Connection is obtained per operation; no-op constructor kept for the no-arg chain.
    }

    @Override
    public int save(Payment payment) throws SQLException {
        String sql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, "
                + "metodo_pago, numero_recibo, año_academico, periodo_academico, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, payment.getStudentId());
            if (payment.getGuardianId() != null) {
                statement.setInt(2, payment.getGuardianId());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            statement.setInt(3, payment.getConceptId());
            statement.setBigDecimal(4, payment.getAmountPaid());
            statement.setDate(5, java.sql.Date.valueOf(payment.getPaymentDate()));
            statement.setString(6, payment.getMethod().getLabel());
            statement.setString(7, payment.getReceiptNumber());
            if (payment.getAcademicYear() != null) {
                statement.setInt(8, payment.getAcademicYear());
            } else {
                statement.setNull(8, java.sql.Types.INTEGER);
            }
            statement.setString(9, payment.getPeriod() == null ? null : payment.getPeriod().getLabel());
            statement.setString(10, payment.getNotes());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    payment.setId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    @Override
    public Payment findById(int id) throws SQLException {
        String sql = "SELECT p.*, c.nombre_concepto FROM pagos p "
                + "JOIN conceptos_pago c ON p.id_concepto = c.id_concepto WHERE p.id_pago = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        return queryPayments(
                "SELECT p.*, c.nombre_concepto FROM pagos p "
                + "JOIN conceptos_pago c ON p.id_concepto = c.id_concepto "
                + "ORDER BY p.fecha_pago DESC, p.id_pago DESC", null);
    }

    @Override
    public List<Payment> findByStudent(int studentId) throws SQLException {
        return queryPayments(
                "SELECT p.*, c.nombre_concepto FROM pagos p "
                + "JOIN conceptos_pago c ON p.id_concepto = c.id_concepto "
                + "WHERE p.id_estudiante = ? ORDER BY p.fecha_pago DESC, p.id_pago DESC",
                studentId);
    }

    @Override
    public List<Payment> findByGuardian(int guardianId) throws SQLException {
        return queryPayments(
                "SELECT p.*, c.nombre_concepto, " + STUDENT_NAME_EXPR + " AS nombre_estudiante "
                + "FROM pagos p JOIN conceptos_pago c ON p.id_concepto = c.id_concepto "
                + "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "JOIN estudiante_acudiente ea ON ea.id_estudiante = e.id_estudiante "
                + "WHERE ea.id_acudiente = ? ORDER BY p.fecha_pago DESC, p.id_pago DESC",
                guardianId);
    }

    @Override
    public LocalDate findLastPaymentDate(int studentId, int academicYear) throws SQLException {
        String sql = "SELECT MAX(fecha_pago) FROM pagos WHERE id_estudiante = ? AND año_academico = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    java.sql.Date date = resultSet.getDate(1);
                    return date == null ? null : date.toLocalDate();
                }
            }
        }
        return null;
    }

    @Override
    public Map<Integer, LocalDate> findLastPaymentDatesByYear(int academicYear) throws SQLException {
        String sql = "SELECT id_estudiante, MAX(fecha_pago) FROM pagos WHERE año_academico = ? GROUP BY id_estudiante";
        Map<Integer, LocalDate> lastDates = new HashMap<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, academicYear);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    java.sql.Date date = resultSet.getDate(2);
                    lastDates.put(resultSet.getInt(1), date == null ? null : date.toLocalDate());
                }
            }
        }
        return lastDates;
    }

    @Override
    public String nextReceiptNumber(int academicYear) throws SQLException {
        String prefix = "REC-" + academicYear + "-";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                String next = DocumentSequenceUtil.next(connection, prefix, "receipt");
                connection.commit();
                return next;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    // ------------------------------------------------------------------
    // Read-only helpers shared with other modules (kept dependency-free).
    // ------------------------------------------------------------------

    @Override
    public List<StudentSummary> listActiveStudents() throws SQLException {
        String sql = "SELECT e.id_estudiante, " + STUDENT_NAME_EXPR + " AS nombre_completo "
                + "FROM estudiantes e WHERE e.estado = 'Activo' "
                + "ORDER BY e.primer_apellido, e.primer_nombre, e.id_estudiante";
        List<StudentSummary> students = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                students.add(new StudentSummary(resultSet.getInt(1), resultSet.getString(2)));
            }
        }
        return students;
    }

    @Override
    public StudentSummary findStudentById(int studentId) throws SQLException {
        String sql = "SELECT e.id_estudiante, " + STUDENT_NAME_EXPR + " AS nombre_completo "
                + "FROM estudiantes e WHERE e.id_estudiante = ? AND e.estado = 'Activo'";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? new StudentSummary(resultSet.getInt(1), resultSet.getString(2))
                        : null;
            }
        }
    }

    @Override
    public List<GuardianSummary> listGuardians() throws SQLException {
        return queryGuardians(
                "SELECT a.id_acudiente, a.tipo_documento, a.numero_documento, " + GUARDIAN_NAME_EXPR
                + " AS nombre_completo, a.direccion FROM acudientes a "
                + "ORDER BY a.primer_apellido, a.primer_nombre, a.id_acudiente", null);
    }

    @Override
    public List<GuardianSummary> findGuardiansByStudent(int studentId) throws SQLException {
        return queryGuardians(
                "SELECT a.id_acudiente, a.tipo_documento, a.numero_documento, " + GUARDIAN_NAME_EXPR
                + " AS nombre_completo, a.direccion FROM acudientes a "
                + "JOIN estudiante_acudiente ea ON ea.id_acudiente = a.id_acudiente "
                + "WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC, a.id_acudiente", studentId);
    }

    @Override
    public GuardianSummary findGuardianById(int guardianId) throws SQLException {
        return queryGuardians(
                "SELECT a.id_acudiente, a.tipo_documento, a.numero_documento, " + GUARDIAN_NAME_EXPR
                + " AS nombre_completo, a.direccion FROM acudientes a WHERE a.id_acudiente = ?", guardianId)
                .stream().findFirst().orElse(null);
    }

    // ------------------------------------------------------------------
    // Internal helpers.
    // ------------------------------------------------------------------

    private List<Payment> queryPayments(String sql, Integer parameter) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setInt(1, parameter);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Payment payment = mapRow(resultSet);
                    payment.setConceptName(resultSet.getString("nombre_concepto"));
                    payments.add(payment);
                }
            }
        }
        return payments;
    }

    private Payment mapRow(ResultSet resultSet) throws SQLException {
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        Integer guardianId = resultSet.getInt("id_acudiente");
        if (resultSet.wasNull()) {
            guardianId = null;
        }
        Integer academicYear = resultSet.getInt("año_academico");
        if (resultSet.wasNull()) {
            academicYear = null;
        }
        String periodLabel = resultSet.getString("periodo_academico");
        Payment payment = new Payment();
        payment.setId(resultSet.getInt("id_pago"));
        payment.setStudentId(resultSet.getInt("id_estudiante"));
        payment.setGuardianId(guardianId);
        payment.setConceptId(resultSet.getInt("id_concepto"));
        payment.setAmountPaid(resultSet.getBigDecimal("monto_pagado"));
        java.sql.Date paymentDate = resultSet.getDate("fecha_pago");
        payment.setPaymentDate(paymentDate == null ? null : paymentDate.toLocalDate());
        payment.setMethod(PaymentMethod.fromLabel(resultSet.getString("metodo_pago")));
        payment.setReceiptNumber(resultSet.getString("numero_recibo"));
        payment.setAcademicYear(academicYear);
        payment.setPeriod(PaymentPeriod.fromLabel(periodLabel));
        payment.setNotes(resultSet.getString("notas"));
        payment.setCreationDate(creation == null ? null : creation.toLocalDateTime());
        return payment;
    }

    private List<GuardianSummary> queryGuardians(String sql, Integer parameter) throws SQLException {
        List<GuardianSummary> guardians = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setInt(1, parameter);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    guardians.add(new GuardianSummary(
                            resultSet.getInt("id_acudiente"),
                            resultSet.getString("tipo_documento"),
                            resultSet.getString("numero_documento"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("direccion")));
                }
            }
        }
        return guardians;
    }
}
