package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.PaymentConcept;

import java.sql.SQLException;
import java.util.List;

/** Data access contract for the payment concepts catalog ({@code conceptos_pago}). */
public interface PaymentConceptDao {

    /** Inserts a new concept and returns its generated id. */
    int save(PaymentConcept concept) throws SQLException;

    PaymentConcept findById(int id) throws SQLException;

    /** Finds a concept by its unique code, or null. */
    PaymentConcept findByCode(String conceptCode) throws SQLException;

    List<PaymentConcept> findAll() throws SQLException;

    /** Lists only concepts flagged as active. */
    List<PaymentConcept> findAllActive() throws SQLException;

    boolean update(PaymentConcept concept) throws SQLException;

    boolean delete(int id) throws SQLException;
}
