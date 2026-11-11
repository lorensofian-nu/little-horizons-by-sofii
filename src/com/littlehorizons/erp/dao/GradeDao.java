package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Grade;

import java.sql.SQLException;
import java.util.List;

/**
 * Read-only data access contract for the {@code grados} table.
 */
public interface GradeDao {

    List<Grade> findAll() throws SQLException;

    Grade findById(int id) throws SQLException;

    int count() throws SQLException;
}
