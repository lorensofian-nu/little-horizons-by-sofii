package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.GradeDao;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only JDBC implementation of {@link GradeDao} for the {@code grados} table.
 */
public class GradeDaoImpl implements GradeDao {

    private static final String SQL_SELECT_BASE =
            "SELECT id_grado, nombre_grado, nivel_numerico FROM grados ";

    @Override
    public List<Grade> findAll() throws SQLException {
        String sql = SQL_SELECT_BASE + "ORDER BY nivel_numerico";
        List<Grade> grades = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                grades.add(mapRow(resultSet));
            }
        }
        return grades;
    }

    @Override
    public Grade findById(int id) throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE id_grado = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM grados";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private Grade mapRow(ResultSet resultSet) throws SQLException {
        Grade grade = new Grade();
        grade.setId(resultSet.getInt("id_grado"));
        grade.setName(resultSet.getString("nombre_grado"));
        grade.setNumericLevel(resultSet.getInt("nivel_numerico"));
        return grade;
    }
}
