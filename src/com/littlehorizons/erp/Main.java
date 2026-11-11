package com.littlehorizons.erp;

import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.view.AcademicView;
import com.littlehorizons.erp.view.ExtracurricularView;
import com.littlehorizons.erp.view.FinancialView;
import com.littlehorizons.erp.view.ReportsView;
import com.littlehorizons.erp.view.ResourceView;
import com.littlehorizons.erp.view.StudentView;
import com.littlehorizons.erp.view.TeacherView;

/**
 * Entry point of the Little Horizons ERP console application.
 *
 * <p>Shows the main menu, checks the database connection at startup and
 * delegates every option to the corresponding module view.</p>
 */
public final class Main {

    private static final int EXIT_OPTION = 8;

    private Main() {
        // Application entry point must not be instantiated.
    }

    public static void main(String[] args) {
        printBanner();

        DatabaseConnection database;
        try {
            database = DatabaseConnection.getInstance();
        } catch (IllegalStateException exception) {
            ConsoleUtils.printError("Configuration error: " + exception.getMessage());
            LoggerUtil.logError("Application started with invalid database configuration",
                    exception);
            return;
        }

        if (!database.testConnection()) {
            ConsoleUtils.printError("Could not connect to the database 'erp_little_horizons'.");
            System.out.println("  1. Verify that MySQL 8.0+ is running.");
            System.out.println("  2. Check the credentials in the 'db.properties' file.");
            System.out.println("  3. Run the database script located at docs/database.sql first.");
            LoggerUtil.logInfo("Application started without a valid database connection");
            return;
        }

        StudentView studentView = new StudentView();
        AcademicView academicView = new AcademicView();
        TeacherView teacherView = new TeacherView();
        ExtracurricularView extracurricularView = new ExtracurricularView();
        FinancialView financialView = new FinancialView();
        ResourceView resourceView = new ResourceView();
        ReportsView reportsView = new ReportsView();

        boolean running = true;
        while (running) {
            printMainMenu();
            int option = ConsoleUtils.readMenuOption("Seleccione una opción: ", EXIT_OPTION);
            switch (option) {
                case 1 -> studentView.show();
                case 2 -> academicView.show();
                case 3 -> teacherView.show();
                case 4 -> extracurricularView.show();
                case 5 -> financialView.show();
                case 6 -> resourceView.show();
                case 7 -> reportsView.show();
                case EXIT_OPTION -> {
                    if (ConsoleUtils.confirm("¿Está seguro que desea salir del sistema?")) {
                        running = false;
                        System.out.println();
                        System.out.println("Gracias por usar el ERP Colegio Little Horizons. ¡Hasta pronto!");
                    }
                }
                default -> ConsoleUtils.printError("Invalid option.");
            }
        }
        LoggerUtil.logInfo("Application closed normally");
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║      ERP COLEGIO LITTLE HORIZONS BY SOFII        ║");
        System.out.println("║         Sistema de Gestión Educativa             ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   ERP Colegio Little Horizons                    ║");
        System.out.println("║   Sistema de Gestión Educativa                   ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  1. Gestión de Estudiantes");
        System.out.println("  2. Gestión Académica");
        System.out.println("  3. Gestión de Profesores");
        System.out.println("  4. Actividades Extracurriculares");
        System.out.println("  5. Gestión Financiera");
        System.out.println("  6. Gestión de Recursos");
        System.out.println("  7. Reportes y Estadísticas");
        System.out.println("  8. Salir");
        System.out.println();
    }
}
