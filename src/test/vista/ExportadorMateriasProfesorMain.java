package test.vista;

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
        ejecutarExportador();
    }

    private static void ejecutarExportador() {
        ExportadorMateriasProfesorController controlador =
                new ExportadorMateriasProfesorController();

        try (Scanner scanner = new Scanner(System.in, java.nio.charset.StandardCharsets.UTF_8)) {
            while (true) {
                mostrarEncabezado();
                System.out.print("Ingrese el ID del profesor para exportar sus materias asignadas: ");

                if (!scanner.hasNextLine()) {
                    return;
                }

                String entrada = scanner.nextLine().trim();
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
                    esperarEnter(scanner);
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
                    esperarEnter(scanner);
                    return;
                } catch (IOException | SecurityException exception) {
                    manejarErrorArchivo(exception);
                    esperarEnter(scanner);
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

    private static void esperarEnter(Scanner scanner) {
        System.out.println();
        System.out.println("Presione Enter para salir...");
        System.out.println("===============================================================================");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}
