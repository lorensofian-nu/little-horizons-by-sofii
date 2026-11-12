package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.ReportDao;
import com.littlehorizons.erp.model.AttendanceRecord;
import com.littlehorizons.erp.model.ClubOccupancyReport;
import com.littlehorizons.erp.model.EventParticipationRecord;
import com.littlehorizons.erp.model.GradeRecord;
import com.littlehorizons.erp.model.LastPaymentRecord;
import com.littlehorizons.erp.model.LoanOverdueReport;
import com.littlehorizons.erp.model.PaymentRecord;
import com.littlehorizons.erp.model.ReportRow;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only JDBC implementation of {@link ReportDao}.
 *
 * <p>Every query is a SELECT executed with a {@link PreparedStatement} inside
 * try-with-resources. This module never modifies data.</p>
 */
public class ReportDaoImpl implements ReportDao {

    /** SQL fragment that builds the full name of a person aliased as "e". */
    private static final String FULL_NAME_E =
            "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, "
                    + "e.primer_apellido, e.segundo_apellido)";

    /** SQL fragment that builds the full name of a person aliased as "p". */
    private static final String FULL_NAME_P =
            "CONCAT_WS(' ', p.primer_nombre, p.segundo_nombre, "
                    + "p.primer_apellido, p.segundo_apellido)";

    private static final String COUNT_ACTIVE_STUDENTS =
            "SELECT COUNT(*) FROM estudiantes WHERE estado = 'Activo'";

    private static final String COUNT_ACTIVE_TEACHERS =
            "SELECT COUNT(*) FROM profesores WHERE estado = 'Activo'";

    private static final String COUNT_ACTIVE_SUBJECTS =
            "SELECT COUNT(*) FROM materias WHERE esta_activa = TRUE";

    private static final String COUNT_ACTIVE_CLUBS =
            "SELECT COUNT(*) FROM clubes WHERE esta_activo = TRUE";

    private static final String TOTAL_PAYMENTS_IN_YEAR =
            "SELECT COALESCE(SUM(monto_pagado), 0) FROM pagos "
                    + "WHERE fecha_pago >= ? AND fecha_pago < ?";

    private static final String COUNT_RESOURCES_BY_STATE =
            "SELECT estado_recurso, COUNT(*) FROM recursos "
                    + "GROUP BY estado_recurso ORDER BY estado_recurso";

    private static final String FIND_GRADES_BY_YEAR =
            "SELECT r.id_estudiante, " + FULL_NAME_E + " AS nombre_completo, "
                    + "g.nombre_grado, m.nombre_materia, "
                    + "r.periodo_academico, r.valor_nota "
                    + "FROM registros_academicos r "
                    + "JOIN estudiantes e ON e.id_estudiante = r.id_estudiante "
                    + "JOIN materias m ON m.id_materia = r.id_materia "
                    + "JOIN matriculas mt ON mt.id_estudiante = r.id_estudiante "
                    + "AND mt.año_academico = r.año_academico "
                    + "JOIN grados g ON g.id_grado = mt.id_grado "
                    + "WHERE r.año_academico = ? "
                    + "ORDER BY nombre_completo, m.nombre_materia, "
                    + "r.periodo_academico";

    private static final String FIND_ATTENDANCE_BY_YEAR =
            "SELECT a.id_estudiante, " + FULL_NAME_E + " AS nombre_completo, "
                    + "a.fecha_asistencia, a.estado_asistencia "
                    + "FROM asistencias a "
                    + "JOIN estudiantes e ON e.id_estudiante = a.id_estudiante "
                    + "WHERE a.fecha_asistencia >= ? AND a.fecha_asistencia < ? "
                    + "ORDER BY nombre_completo, a.fecha_asistencia";

