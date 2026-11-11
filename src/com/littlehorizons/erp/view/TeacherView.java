package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.TeacherAssignmentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.util.ConsoleUtils;

/**
 * Console view of the teachers module. It opens the main teachers menu and
 * delegates every action to {@link TeacherController} and
 * {@link TeacherAssignmentController} (instantiated with their no-arg
 * constructor chains). All I/O goes through {@link ConsoleUtils}.
 */
public class TeacherView {

    private final TeacherController teacherController = new TeacherController();
    private final TeacherAssignmentController assignmentController =
            new TeacherAssignmentController();

    /** Shows the main teachers module menu; option 0 returns to the previous menu. */
    public void show() {
        while (true) {
            ConsoleUtils.printHeader("TEACHERS MODULE - LITTLE HORIZONS");
            System.out.println("  1. Teachers");
            System.out.println("  2. Subject assignments");
            System.out.println("  0. Back to main menu");
            int option = ConsoleUtils.readMenuOption("  Select an option: ", 2);
            switch (option) {
                case 1 -> teachersMenu();
                case 2 -> assignmentsMenu();
                default -> {
                    return;
                }
            }
        }
    }

    private void teachersMenu() {
        while (true) {
            ConsoleUtils.printHeader("TEACHERS");
            System.out.println("  1. Register teacher");
            System.out.println("  2. List all teachers");
            System.out.println("  3. List active teachers");
            System.out.println("  4. Find by id");
            System.out.println("  5. Find by document");
            System.out.println("  6. Update teacher");
            System.out.println("  7. Change status");
            System.out.println("  8. Delete teacher (permanent)");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("  Select an option: ", 8);
            switch (option) {
                case 1 -> teacherController.createTeacher();
                case 2 -> teacherController.listAllTeachers();
                case 3 -> teacherController.listActiveTeachers();
                case 4 -> teacherController.findTeacherById();
                case 5 -> teacherController.findTeacherByDocument();
                case 6 -> teacherController.updateTeacher();
                case 7 -> teacherController.changeTeacherStatus();
                case 8 -> teacherController.deleteTeacher();
                default -> {
                    return;
                }
            }
            ConsoleUtils.pause();
        }
    }

    private void assignmentsMenu() {
        while (true) {
            ConsoleUtils.printHeader("SUBJECT ASSIGNMENTS");
            System.out.println("  1. New assignment");
            System.out.println("  2. List assignments by teacher");
            System.out.println("  3. List assignments by subject and year");
            System.out.println("  4. Delete assignment");
            System.out.println("  0. Back");
            int option = ConsoleUtils.readMenuOption("  Select an option: ", 4);
            switch (option) {
                case 1 -> assignmentController.createAssignment();
                case 2 -> assignmentController.listAssignmentsByTeacher();
                case 3 -> assignmentController.listAssignmentsBySubjectAndYear();
                case 4 -> assignmentController.deleteAssignment();
                default -> {
                    return;
                }
            }
            ConsoleUtils.pause();
        }
    }
}
