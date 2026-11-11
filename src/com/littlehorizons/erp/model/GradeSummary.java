package com.littlehorizons.erp.model;

/**
 * Lightweight read-only summary of a grade level (table {@code grados}) used
 * by the academic module when creating or listing subjects.
 *
 * @param id    grade id (id_grado)
 * @param name  grade name (nombre_grado), e.g. 'Preescolar', 'Quinto', 'Once'
 * @param level numeric level for ordering (nivel_numerico), 0..11
 */
public record GradeSummary(int id, String name, int level) {

    @Override
    public String toString() {
        return "id " + id + " - " + name + " (level " + level + ")";
    }
}