    private static final String FIND_PAYMENTS_BY_YEAR =
            "SELECT p.id_estudiante, " + FULL_NAME_E + " AS nombre_completo, "
                    + "c.nombre_concepto, p.monto_pagado, p.fecha_pago "
                    + "FROM pagos p "
                    + "JOIN estudiantes e ON e.id_estudiante = p.id_estudiante "
                    + "JOIN conceptos_pago c ON c.id_concepto = p.id_concepto "
                    + "WHERE p.fecha_pago >= ? AND p.fecha_pago < ? "
                    + "ORDER BY nombre_completo, p.fecha_pago";

    private static final String FIND_LAST_PAYMENT_DATES =
            "SELECT e.id_estudiante, " + FULL_NAME_E + " AS nombre_completo, "
                    + "MAX(p.fecha_pago) AS ultimo_pago "
                    + "FROM estudiantes e "
                    + "LEFT JOIN pagos p ON p.id_estudiante = e.id_estudiante "
                    + "WHERE e.estado = 'Activo' "
                    + "GROUP BY e.id_estudiante, e.primer_nombre, "
                    + "e.segundo_nombre, e.primer_apellido, e.segundo_apellido "
                    + "ORDER BY nombre_completo";

    private static final String FIND_CLUB_OCCUPANCY =
            "SELECT c.nombre_club, "
                    + "COALESCE(" + FULL_NAME_P + ", 'Sin supervisor') AS supervisor, "
                    + "c.capacidad_maxima, COUNT(m.id_membresia) AS activos "
                    + "FROM clubes c "
                    + "LEFT JOIN membresias_club m "
                    + "ON m.id_club = c.id_club AND m.estado_membresia = 'Activa' "
                    + "LEFT JOIN profesores p "
                    + "ON p.id_profesor = c.id_profesor_supervisor "
                    + "WHERE c.esta_activo = TRUE "
                    + "GROUP BY c.id_club, c.nombre_club, p.primer_nombre, "
                    + "p.segundo_nombre, p.primer_apellido, p.segundo_apellido, "
                    + "c.capacidad_maxima "
                    + "ORDER BY c.nombre_club";

    private static final String FIND_EVENT_PARTICIPATIONS =
            "SELECT ev.nombre_evento, ev.fecha_evento, ev.tiene_competencia, "
                    + "e.id_estudiante, " + FULL_NAME_E + " AS nombre_completo, "
                    + "pe.rol_participacion, pe.posicion_obtenida "
                    + "FROM participantes_evento pe "
                    + "JOIN eventos ev ON ev.id_evento = pe.id_evento "
                    + "JOIN estudiantes e ON e.id_estudiante = pe.id_estudiante "
                    + "WHERE ev.fecha_evento >= ? AND ev.fecha_evento < ? "
                    + "ORDER BY ev.fecha_evento, nombre_completo";

    private static final String COUNT_LOANS_BY_STATE =
            "SELECT estado_prestamo, COUNT(*) FROM ( "
                    + "SELECT estado_prestamo FROM prestamos_estudiante "
                    + "UNION ALL "
                    + "SELECT estado_prestamo FROM prestamos_profesor "
                    + ") AS todos_los_prestamos "
                    + "GROUP BY estado_prestamo ORDER BY estado_prestamo";

    private static final String FIND_OVERDUE_LOANS =
            "SELECT 'Estudiante' AS tipo_prestamo, r.nombre_recurso, "
                    + FULL_NAME_E + " AS titular, pl.fecha_prestamo, "
                    + "pl.fecha_devolucion_esperada "
                    + "FROM prestamos_estudiante pl "
                    + "JOIN recursos r ON r.id_recurso = pl.id_recurso "
                    + "JOIN estudiantes e ON e.id_estudiante = pl.id_estudiante "
                    + "WHERE pl.estado_prestamo = 'Activo' "
                    + "AND pl.fecha_devolucion_esperada < ? "
                    + "UNION ALL "
                    + "SELECT 'Profesor' AS tipo_prestamo, r.nombre_recurso, "
                    + FULL_NAME_P + " AS titular, pl.fecha_prestamo, "
                    + "pl.fecha_devolucion_esperada "
                    + "FROM prestamos_profesor pl "
                    + "JOIN recursos r ON r.id_recurso = pl.id_recurso "
                    + "JOIN profesores p ON p.id_profesor = pl.id_profesor "
                    + "WHERE pl.estado_prestamo = 'Activo' "
                    + "AND pl.fecha_devolucion_esperada < ? "
                    + "ORDER BY fecha_devolucion_esperada, titular";

