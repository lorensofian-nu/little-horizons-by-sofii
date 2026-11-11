package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.controller.StudentLoanController;
import com.littlehorizons.erp.controller.TeacherLoanController;
import com.littlehorizons.erp.util.ConsoleUtils;

/**
 * Console menu of the resource management module (library / laboratories).
 * Option {@code 0} always goes back to the previous menu.
 */
public class ResourceView {

    private final ResourceController resourceController = new ResourceController();
    private final StudentLoanController studentLoanController = new StudentLoanController();
    private final TeacherLoanController teacherLoanController = new TeacherLoanController();

    /** Shows the main menu loop of the module until the user goes back. */
    public void show() {
        while (true) {
            ConsoleUtils.printHeader("RESOURCE MANAGEMENT (Library / Laboratories)");
            System.out.println("  1. Resource categories");
            System.out.println("  2. Resource inventory");
            System.out.println("  3. Loans to students");
            System.out.println("  4. Loans to teachers");
            System.out.println("  5. Overdue report & inventory stats");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 5);
            switch (option) {
                case 1:
                    showCategoriesMenu();
                    break;
                case 2:
                    showInventoryMenu();
                    break;
                case 3:
                    showStudentLoansMenu();
                    break;
                case 4:
                    showTeacherLoansMenu();
                    break;
                case 5:
                    showReportsMenu();
                    break;
                case 0:
                    return;
                default:
                    // readMenuOption already validates the range.
                    break;
            }
        }
    }

    private void showCategoriesMenu() {
        while (true) {
            ConsoleUtils.printHeader("RESOURCE CATEGORIES");
            System.out.println("  1. List categories");
            System.out.println("  2. Create new category");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 2);
            switch (option) {
                case 1:
                    resourceController.listCategories();
                    break;
                case 2:
                    resourceController.createCategory();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    private void showInventoryMenu() {
        while (true) {
            ConsoleUtils.printHeader("RESOURCE INVENTORY");
            System.out.println("  1. Register new resource");
            System.out.println("  2. List all resources");
            System.out.println("  3. Filter by category");
            System.out.println("  4. Filter by state");
            System.out.println("  5. Search by name");
            System.out.println("  6. Update resource");
            System.out.println("  7. Change state (maintenance / write off)");
            System.out.println("  8. Delete resource");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 8);
            switch (option) {
                case 1:
                    resourceController.createResource();
                    break;
                case 2:
                    resourceController.listResources();
                    break;
                case 3:
                    resourceController.listResourcesByCategory();
                    break;
                case 4:
                    resourceController.listResourcesByState();
                    break;
                case 5:
                    resourceController.searchResourcesByName();
                    break;
                case 6:
                    resourceController.updateResource();
                    break;
                case 7:
                    resourceController.changeResourceState();
                    break;
                case 8:
                    resourceController.deleteResource();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    private void showStudentLoansMenu() {
        while (true) {
            ConsoleUtils.printHeader("LOANS TO STUDENTS");
            System.out.println("  1. Register loan");
            System.out.println("  2. Register return");
            System.out.println("  3. List active loans");
            System.out.println("  4. Loans by student");
            System.out.println("  5. Overdue loans report");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 5);
            switch (option) {
                case 1:
                    studentLoanController.registerLoan();
                    break;
                case 2:
                    studentLoanController.registerReturn();
                    break;
                case 3:
                    studentLoanController.listActiveLoans();
                    break;
                case 4:
                    studentLoanController.listLoansByStudent();
                    break;
                case 5:
                    studentLoanController.showOverdueReport();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    private void showTeacherLoansMenu() {
        while (true) {
            ConsoleUtils.printHeader("LOANS TO TEACHERS");
            System.out.println("  1. Register loan");
            System.out.println("  2. Register return");
            System.out.println("  3. List active loans");
            System.out.println("  4. Loans by teacher");
            System.out.println("  5. Overdue loans report");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 5);
            switch (option) {
                case 1:
                    teacherLoanController.registerLoan();
                    break;
                case 2:
                    teacherLoanController.registerReturn();
                    break;
                case 3:
                    teacherLoanController.listActiveLoans();
                    break;
                case 4:
                    teacherLoanController.listLoansByTeacher();
                    break;
                case 5:
                    teacherLoanController.showOverdueReport();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    private void showReportsMenu() {
        while (true) {
            ConsoleUtils.printHeader("REPORTS & STATISTICS");
            System.out.println("  1. Overdue student loans report");
            System.out.println("  2. Overdue teacher loans report");
            System.out.println("  3. Inventory statistics");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 3);
            switch (option) {
                case 1:
                    studentLoanController.showOverdueReport();
                    break;
                case 2:
                    teacherLoanController.showOverdueReport();
                    break;
                case 3:
                    resourceController.showInventoryStats();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }
}
