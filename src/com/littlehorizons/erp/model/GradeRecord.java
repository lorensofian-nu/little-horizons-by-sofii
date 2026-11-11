package com.littlehorizons.erp.model;

/**
 * Single grade record (calificación) fetched from the database so averages can
 * be computed in Java with the Stream API.
 *
 * @param studentId   student identifier (id_estudiante)
 * @param studentName full student name
 * @param gradeName   grade of the student for that academic year (nombre_grado)
 * @param subjectName subject name (nombre_materia)
 * @param period      academic period (I, II, III, IV or Final)
 * @param note        grade value between 0.0 and 5.0 (valor_nota)
 */
public record GradeRecord(
        long studentId,
        String studentName,
        String gradeName,
        String subjectName,
        String period,
        double note) {
}
