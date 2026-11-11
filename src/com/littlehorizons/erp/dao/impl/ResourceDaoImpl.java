package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceState;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of {@link ResourceDao} for {@code recursos}. */
public class ResourceDaoImpl implements ResourceDao {

    private static final String BASE_SELECT =
            "SELECT id_recurso, codigo_recurso, nombre_recurso, id_categoria, descripcion, "
                    + "fecha_adquisicion, ubicacion_fisica, estado_recurso, fecha_creacion, "
                    + "fecha_actualizacion FROM recursos ";

    @Override
    public int save(Resource resource) throws SQLException {
        String sql = "INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, "
                + "fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, resource.getCode());
            statement.setString(2, resource.getName());
            statement.setInt(3, resource.getCategoryId());
            statement.setString(4, resource.getDescription());
            statement.setDate(5, resource.getAcquisitionDate() != null
                    ? java.sql.Date.valueOf(resource.getAcquisitionDate()) : null);
            statement.setString(6, resource.getPhysicalLocation());
            statement.setString(7, resource.getState() != null
                    ? resource.getState().getDbValue() : ResourceState.AVAILABLE.getDbValue());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    resource.setId(keys.getInt(1));
                }
            }
            return resource.getId();
        }
    }

    @Override
    public Resource findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE id_recurso = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Resource> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY codigo_recurso";
        List<Resource> resources = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                resources.add(mapRow(resultSet));
            }
        }
        return resources;
    }

    @Override
    public boolean update(Resource resource) throws SQLException {
        String sql = "UPDATE recursos SET codigo_recurso = ?, nombre_recurso = ?, id_categoria = ?, "
                + "descripcion = ?, fecha_adquisicion = ?, ubicacion_fisica = ? WHERE id_recurso = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resource.getCode());
            statement.setString(2, resource.getName());
            statement.setInt(3, resource.getCategoryId());
            statement.setString(4, resource.getDescription());
            statement.setDate(5, resource.getAcquisitionDate() != null
                    ? java.sql.Date.valueOf(resource.getAcquisitionDate()) : null);
            statement.setString(6, resource.getPhysicalLocation());
            statement.setInt(7, resource.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM recursos WHERE id_recurso = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public List<Resource> findByState(ResourceState state) throws SQLException {
        String sql = BASE_SELECT + "WHERE estado_recurso = ? ORDER BY codigo_recurso";
        List<Resource> resources = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, state.getDbValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resources.add(mapRow(resultSet));
                }
            }
        }
        return resources;
    }

    @Override
    public List<Resource> findByCategory(int categoryId) throws SQLException {
        String sql = BASE_SELECT + "WHERE id_categoria = ? ORDER BY codigo_recurso";
        List<Resource> resources = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resources.add(mapRow(resultSet));
                }
            }
        }
        return resources;
    }

    @Override
    public List<Resource> findByName(String nameFragment) throws SQLException {
        String sql = BASE_SELECT + "WHERE LOWER(nombre_recurso) LIKE ? ORDER BY codigo_recurso";
        List<Resource> resources = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + nameFragment.toLowerCase() + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resources.add(mapRow(resultSet));
                }
            }
        }
        return resources;
    }

    @Override
    public boolean updateState(int id, ResourceState state) throws SQLException {
        String sql = "UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, state.getDbValue());
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean hasLoanHistory(int id) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM prestamos_estudiante WHERE id_recurso = ?) + "
                + "(SELECT COUNT(*) FROM prestamos_profesor WHERE id_recurso = ?) AS total";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setInt(2, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("total") > 0;
            }
        }
    }

    @Override
    public boolean codeExists(String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM recursos WHERE codigo_recurso = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private Resource mapRow(ResultSet resultSet) throws SQLException {
        LocalDate acquisitionDate = resultSet.getDate("fecha_adquisicion") != null
                ? resultSet.getDate("fecha_adquisicion").toLocalDate() : null;
        Timestamp creationDate = resultSet.getTimestamp("fecha_creacion");
        Timestamp lastUpdateDate = resultSet.getTimestamp("fecha_actualizacion");
        return new Resource(
                resultSet.getInt("id_recurso"),
                resultSet.getString("codigo_recurso"),
                resultSet.getString("nombre_recurso"),
                resultSet.getInt("id_categoria"),
                resultSet.getString("descripcion"),
                acquisitionDate,
                resultSet.getString("ubicacion_fisica"),
                ResourceState.fromDbValue(resultSet.getString("estado_recurso")),
                creationDate != null ? creationDate.toLocalDateTime() : null,
                lastUpdateDate != null ? lastUpdateDate.toLocalDateTime() : null);
    }
}
