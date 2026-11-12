package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.InvoiceDao;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.DocumentSequenceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of {@link InvoiceDao}. Opens a fresh connection per operation. */
public class InvoiceDaoImpl implements InvoiceDao {

    private static final String SELECT_BASE =
            "SELECT id_factura, numero_factura, id_pago, fecha_emision, cufe, "
            + "nombre_archivo_txt, ruta_archivo, fecha_creacion FROM facturas ";

    public InvoiceDaoImpl() {
        // Connection is obtained per operation; no-op constructor kept for the no-arg chain.
    }

    @Override
    public int save(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, "
                + "nombre_archivo_txt, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, invoice.getInvoiceNumber());
            statement.setInt(2, invoice.getPaymentId());
            statement.setDate(3, java.sql.Date.valueOf(invoice.getIssueDate()));
            statement.setString(4, invoice.getCufe());
            statement.setString(5, invoice.getFileName());
            statement.setString(6, invoice.getFilePath());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    invoice.setId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    @Override
    public Invoice findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_factura = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public Invoice findByInvoiceNumber(String invoiceNumber) throws SQLException {
        String sql = SELECT_BASE + "WHERE numero_factura = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, invoiceNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public Invoice findByPaymentId(int paymentId) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_pago = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, paymentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Invoice> findAll() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BASE + "ORDER BY id_factura DESC");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                invoices.add(mapRow(resultSet));
            }
        }
        return invoices;
    }

    @Override
    public String nextInvoiceNumber() throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                String next = DocumentSequenceUtil.next(connection, "FAC-", "invoice");
                connection.commit();
                return next;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private Invoice mapRow(ResultSet resultSet) throws SQLException {
        Timestamp creation = resultSet.getTimestamp("fecha_creacion");
        java.sql.Date issueDate = resultSet.getDate("fecha_emision");
        return new Invoice(
                resultSet.getInt("id_factura"),
                resultSet.getString("numero_factura"),
                resultSet.getInt("id_pago"),
                issueDate == null ? null : issueDate.toLocalDate(),
                resultSet.getString("cufe"),
                resultSet.getString("nombre_archivo_txt"),
                resultSet.getString("ruta_archivo"),
                creation == null ? null : creation.toLocalDateTime());
    }
}
