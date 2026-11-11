package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ClubController;
import com.littlehorizons.erp.controller.ClubMembershipController;
import com.littlehorizons.erp.controller.EventController;
import com.littlehorizons.erp.controller.EventParticipationController;
import com.littlehorizons.erp.util.ConsoleUtils;

/**
 * Main menu of the extracurricular activities module.
 * Option 0 always goes back to the previous menu.
 */
public class ExtracurricularView {

    private final ClubController clubController = new ClubController();
    private final ClubMembershipController clubMembershipController = new ClubMembershipController();
    private final EventController eventController = new EventController();
    private final EventParticipationController eventParticipationController =
            new EventParticipationController();

    public void show() {
        while (true) {
            ConsoleUtils.printHeader("EXTRACURRICULAR ACTIVITIES");
            System.out.println(" 1. Clubs");
            System.out.println(" 2. Club memberships");
            System.out.println(" 3. Events");
            System.out.println(" 4. Event participants and results");
            System.out.println(" 0. Back");
            int option = ConsoleUtils.readMenuOption("Select an option: ", 4);
            switch (option) {
                case 1 -> clubController.showMenu();
                case 2 -> clubMembershipController.showMenu();
                case 3 -> eventController.showMenu();
                case 4 -> eventParticipationController.showMenu();
                case 0 -> {
                    return;
                }
                default -> {
                }
            }
        }
    }
}
