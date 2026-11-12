package com.littlehorizons.erp;

import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.model.AuthenticatedUser;
import com.littlehorizons.erp.service.AuthenticationService;
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
            Runtime.getRuntime().addShutdownHook(new Thread(database::closePool,
                    "database-pool-shutdown"));
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

        AuthenticationService authenticationService = new AuthenticationService();
        try {
            if (authenticationService.bootstrapInitialAdministrator()) {
                ConsoleUtils.printSuccess("Initial administrator created from environment configuration.");
            }
            if (!authenticationService.hasUsers()) {
                ConsoleUtils.printError("No application users exist. Set ERP_INITIAL_ADMIN_PASSWORD "
                        + "to a private value of at least 12 characters and restart.");
                return;
            }
        } catch (java.sql.SQLException exception) {
            ConsoleUtils.printError("Could not initialize application users.");
            LoggerUtil.logError("Main.initializeUsers", exception);
            return;
        }
        AuthenticatedUser user = authenticate(authenticationService);
        if (user == null) {
            return;
        }
        if (user.mustChangePassword() && !changeInitialPassword(authenticationService, user)) {
            return;
        }

        StudentView studentView = new StudentView();
        AcademicView academicView = new AcademicView();
        TeacherView teacherView = new TeacherView();
        ExtracurricularView extracurricularView = new ExtracurricularView();
        FinancialView financialView = new FinancialView(user.getRole());
        ResourceView resourceView = new ResourceView();
        ReportsView reportsView = new ReportsView(user.getRole());

        boolean running = true;
        while (running) {
            printMainMenu(user);
            int option = ConsoleUtils.readMenuOption("Seleccione una opción: ", EXIT_OPTION);
            if (option != EXIT_OPTION && !user.getRole().canAccess(option)) {
                ConsoleUtils.printError("You are not authorized to access that module.");
                continue;
            }
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

    private static AuthenticatedUser authenticate(AuthenticationService authenticationService) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            String username = ConsoleUtils.readRequiredText("Username: ", 80);
            char[] password = ConsoleUtils.readSecret("Password: ");
            try {
                AuthenticatedUser user = authenticationService.authenticate(username, password);
                java.util.Arrays.fill(password, '\0');
                if (user != null) {
                    System.out.println("  Authenticated as " + user.getUsername()
                            + " (" + user.getRole().getDatabaseValue() + ").");
                    return user;
                }
            } catch (java.sql.SQLException exception) {
                java.util.Arrays.fill(password, '\0');
                ConsoleUtils.printError("Could not authenticate against the database.");
                LoggerUtil.logError("Main.authenticate", exception);
                return null;
            }
            ConsoleUtils.printError("Invalid credentials. Attempt " + attempt + " of 3.");
        }
        ConsoleUtils.printError("Maximum login attempts exceeded.");
        return null;
    }

    private static boolean changeInitialPassword(AuthenticationService authenticationService,
                                                 AuthenticatedUser user) {
        System.out.println("  You must change the initial password before continuing.");
        char[] first = ConsoleUtils.readSecret("New password (minimum 12 characters): ");
        char[] second = ConsoleUtils.readSecret("Repeat new password: ");
        try {
            if (first.length < 12 || !java.util.Arrays.equals(first, second)) {
                ConsoleUtils.printError("Passwords must match and contain at least 12 characters.");
                return false;
            }
            authenticationService.changePassword(user.getUsername(), first);
            ConsoleUtils.printSuccess("Password changed successfully.");
            return true;
        } catch (java.sql.SQLException exception) {
            ConsoleUtils.printError("Could not change the initial password.");
            LoggerUtil.logError("Main.changeInitialPassword", exception);
            return false;
        } finally {
            java.util.Arrays.fill(first, '\0');
            java.util.Arrays.fill(second, '\0');
        }
    }

    private static void printMainMenu(AuthenticatedUser user) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   ERP Colegio Little Horizons                    ║");
        System.out.println("║   Sistema de Gestión Educativa                   ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        if (user.getRole().canAccess(1)) System.out.println("  1. Gestión de Estudiantes");
        if (user.getRole().canAccess(2)) System.out.println("  2. Gestión Académica");
        if (user.getRole().canAccess(3)) System.out.println("  3. Gestión de Profesores");
        if (user.getRole().canAccess(4)) System.out.println("  4. Actividades Extracurriculares");
        if (user.getRole().canAccess(5)) System.out.println("  5. Gestión Financiera");
        if (user.getRole().canAccess(6)) System.out.println("  6. Gestión de Recursos");
        if (user.getRole().canAccess(7)) System.out.println("  7. Reportes y Estadísticas");
        System.out.println("  8. Salir");
        System.out.println();
    }
}
