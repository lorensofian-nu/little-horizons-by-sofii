package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data access contract for the {@code profesores} table.
 */
public interface TeacherDao {

    /** Inserts a new teacher and returns the generated {@code id_profesor}. */
    int save(Teacher teacher) throws SQLException;

    /** Finds a teacher by primary key. */
    Optional<Teacher> findById(int id) throws SQLException;

    /** Returns every teacher ordered by last name and first name. */
    List<Teacher> findAll() throws SQLException;

    /** Returns only teachers with {@code estado = 'Activo'}. */
    List<Teacher> listActiveTeachers() throws SQLException;

    /** Updates every editable column of the teacher row. */
    boolean update(Teacher teacher) throws SQLException;

    /** Changes only the {@code estado} column (preferred over hard delete). */
    boolean updateStatus(int teacherId, TeacherStatus status) throws SQLException;

    /** Hard-deletes a teacher. Assignments are removed by ON DELETE CASCADE. */
    boolean delete(int id) throws SQLException;

    /** Finds a teacher by document number ({@code numero_documento}, unique). */
    Optional<Teacher> findByDocument(String documentNumber) throws SQLException;

    /** Finds a teacher by email ({@code correo_electronico}, unique). */
    Optional<Teacher> findByEmail(String email) throws SQLException;
}
