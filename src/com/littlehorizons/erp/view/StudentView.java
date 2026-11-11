package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.EnrollmentController;
import com.littlehorizons.erp.controller.GuardianController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.dao.impl.EnrollmentDaoImpl;
import com.littlehorizons.erp.dao.impl.GradeDaoImpl;
import com.littlehorizons.erp.dao.impl.GuardianDaoImpl;
import com.littlehorizons.erp.dao.impl.StudentDaoImpl;
import com.littlehorizons.erp.dao.impl.StudentGuardianDaoImpl;
import com.littlehorizons.erp.util.ConsoleUtils;

/**
 * Main entry point of the students module. Offers the submenus:
 * students, guardians, student-guardian links and enrollments.
 * Option 0 always goes back to the previous menu.
 */
public class StudentView {

    private final StudentController studentController;
    private final GuardianController guardianController;
    private final EnrollmentController enrollmentController;

    public StudentView() {
        StudentDaoImpl studentDao = new StudentDaoImpl();
        StudentGuardianDaoImpl studentGuardianDao = new StudentGuardianDaoImpl();
        this.studentController = new StudentController(studentDao);
        this.guardianController = new GuardianController(new GuardianDaoImpl(),
                studentGuardianDao, studentDao);
        this.enrollmentController = new EnrollmentController(new EnrollmentDaoImpl(),
                studentDao, new GradeDaoImpl(), studentGuardianDao);
    }

    /** Main loop of the module. */
    public void show() {
        int option;
        do {
            ConsoleUtils.printHeader("STUDENTS MODULE");
            System.out.println(" 1. Students");
            System.out.println(" 2. Guardians");
            System.out.println(" 3. Student-Guardian links");
            System.out.println(" 4. Enrollments");
            System.out.println(" 0. Back to main menu");
            option = ConsoleUtils.readMenuOption("Select an option: ", 4);
            switch (option) {
                case 1 -> showStudentsMenu();
                case 2 -> showGuardiansMenu();
                case 3 -> showLinksMenu();
                case 4 -> showEnrollmentsMenu();
                case 0 -> System.out.println("  Returning to main menu...");
                default -> ConsoleUtils.printError("Invalid option.");
            }
        } while (option != 0);
    }

    private void showStudentsMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("STUDENTS");
            System.out.println(" 1. Register new student");
            System.out.println(" 2. List / search students");
            System.out.println(" 3. Update student");
            System.out.println(" 4. Change student status");
            System.out.println(" 5. Delete student");
            System.out.println(" 0. Back");
            option = ConsoleUtils.readMenuOption("Select an option: ", 5);
            switch (option) {
                case 1 -> studentController.createStudent();
                case 2 -> studentController.listStudents();
                case 3 -> studentController.updateStudent();
                case 4 -> studentController.changeStudentStatus();
                case 5 -> studentController.deleteStudent();
                case 0 -> { }
                default -> ConsoleUtils.printError("Invalid option.");
            }
            if (option >= 1 && option <= 5) {
                ConsoleUtils.pause();
            }
        } while (option != 0);
    }

    private void showGuardiansMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("GUARDIANS");
            System.out.println(" 1. Register new guardian");
            System.out.println(" 2. List / search guardians");
            System.out.println(" 3. Update guardian");
            System.out.println(" 4. Delete guardian");
            System.out.println(" 0. Back");
            option = ConsoleUtils.readMenuOption("Select an option: ", 4);
            switch (option) {
                case 1 -> guardianController.createGuardian();
                case 2 -> guardianController.listGuardians();
                case 3 -> guardianController.updateGuardian();
                case 4 -> guardianController.deleteGuardian();
                case 0 -> { }
                default -> ConsoleUtils.printError("Invalid option.");
            }
            if (option >= 1 && option <= 4) {
                ConsoleUtils.pause();
            }
        } while (option != 0);
    }

    private void showLinksMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("STUDENT-GUARDIAN LINKS");
            System.out.println(" 1. Assign guardian to student");
            System.out.println(" 2. Remove guardian link");
            System.out.println(" 3. List guardians of a student");
            System.out.println(" 4. List students of a guardian");
            System.out.println(" 0. Back");
            option = ConsoleUtils.readMenuOption("Select an option: ", 4);
            switch (option) {
                case 1 -> guardianController.assignGuardianToStudent();
                case 2 -> guardianController.removeGuardianLink();
                case 3 -> guardianController.listGuardiansOfStudent();
                case 4 -> guardianController.listStudentsOfGuardian();
                case 0 -> { }
                default -> ConsoleUtils.printError("Invalid option.");
            }
            if (option >= 1 && option <= 4) {
                ConsoleUtils.pause();
            }
        } while (option != 0);
    }

    private void showEnrollmentsMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("ENROLLMENTS");
            System.out.println(" 1. Enroll student in a grade");
            System.out.println(" 2. List enrollments");
            System.out.println(" 3. Withdraw enrollment");
            System.out.println(" 0. Back");
            option = ConsoleUtils.readMenuOption("Select an option: ", 3);
            switch (option) {
                case 1 -> enrollmentController.enrollStudent();
                case 2 -> enrollmentController.listEnrollments();
                case 3 -> enrollmentController.withdrawEnrollment();
                case 0 -> { }
                default -> ConsoleUtils.printError("Invalid option.");
            }
            if (option >= 1 && option <= 3) {
                ConsoleUtils.pause();
            }
        } while (option != 0);
    }
}
