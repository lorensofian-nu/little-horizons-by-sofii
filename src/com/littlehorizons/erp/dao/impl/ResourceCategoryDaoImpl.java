package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.ResourceCategoryDao;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of {@link ResourceCategoryDao} for {@code categorias_recurso}. */
public class ResourceCategoryDaoImpl implements ResourceCategoryDao {

    private static final String BASE_SELECT =
            "SELECT id_categoria, nombre_categoria, descripcion, fecha_creacion "
                    + "FROM categorias_recurso ";

    @Override
    public int save(ResourceCategory category) throws SQLException {
        String sql = "INSERT INTO categorias_recurso (nombre_categoria, descripcion) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
            return category.getId();
        }
    }

    @Override
    public ResourceCategory findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE id_categoria = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<ResourceCategory> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY nombre_categoria";
        List<ResourceCategory> categories = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(mapRow(resultSet));
            }
        }
        return categories;
    }

    @Override
    public boolean update(ResourceCategory category) throws SQLException {
        String sql = "UPDATE categorias_recurso SET nombre_categoria = ?, descripcion = ? WHERE id_categoria = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, category.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM categorias_recurso WHERE id_categoria = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private ResourceCategory mapRow(ResultSet resultSet) throws SQLException {
        Timestamp creationDate = resultSet.getTimestamp("fecha_creacion");
        return new ResourceCategory(
                resultSet.getInt("id_categoria"),
                resultSet.getString("nombre_categoria"),
                resultSet.getString("descripcion"),
                creationDate != null ? creationDate.toLocalDateTime() : null);
    }
}
