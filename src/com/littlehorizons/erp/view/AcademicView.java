package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicRecordController;
import com.littlehorizons.erp.controller.AttendanceController;
import com.littlehorizons.erp.controller.SubjectController;
import com.littlehorizons.erp.controller.SubjectEnrollmentController;
import com.littlehorizons.erp.util.ConsoleUtils;

/**
 * Console view of the academic module. It only shows the main menu and
 * delegates each option to its controller; 0 goes back to the main menu.
 */
public class AcademicView {

    private final SubjectController subjectController;
    private final SubjectEnrollmentController subjectEnrollmentController;
    private final AcademicRecordController academicRecordController;
    private final AttendanceController attendanceController;

    /** Builds the view with its controllers using no-arg constructor chains. */
    public AcademicView() {
        this.subjectController = new SubjectController();
        this.subjectEnrollmentController = new SubjectEnrollmentController();
        this.academicRecordController = new AcademicRecordController();
        this.attendanceController = new AttendanceController();
    }

    /** Main academic menu loop. */
    public void show() {
        while (true) {
            ConsoleUtils.printHeader("ACADEMIC MODULE");
            System.out.println(" 1. Subjects");
            System.out.println(" 2. Subject enrollments");
            System.out.println(" 3. Grades / qualifications");
            System.out.println(" 4. Attendance");
            int option = ConsoleUtils.readMenuOption(" Select an option: ", 4);
            switch (option) {
                case 1 -> subjectController.show();
                case 2 -> subjectEnrollmentController.show();
                case 3 -> academicRecordController.show();
                case 4 -> attendanceController.show();
                case 0 -> {
                    return;
                }
                default -> { /* unreachable */ }
            }
        }
    }
}
