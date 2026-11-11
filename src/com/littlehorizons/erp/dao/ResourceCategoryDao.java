package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ResourceCategory;

import java.sql.SQLException;
import java.util.List;

/** Data access contract for the {@code categorias_recurso} table. */
public interface ResourceCategoryDao {

    /** Inserts a category and returns its generated id. */
    int save(ResourceCategory category) throws SQLException;

    ResourceCategory findById(int id) throws SQLException;

    List<ResourceCategory> findAll() throws SQLException;

    boolean update(ResourceCategory category) throws SQLException;

    boolean delete(int id) throws SQLException;
}
