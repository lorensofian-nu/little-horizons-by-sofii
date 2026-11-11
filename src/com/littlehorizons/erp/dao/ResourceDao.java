package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceState;

import java.sql.SQLException;
import java.util.List;

/** Data access contract for the {@code recursos} table. */
public interface ResourceDao {

    /** Inserts a resource and returns its generated id. */
    int save(Resource resource) throws SQLException;

    Resource findById(int id) throws SQLException;

    List<Resource> findAll() throws SQLException;

    boolean update(Resource resource) throws SQLException;

    boolean delete(int id) throws SQLException;

    List<Resource> findByState(ResourceState state) throws SQLException;

    List<Resource> findByCategory(int categoryId) throws SQLException;

    /** Case-insensitive partial match on the resource name. */
    List<Resource> findByName(String nameFragment) throws SQLException;

    /** Updates only the state column of a resource. */
    boolean updateState(int id, ResourceState state) throws SQLException;

    /** True when the resource appears in any student or teacher loan. */
    boolean hasLoanHistory(int id) throws SQLException;

    /** True when another resource already uses the given unique code. */
    boolean codeExists(String code) throws SQLException;
}