    private static final String COUNT_INVENTORY_BY_CATEGORY_AND_STATE =
            "SELECT CONCAT(c.nombre_categoria, ' - ', r.estado_recurso), "
                    + "COUNT(*) "
                    + "FROM recursos r "
                    + "JOIN categorias_recurso c ON c.id_categoria = r.id_categoria "
                    + "GROUP BY c.nombre_categoria, r.estado_recurso "
                    + "ORDER BY c.nombre_categoria, r.estado_recurso";

    @Override
    public int countActiveStudents() throws SQLException {
        return executeSingleCount(COUNT_ACTIVE_STUDENTS);
    }

    @Override
    public int countActiveTeachers() throws SQLException {
        return executeSingleCount(COUNT_ACTIVE_TEACHERS);
    }

    @Override
    public int countActiveSubjects() throws SQLException {
        return executeSingleCount(COUNT_ACTIVE_SUBJECTS);
    }

    @Override
    public int countActiveClubs() throws SQLException {
        return executeSingleCount(COUNT_ACTIVE_CLUBS);
    }

    @Override
    public double totalPaymentsInYear(int year) throws SQLException {
        Date start = Date.valueOf(LocalDate.of(year, 1, 1));
        Date end = Date.valueOf(LocalDate.of(year + 1, 1, 1));
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, TOTAL_PAYMENTS_IN_YEAR, start, end);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getDouble(1);
        }
    }

    @Override
    public List<ReportRow> countResourcesByState() throws SQLException {
        return executeLabelCountQuery(COUNT_RESOURCES_BY_STATE);
    }

    @Override
    public List<GradeRecord> findGradesByYear(int year) throws SQLException {
        List<GradeRecord> records = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, FIND_GRADES_BY_YEAR, year);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                records.add(new GradeRecord(
                        resultSet.getLong("id_estudiante"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("nombre_grado"),
                        resultSet.getString("nombre_materia"),
                        resultSet.getString("periodo_academico"),
                        resultSet.getDouble("valor_nota")));
            }
        }
        return records;
    }

    @Override
    public List<AttendanceRecord> findAttendanceByYear(int year) throws SQLException {
        List<AttendanceRecord> records = new ArrayList<>();
        Date start = Date.valueOf(LocalDate.of(year, 1, 1));
        Date end = Date.valueOf(LocalDate.of(year + 1, 1, 1));
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, FIND_ATTENDANCE_BY_YEAR, start, end);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Date attendanceDate = resultSet.getDate("fecha_asistencia");
                records.add(new AttendanceRecord(
                        resultSet.getLong("id_estudiante"),
                        resultSet.getString("nombre_completo"),
                        attendanceDate.toLocalDate(),
                        resultSet.getString("estado_asistencia")));
            }
        }
        return records;
    }

    @Override
    public List<PaymentRecord> findPaymentsByYear(int year) throws SQLException {
        List<PaymentRecord> records = new ArrayList<>();
        Date start = Date.valueOf(LocalDate.of(year, 1, 1));
        Date end = Date.valueOf(LocalDate.of(year + 1, 1, 1));
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, FIND_PAYMENTS_BY_YEAR, start, end);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Date paymentDate = resultSet.getDate("fecha_pago");
                records.add(new PaymentRecord(
                        resultSet.getLong("id_estudiante"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("nombre_concepto"),
                        resultSet.getDouble("monto_pagado"),
                        paymentDate.toLocalDate()));
            }
        }
        return records;
    }

    @Override
    public List<LastPaymentRecord> findLastPaymentDates() throws SQLException {
        List<LastPaymentRecord> records = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, FIND_LAST_PAYMENT_DATES);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Date lastPayment = resultSet.getDate("ultimo_pago");
                records.add(new LastPaymentRecord(
                        resultSet.getLong("id_estudiante"),
                        resultSet.getString("nombre_completo"),
                        lastPayment == null ? null : lastPayment.toLocalDate()));
            }
        }
        return records;
    }

    @Override
    public List<ClubOccupancyReport> findClubOccupancy() throws SQLException {
        List<ClubOccupancyReport> records = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, FIND_CLUB_OCCUPANCY);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                records.add(new ClubOccupancyReport(
                        resultSet.getString("nombre_club"),
                        resultSet.getString("supervisor"),
                        resultSet.getInt("activos"),
                        resultSet.getInt("capacidad_maxima")));
            }
        }
        return records;
    }

    @Override
    public List<EventParticipationRecord> findEventParticipations(int year)
            throws SQLException {
        List<EventParticipationRecord> records = new ArrayList<>();
        Date start = Date.valueOf(LocalDate.of(year, 1, 1));
        Date end = Date.valueOf(LocalDate.of(year + 1, 1, 1));
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, FIND_EVENT_PARTICIPATIONS, start, end);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Date eventDate = resultSet.getDate("fecha_evento");
                records.add(new EventParticipationRecord(
                        resultSet.getString("nombre_evento"),
                        eventDate.toLocalDate(),
                        resultSet.getBoolean("tiene_competencia"),
                        resultSet.getLong("id_estudiante"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("rol_participacion"),
                        resultSet.getString("posicion_obtenida")));
            }
        }
        return records;
    }

    @Override
    public List<ReportRow> countLoansByState() throws SQLException {
        return executeLabelCountQuery(COUNT_LOANS_BY_STATE);
    }

    @Override
    public List<LoanOverdueReport> findOverdueLoans(LocalDate referenceDate)
            throws SQLException {
        List<LoanOverdueReport> records = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, FIND_OVERDUE_LOANS,
                     Date.valueOf(referenceDate), Date.valueOf(referenceDate));
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                LocalDate expectedReturn =
                        resultSet.getDate("fecha_devolucion_esperada").toLocalDate();
                records.add(new LoanOverdueReport(
                        resultSet.getString("tipo_prestamo"),
                        resultSet.getString("nombre_recurso"),
                        resultSet.getString("titular"),
                        resultSet.getDate("fecha_prestamo").toLocalDate(),
                        expectedReturn,
                        ChronoUnit.DAYS.between(expectedReturn, referenceDate)));
            }
        }
        return records;
    }

    @Override
    public List<ReportRow> countInventoryByCategoryAndState() throws SQLException {
        return executeLabelCountQuery(COUNT_INVENTORY_BY_CATEGORY_AND_STATE);
    }

    // ---------- Private helpers ----------

    /** Opens a fresh connection; the caller closes it (try-with-resources). */
    private Connection openConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Creates a prepared statement and binds the given parameters. */
    private PreparedStatement prepare(Connection connection, String sql,
                                      Object... parameters) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
        return statement;
    }

    /** Executes a "SELECT COUNT(*)" query and returns the single value. */
    private int executeSingleCount(String sql) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    /**
     * Executes a "SELECT label, COUNT(*) ... GROUP BY label" query and maps
     * each row to a {@link ReportRow} with the count as its value.
     */
    private List<ReportRow> executeLabelCountQuery(String sql) throws SQLException {
        List<ReportRow> rows = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rows.add(new ReportRow(resultSet.getString(1),
                        String.valueOf(resultSet.getLong(2))));
            }
        }
        return rows;
    }
}
