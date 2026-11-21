package test.controlador;

import com.littlehorizons.erp.util.DatabaseConnection;
import test.modelo.MateriaAsignada;
import test.modelo.Profesor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Consulta la carga académica y genera el reporte de Recursos Humanos. */
public final class ExportadorMateriasProfesorController {

    public static final int ANIO_ACADEMICO = 2025;
    private static final String SEPARADOR_PRINCIPAL = "=".repeat(79);
    private static final String SEPARADOR_SECUNDARIO = "-".repeat(79);
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String CONSULTA_PROFESOR =
            "SELECT id_profesor, tipo_documento, numero_documento, primer_nombre, "
                    + "segundo_nombre, primer_apellido, segundo_apellido "
                    + "FROM profesores WHERE id_profesor = ?";

    private static final String CONSULTA_MATERIAS =
            "SELECT m.codigo_materia, m.nombre_materia, g.nombre_grado "
                    + "FROM asignaciones_profesor ap "
                    + "JOIN materias m ON m.id_materia = ap.id_materia "
                    + "JOIN grados g ON g.id_grado = m.id_grado "
                    + "WHERE ap.id_profesor = ? AND ap.año_academico = ? "
                    + "ORDER BY g.nivel_numerico, m.nombre_materia, m.codigo_materia";

    /** Busca un profesor por su identificador. */
    public Optional<Profesor> buscarProfesor(int idProfesor) throws SQLException {
        try (Connection conexion = DatabaseConnection.getInstance().getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(CONSULTA_PROFESOR)) {
            sentencia.setInt(1, idProfesor);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Profesor(
                        resultado.getInt("id_profesor"),
                        resultado.getString("tipo_documento"),
                        resultado.getString("numero_documento"),
                        resultado.getString("primer_nombre"),
                        resultado.getString("segundo_nombre"),
                        resultado.getString("primer_apellido"),
                        resultado.getString("segundo_apellido")));
            }
        }
    }

    /** Obtiene únicamente las materias asignadas al profesor en 2025. */
    public List<MateriaAsignada> buscarMateriasAsignadas(int idProfesor) throws SQLException {
        return buscarMateriasAsignadas(idProfesor, ANIO_ACADEMICO);
    }

    /** Obtiene las materias asignadas al profesor en el año indicado. */
    public List<MateriaAsignada> buscarMateriasAsignadas(int idProfesor, int anioAcademico)
            throws SQLException {
        List<MateriaAsignada> materias = new ArrayList<>();
        try (Connection conexion = DatabaseConnection.getInstance().getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(CONSULTA_MATERIAS)) {
            sentencia.setInt(1, idProfesor);
            sentencia.setInt(2, anioAcademico);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    materias.add(new MateriaAsignada(
                            resultado.getString("codigo_materia"),
                            resultado.getString("nombre_materia"),
                            resultado.getString("nombre_grado")));
                }
            }
        }
        return materias;
    }

    /** Genera el archivo en el directorio desde el que se ejecuta la aplicación. */
    public Path generarArchivo(Profesor profesor, List<MateriaAsignada> materias)
            throws IOException {
        return generarArchivo(profesor, materias, ANIO_ACADEMICO);
    }

    /** Genera el archivo para el año académico indicado. */
    public Path generarArchivo(Profesor profesor, List<MateriaAsignada> materias,
                               int anioAcademico) throws IOException {
        String nombreArchivo = "materias_profesor_" + profesor.getId() + "_"
                + anioAcademico + ".txt";
        Path archivo = Path.of(nombreArchivo).toAbsolutePath().normalize();
        try (BufferedWriter escritor = Files.newBufferedWriter(
                archivo,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            escritor.write(construirReporte(profesor, materias, anioAcademico));
        }
        return archivo;
    }

    /** Construye el contenido exacto del documento de texto. */
    public String construirReporte(Profesor profesor, List<MateriaAsignada> materias) {
        return construirReporte(profesor, materias, ANIO_ACADEMICO);
    }

    /** Construye el contenido del documento para el año académico indicado. */
    public String construirReporte(Profesor profesor, List<MateriaAsignada> materias,
                                   int anioAcademico) {
        String salto = System.lineSeparator();
        StringBuilder reporte = new StringBuilder();
        reporte.append(SEPARADOR_PRINCIPAL).append(salto);
        reporte.append("           COLEGIO INTERNACIONAL \"LITTLE HORIZONS\"").append(salto);
        reporte.append("              REPORTE DE MATERIAS ASIGNADAS - AÑO ")
                .append(anioAcademico).append(salto);
        reporte.append(SEPARADOR_PRINCIPAL).append(salto).append(salto);

        reporte.append("DATOS DEL PROFESOR").append(salto);
        reporte.append(SEPARADOR_SECUNDARIO).append(salto);
        reporte.append(String.format("Nombre completo    : %s%s",
                profesor.getNombreCompleto(), salto));
        reporte.append(String.format("Documento          : %s%s",
                profesor.getDocumentoCompleto(), salto)).append(salto);

        reporte.append(SEPARADOR_PRINCIPAL).append(salto);
        reporte.append("                        MATERIAS ASIGNADAS").append(salto);
        reporte.append(SEPARADOR_PRINCIPAL).append(salto).append(salto);

        reporte.append(String.format("%-10s%-35s%-20s%s",
                "Código", "Materia", "Grado", salto));
        reporte.append(SEPARADOR_SECUNDARIO).append(salto);
        for (MateriaAsignada materia : materias) {
            reporte.append(String.format("%-10s%-35s%-20s%s",
                    materia.getCodigo(), materia.getNombre(), materia.getGrado(), salto));
        }

        reporte.append(salto).append(SEPARADOR_PRINCIPAL).append(salto).append(salto);
        reporte.append("Documento generado el: ")
                .append(LocalDate.now().format(FORMATO_FECHA)).append(salto).append(salto);
        reporte.append("________________________________________________").append(salto);
        reporte.append("Recursos Humanos").append(salto);
        reporte.append("Colegio Internacional \"Little Horizons\"").append(salto);
        return reporte.toString();
    }
}
