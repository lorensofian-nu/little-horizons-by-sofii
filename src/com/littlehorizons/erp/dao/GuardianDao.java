package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Guardian;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for the {@code acudientes} table.
 */
public interface GuardianDao {

    /** Inserts a guardian and returns the generated key ({@code id_acudiente}). */
    int save(Guardian guardian) throws SQLException;

    Guardian findById(int id) throws SQLException;

    List<Guardian> findAll() throws SQLException;

    Guardian findByDocumentNumber(String documentNumber) throws SQLException;

    /** Search by full name fragments or document number. */
    List<Guardian> searchByNameOrDocument(String term) throws SQLException;

    boolean update(Guardian guardian) throws SQLException;

    boolean deleteById(int id) throws SQLException;

    int count() throws SQLException;
}
