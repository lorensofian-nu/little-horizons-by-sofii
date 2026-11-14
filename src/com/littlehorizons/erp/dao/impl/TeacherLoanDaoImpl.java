package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.TeacherLoanDao;
import com.littlehorizons.erp.model.LoanState;
import com.littlehorizons.erp.model.LoanSummary;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.model.TeacherSummary;
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
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of {@link TeacherLoanDao} for {@code prestamos_profesor}. */
public class TeacherLoanDaoImpl implements TeacherLoanDao {

    private static final String BASE_SELECT =
            "SELECT id_prestamo, id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_esperada, "
                    + "fecha_devolucion_real, estado_prestamo, observaciones, fecha_creacion, "
                    + "fecha_actualizacion FROM prestamos_profesor ";

    private static final String SUMMARY_SELECT =
            "SELECT p.id_prestamo, p.fecha_prestamo, p.fecha_devolucion_esperada, "
                    + "p.fecha_devolucion_real, p.estado_prestamo, p.observaciones, "
                    + "r.codigo_recurso, r.nombre_recurso, "
                    + "CONCAT_WS(' ', pr.primer_nombre, pr.segundo_nombre, pr.primer_apellido, "
                    + "pr.segundo_apellido) AS nombre_completo "
                    + "FROM prestamos_profesor p "
                    + "JOIN recursos r ON r.id_recurso = p.id_recurso "
                    + "JOIN profesores pr ON pr.id_profesor = p.id_profesor ";

