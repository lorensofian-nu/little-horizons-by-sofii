package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Invoice;

import java.sql.SQLException;
import java.util.List;

/** Data access contract for electronic invoices ({@code facturas}). */
public interface InvoiceDao {

    /** Inserts an invoice and returns its generated id. */
    int save(Invoice invoice) throws SQLException;

    Invoice findById(int id) throws SQLException;

    Invoice findByInvoiceNumber(String invoiceNumber) throws SQLException;

    /** Finds the invoice generated for a payment, or null. */
    Invoice findByPaymentId(int paymentId) throws SQLException;

    List<Invoice> findAll() throws SQLException;

    /** Next sequential invoice number, e.g. {@code FAC-000123}. */
    String nextInvoiceNumber() throws SQLException;
}
