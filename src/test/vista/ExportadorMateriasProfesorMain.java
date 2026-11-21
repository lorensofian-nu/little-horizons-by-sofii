package test.vista;

import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;
import test.controlador.ExportadorMateriasProfesorController;
import test.modelo.MateriaAsignada;
import test.modelo.Profesor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** Punto de entrada independiente para exportar la carga académica docente. */
public final class ExportadorMateriasProfesorMain {

    private ExportadorMateriasProfesorMain() {
        // Clase de utilidad; no se instancia.
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in, java.nio.charset.StandardCharsets.UTF_8)) {
            ejecutarExportador((prompt) -> {
                System.out.print(prompt);
                return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
            }, true);
        } catch (RuntimeException exception) {
            LoggerUtil.logError("Exportador de materias: error inesperado", exception);
            System.out.println();
            System.out.println("Error inesperado: no fue posible completar la operación.");
        }
    }

    /** Ejecuta el exportador desde el menú de profesores sin cerrar System.in. */
    public static void ejecutarDesdeMenu() {
        ejecutarExportador(ConsoleUtils::readLine, false);
    }

    @FunctionalInterface
    private interface LectorEntrada {
        String leer(String prompt);
    }

    private static void ejecutarExportador(LectorEntrada lector, boolean ejecucionDirecta) {
        ExportadorMateriasProfesorController controlador =
                new ExportadorMateriasProfesorController();

        try {
            while (true) {
                mostrarEncabezado();

                String entrada = lector.leer(
                        "Ingrese el ID del profesor para exportar sus materias asignadas: ");
                if (entrada == null) {
                    return;
                }

                int idProfesor;
                try {
                    idProfesor = Integer.parseInt(entrada);
                    if (idProfesor <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException exception) {
                    System.out.println("Error: Debe ingresar un número válido.");
                    continue;
                }

                Optional<Profesor> profesor;
                try {
                    profesor = controlador.buscarProfesor(idProfesor);
                } catch (SQLException exception) {
                    manejarErrorBaseDatos(exception);
                    return;
                } catch (IllegalStateException exception) {
                    manejarErrorConfiguracion(exception);
                    return;
                }

                if (profesor.isEmpty()) {
                    System.out.printf("%nError: No se encontró ningún profesor con el ID %d.%n",
                            idProfesor);
                    System.out.println("Verifique el ID e intente nuevamente.");
                    continue;
                }

                System.out.printf("%nGenerando reporte para profesor con ID %d...%n", idProfesor);
                System.out.printf("Consultando asignaciones del año %d...%n",
                        ExportadorMateriasProfesorController.ANIO_ACADEMICO);

                List<MateriaAsignada> materias;
                try {
                    materias = controlador.buscarMateriasAsignadas(profesor.get().getId());
                } catch (SQLException exception) {
                    manejarErrorBaseDatos(exception);
                    return;
                } catch (IllegalStateException exception) {
                    manejarErrorConfiguracion(exception);
                    return;
                }

                if (materias.isEmpty()) {
                    System.out.printf("%nError: El profesor %s no tiene materias asignadas%n",
                            profesor.get().getNombreCompleto());
                    System.out.printf("para el año académico %d.%n",
                            ExportadorMateriasProfesorController.ANIO_ACADEMICO);
                    esperarFin(lector, ejecucionDirecta);
                    return;
                }

                try {
                    java.nio.file.Path archivo = controlador.generarArchivo(profesor.get(), materias);
                    System.out.println();
                    System.out.println("===============================================================================");
                    System.out.println("            ARCHIVO GENERADO EXITOSAMENTE");
                    System.out.println("===============================================================================");
                    System.out.println();
                    System.out.println("Profesor: " + profesor.get().getNombreCompleto());
                    System.out.println("Archivo generado: materias_profesor_" + idProfesor
                            + "_" + ExportadorMateriasProfesorController.ANIO_ACADEMICO + ".txt");
                    System.out.println("Ubicación: " + archivo);
                    System.out.println();
                    System.out.println("El archivo está listo para ser utilizado.");
                    System.out.println();
                    System.out.println("===============================================================================");
                    esperarFin(lector, ejecucionDirecta);
                    return;
                } catch (IOException | SecurityException exception) {
                    manejarErrorArchivo(exception);
                    esperarFin(lector, ejecucionDirecta);
                    return;
                }
            }
        } catch (IllegalStateException exception) {
            manejarErrorConfiguracion(exception);
        } catch (RuntimeException exception) {
            LoggerUtil.logError("Exportador de materias: error inesperado", exception);
            System.out.println();
            System.out.println("Error inesperado: no fue posible completar la operación.");
        }
    }

    private static void manejarErrorBaseDatos(SQLException exception) {
        LoggerUtil.logError("Exportador de materias: consulta a la base de datos", exception);
        System.out.println();
        System.out.println("ERROR: No fue posible consultar la base de datos.");
        System.out.println("Verifique la conexión y la configuración del sistema.");
    }

    private static void manejarErrorConfiguracion(IllegalStateException exception) {
        LoggerUtil.logError("Exportador de materias: configuración", exception);
        System.out.println();
        System.out.println("ERROR: La configuración de la base de datos no es válida.");
        System.out.println("Verifique el archivo db.properties o las variables de entorno.");
    }

    private static void manejarErrorArchivo(Exception exception) {
        LoggerUtil.logError("Exportador de materias: creación del archivo", exception);
        System.out.println();
        System.out.println("===============================================================================");
        System.out.println("ERROR: No se pudo crear el archivo de exportación.");
        System.out.println("Verifique los permisos de escritura en la carpeta del proyecto.");
        System.out.println("===============================================================================");
    }

    private static void mostrarEncabezado() {
        System.out.println("===============================================================================");
        System.out.println("     EXPORTADOR DE MATERIAS ASIGNADAS A PROFESOR - AÑO 2025");
        System.out.println("===============================================================================");
        System.out.println();
    }

    private static void esperarFin(LectorEntrada lector, boolean ejecucionDirecta) {
        System.out.println();
        System.out.println(ejecucionDirecta
                ? "Presione Enter para salir..."
                : "Presione Enter para volver al menú de profesores...");
        System.out.println("===============================================================================");
        lector.leer("");
    }
}