    @Override
    public int save(TeacherLoan loan) throws SQLException {
        String sql = "INSERT INTO prestamos_profesor (id_recurso, id_profesor, fecha_prestamo, "
                + "fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, loan.getResourceId());
            statement.setInt(2, loan.getTeacherId());
            statement.setDate(3, Date.valueOf(loan.getLoanDate()));
            statement.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
            statement.setDate(5, loan.getRealReturnDate() != null
                    ? Date.valueOf(loan.getRealReturnDate()) : null);
            statement.setString(6, loan.getState() != null
                    ? loan.getState().getDbValue() : LoanState.ACTIVE.getDbValue());
            statement.setString(7, loan.getObservations());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    loan.setId(keys.getInt(1));
                }
            }
            return loan.getId();
        }
    }

    @Override
    public int saveAndLoanResource(TeacherLoan loan) throws SQLException {
        String insertSql = "INSERT INTO prestamos_profesor (id_recurso, id_profesor, fecha_prestamo, "
                + "fecha_devolucion_esperada, estado_prestamo, observaciones) "
                + "VALUES (?, ?, ?, ?, 'Activo', ?)";
        String resourceSql = "UPDATE recursos SET estado_recurso = 'Prestado' "
                + "WHERE id_recurso = ? AND estado_recurso = 'Disponible'";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            try {
                connection.setAutoCommit(false);
                int loanId;
                try (PreparedStatement statement =
                             connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, loan.getResourceId());
                    statement.setInt(2, loan.getTeacherId());
                    statement.setDate(3, Date.valueOf(loan.getLoanDate()));
                    statement.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
                    statement.setString(5, loan.getObservations());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Loan insert did not return a generated key.");
                        }
                        loanId = keys.getInt(1);
                        loan.setId(loanId);
                    }
                }
                try (PreparedStatement resourceStatement = connection.prepareStatement(resourceSql)) {
                    resourceStatement.setInt(1, loan.getResourceId());
                    if (resourceStatement.executeUpdate() != 1) {
                        connection.rollback();
                        throw new SQLException("The resource is no longer available for loan.");
                    }
                }
                connection.commit();
                return loanId;
            } catch (SQLException exception) {
                safeRollback(connection, "register teacher loan", exception);
                throw exception;
            } finally {
                restoreAutoCommit(connection, "teacher loan transaction");
            }
        }
    }

    @Override
    public boolean registerReturn(int loanId, LocalDate realReturnDate, String observations)
            throws SQLException {
        String selectSql = "SELECT id_recurso, estado_prestamo FROM prestamos_profesor WHERE id_prestamo = ?";
        String updateLoanSql = "UPDATE prestamos_profesor SET fecha_devolucion_real = ?, "
                + "estado_prestamo = 'Devuelto', observaciones = ? "
                + "WHERE id_prestamo = ? AND estado_prestamo = 'Activo'";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Disponible' WHERE id_recurso = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            try {
                connection.setAutoCommit(false);
                int resourceId;
                try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
                    selectStatement.setInt(1, loanId);
                    try (ResultSet resultSet = selectStatement.executeQuery()) {
                        if (!resultSet.next()
                                || !LoanState.ACTIVE.getDbValue().equals(resultSet.getString("estado_prestamo"))) {
                            connection.rollback();
                            return false;
                        }
                        resourceId = resultSet.getInt("id_recurso");
                    }
                }
                try (PreparedStatement loanStatement = connection.prepareStatement(updateLoanSql)) {
                    loanStatement.setDate(1, Date.valueOf(realReturnDate));
                    loanStatement.setString(2, observations);
                    loanStatement.setInt(3, loanId);
                    if (loanStatement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                try (PreparedStatement resourceStatement = connection.prepareStatement(updateResourceSql)) {
                    resourceStatement.setInt(1, resourceId);
                    resourceStatement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                safeRollback(connection, "register teacher loan return", exception);
                throw exception;
            } finally {
                restoreAutoCommit(connection, "teacher loan return transaction");
            }
        }
    }

    @Override
    public int markOverdue(LocalDate today) throws SQLException {
        String sql = "UPDATE prestamos_profesor SET estado_prestamo = 'Vencido' "
                + "WHERE estado_prestamo = 'Activo' AND fecha_devolucion_esperada < ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(today));
            return statement.executeUpdate();
        }
    }

    @Override
    public boolean hasActiveLoanForResource(int resourceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos_profesor "
                + "WHERE id_recurso = ? AND estado_prestamo IN ('Activo', 'Vencido')";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resourceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public TeacherLoan findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE id_prestamo = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<TeacherLoan> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY fecha_prestamo DESC, id_prestamo DESC";
        List<TeacherLoan> loans = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                loans.add(mapRow(resultSet));
            }
        }
        return loans;
    }

    @Override
    public boolean update(TeacherLoan loan) throws SQLException {
        String sql = "UPDATE prestamos_profesor SET id_recurso = ?, id_profesor = ?, fecha_prestamo = ?, "
                + "fecha_devolucion_esperada = ?, fecha_devolucion_real = ?, estado_prestamo = ?, "
                + "observaciones = ? WHERE id_prestamo = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, loan.getResourceId());
            statement.setInt(2, loan.getTeacherId());
            statement.setDate(3, Date.valueOf(loan.getLoanDate()));
            statement.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
            statement.setDate(5, loan.getRealReturnDate() != null
                    ? Date.valueOf(loan.getRealReturnDate()) : null);
            statement.setString(6, loan.getState().getDbValue());
            statement.setString(7, loan.getObservations());
            statement.setInt(8, loan.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM prestamos_profesor WHERE id_prestamo = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public List<TeacherSummary> listActiveTeachers() throws SQLException {
        String sql = "SELECT id_profesor, CONCAT_WS(' ', primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido) AS nombre_completo "
                + "FROM profesores WHERE estado = 'Activo' ORDER BY primer_apellido, primer_nombre";
        List<TeacherSummary> teachers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                teachers.add(new TeacherSummary(
                        resultSet.getInt("id_profesor"),
                        resultSet.getString("nombre_completo")));
            }
        }
        return teachers;
    }

    @Override
    public List<LoanSummary> findActiveSummaries() throws SQLException {
        return querySummaries(SUMMARY_SELECT + "WHERE p.estado_prestamo IN ('Activo', 'Vencido') "
                + "ORDER BY p.fecha_devolucion_esperada, p.id_prestamo", statement -> { });
    }

    @Override
    public List<LoanSummary> findOverdueSummaries(LocalDate today) throws SQLException {
        return querySummaries(SUMMARY_SELECT
                + "WHERE p.estado_prestamo IN ('Activo', 'Vencido') AND p.fecha_devolucion_esperada < ? "
                + "ORDER BY p.fecha_devolucion_esperada", statement -> statement.setDate(1, Date.valueOf(today)));
    }

    @Override
    public List<LoanSummary> findSummariesByTeacher(int teacherId) throws SQLException {
        return querySummaries(SUMMARY_SELECT + "WHERE p.id_profesor = ? "
                + "ORDER BY p.fecha_prestamo DESC, p.id_prestamo DESC",
                statement -> statement.setInt(1, teacherId));
    }

    private List<LoanSummary> querySummaries(String sql, SqlBinder binder) throws SQLException {
        List<LoanSummary> summaries = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    summaries.add(mapSummaryRow(resultSet));
                }
            }
        }
        return summaries;
    }

    private LoanSummary mapSummaryRow(ResultSet resultSet) throws SQLException {
        LocalDate realReturnDate = resultSet.getDate("fecha_devolucion_real") != null
                ? resultSet.getDate("fecha_devolucion_real").toLocalDate() : null;
        return new LoanSummary(
                resultSet.getInt("id_prestamo"),
                resultSet.getString("codigo_recurso"),
                resultSet.getString("nombre_recurso"),
                resultSet.getString("nombre_completo"),
                resultSet.getDate("fecha_prestamo").toLocalDate(),
                resultSet.getDate("fecha_devolucion_esperada").toLocalDate(),
                realReturnDate,
                LoanState.fromDbValue(resultSet.getString("estado_prestamo")),
                resultSet.getString("observaciones"));
    }

    private TeacherLoan mapRow(ResultSet resultSet) throws SQLException {
        LocalDate realReturnDate = resultSet.getDate("fecha_devolucion_real") != null
                ? resultSet.getDate("fecha_devolucion_real").toLocalDate() : null;
        Timestamp creationDate = resultSet.getTimestamp("fecha_creacion");
        Timestamp lastUpdateDate = resultSet.getTimestamp("fecha_actualizacion");
        return new TeacherLoan(
                resultSet.getInt("id_prestamo"),
                resultSet.getInt("id_recurso"),
                resultSet.getInt("id_profesor"),
                resultSet.getDate("fecha_prestamo").toLocalDate(),
                resultSet.getDate("fecha_devolucion_esperada").toLocalDate(),
                realReturnDate,
                LoanState.fromDbValue(resultSet.getString("estado_prestamo")),
                resultSet.getString("observaciones"),
                creationDate != null ? creationDate.toLocalDateTime() : null,
                lastUpdateDate != null ? lastUpdateDate.toLocalDateTime() : null);
    }

    private void safeRollback(Connection connection, String context, SQLException original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            LoggerUtil.logError("Rollback failed while trying to " + context, rollbackException);
        }
    }

    private void restoreAutoCommit(Connection connection, String context) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException closeException) {
            LoggerUtil.logError("Restoring auto-commit for " + context, closeException);
        }
    }

    /** Functional callback that binds parameters to a prepared statement. */
    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
