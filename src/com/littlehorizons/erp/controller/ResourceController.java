package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ResourceCategoryDao;
import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.impl.ResourceCategoryDaoImpl;
import com.littlehorizons.erp.dao.impl.ResourceDaoImpl;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.ResourceState;
import com.littlehorizons.erp.util.ConsoleUtils;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for resource categories and the resource inventory: CRUD,
 * state changes, searches and inventory statistics.
 */
public class ResourceController {

    private final ResourceCategoryDao categoryDao = new ResourceCategoryDaoImpl();
    private final ResourceDao resourceDao = new ResourceDaoImpl();

    // ------------------------------------------------------------------
    // Categories
    // ------------------------------------------------------------------

    /** Lists every resource category (read-only data plus user-created ones). */
    public void listCategories() {
        try {
            List<ResourceCategory> categories = categoryDao.findAll();
            if (categories.isEmpty()) {
                ConsoleUtils.printError("There are no resource categories registered yet.");
                return;
            }
            ConsoleUtils.printHeader("RESOURCE CATEGORIES");
            System.out.printf("%-4s | %-28s | %s%n", "ID", "NAME", "DESCRIPTION");
            System.out.println("------------------------------------------------------------------");
            categories.forEach(category -> System.out.printf(
                    "%-4d | %-28s | %s%n",
                    category.getId(),
                    category.getName(),
                    category.getDescription() == null ? "-" : category.getDescription()));
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not load the resource categories.");
            LoggerUtil.logError("ResourceController.listCategories", exception);
        }
    }

    /** Creates a new resource category. */
    public void createCategory() {
        try {
            ConsoleUtils.printHeader("NEW RESOURCE CATEGORY");
            ResourceCategory category = new ResourceCategory();
            category.setName(ConsoleUtils.readRequiredText("Category name: "));
            category.setDescription(ConsoleUtils.readOptionalText("Description (optional): ", null));
            categoryDao.save(category);
            ConsoleUtils.printSuccess("Category '" + category.getName()
                    + "' created with id " + category.getId() + ".");
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not create the category (the name must be unique).");
            LoggerUtil.logError("ResourceController.createCategory", exception);
        }
    }

    // ------------------------------------------------------------------
    // Inventory CRUD
    // ------------------------------------------------------------------

    /** Registers a new resource in the inventory. */
    public void createResource() {
        try {
            ConsoleUtils.printHeader("NEW RESOURCE");
            Resource resource = new Resource();
            resource.setCode(ConsoleUtils.readRequiredText("Resource code (e.g. LIB-001): "));
            if (resourceDao.codeExists(resource.getCode())) {
                ConsoleUtils.printError("A resource with code '" + resource.getCode()
                        + "' already exists. Codes must be unique.");
                return;
            }
            resource.setName(ConsoleUtils.readRequiredText("Resource name: "));

            Integer categoryId = pickCategoryId();
            if (categoryId == null) {
                return;
            }
            resource.setCategoryId(categoryId);

            resource.setDescription(ConsoleUtils.readOptionalText("Description (optional): ", null));
            resource.setAcquisitionDate(ConsoleUtils.readOptionalDate("Acquisition date", LocalDate.now()));
            resource.setPhysicalLocation(ConsoleUtils.readOptionalText(
                    "Physical location (optional): ", null));
            resource.setState(ResourceState.AVAILABLE);

            int id = resourceDao.save(resource);
            ConsoleUtils.printSuccess("Resource '" + resource.getName() + "' registered with id " + id + ".");
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not register the resource.");
            LoggerUtil.logError("ResourceController.createResource", exception);
        }
    }

    /** Lists every resource in the inventory. */
    public void listResources() {
        try {
            printResourceTable(resourceDao.findAll());
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not load the inventory.");
            LoggerUtil.logError("ResourceController.listResources", exception);
        }
    }

    /** Lists resources filtered by category. */
    public void listResourcesByCategory() {
        try {
            Integer categoryId = pickCategoryId();
            if (categoryId == null) {
                return;
            }
            printResourceTable(resourceDao.findByCategory(categoryId));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not load the inventory by category.");
            LoggerUtil.logError("ResourceController.listResourcesByCategory", exception);
        }
    }

    /** Lists resources filtered by state. */
    public void listResourcesByState() {
        try {
            ResourceState state = pickResourceState();
            if (state == null) {
                return;
            }
            printResourceTable(resourceDao.findByState(state));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not load the inventory by state.");
            LoggerUtil.logError("ResourceController.listResourcesByState", exception);
        }
    }

    /** Searches resources by name (partial, case-insensitive). */
    public void searchResourcesByName() {
        try {
            ConsoleUtils.printHeader("SEARCH RESOURCES BY NAME");
            String fragment = ConsoleUtils.readRequiredText("Name fragment: ");
            printResourceTable(resourceDao.findByName(fragment));
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not search the inventory.");
            LoggerUtil.logError("ResourceController.searchResourcesByName", exception);
        }
    }

    /** Updates the descriptive data of an existing resource. */
    public void updateResource() {
        try {
            ConsoleUtils.printHeader("UPDATE RESOURCE");
            Resource resource = pickResource();
            if (resource == null) {
                return;
            }
            System.out.println("Leave a field empty to keep its current value.");
            resource.setCode(ConsoleUtils.readOptionalText(
                    "Code [" + resource.getCode() + "]: ", resource.getCode()));
            String newCode = resource.getCode();
            boolean codeTakenByOther = resourceDao.findAll().stream()
                    .anyMatch(candidate -> candidate.getCode().equalsIgnoreCase(newCode)
                            && candidate.getId() != resource.getId());
            if (codeTakenByOther) {
                ConsoleUtils.printError("Another resource already uses code '" + newCode + "'.");
                return;
            }
            resource.setName(ConsoleUtils.readOptionalText(
                    "Name [" + resource.getName() + "]: ", resource.getName()));

            Integer newCategoryId = pickCategoryId();
            if (newCategoryId != null) {
                resource.setCategoryId(newCategoryId);
            }

            resource.setDescription(ConsoleUtils.readOptionalText(
                    "Description [" + (resource.getDescription() == null ? "-" : resource.getDescription())
                            + "]: ", resource.getDescription()));
            resource.setAcquisitionDate(ConsoleUtils.readOptionalDate(
                    "Acquisition date",
                    resource.getAcquisitionDate() != null ? resource.getAcquisitionDate() : LocalDate.now()));
            resource.setPhysicalLocation(ConsoleUtils.readOptionalText(
                    "Physical location [" + (resource.getPhysicalLocation() == null
                            ? "-" : resource.getPhysicalLocation()) + "]: ", resource.getPhysicalLocation()));

            if (resourceDao.update(resource)) {
                ConsoleUtils.printSuccess("Resource updated.");
            } else {
                ConsoleUtils.printError("The resource could not be updated.");
            }
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not update the resource.");
            LoggerUtil.logError("ResourceController.updateResource", exception);
        }
    }

    /**
     * Changes the state of a resource (send to maintenance, write off,
     * reactivate...). The 'Prestado' state is managed exclusively by loans.
     */
    public void changeResourceState() {
        try {
            ConsoleUtils.printHeader("CHANGE RESOURCE STATE");
            Resource resource = pickResource();
            if (resource == null) {
                return;
            }
            System.out.println("Current state: " + resource.getState().getDbValue());
            ResourceState newState = pickManagedState();
            if (newState == null) {
                return;
            }
            if (newState == ResourceState.WRITTEN_OFF
                    && !ConsoleUtils.confirm("Write off a resource is usually permanent. Continue?")) {
                return;
            }
            if (resourceDao.updateState(resource.getId(), newState)) {
                ConsoleUtils.printSuccess("Resource '" + resource.getCode() + "' is now "
                        + newState.getDbValue() + ".");
            } else {
                ConsoleUtils.printError("The state could not be changed.");
            }
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not change the resource state.");
            LoggerUtil.logError("ResourceController.changeResourceState", exception);
        }
    }

    /**
     * Deletes a resource only when it has no loan history; otherwise it must
     * be written off ('Dado de Baja') instead of deleted.
     */
    public void deleteResource() {
        try {
            ConsoleUtils.printHeader("DELETE RESOURCE");
            Resource resource = pickResource();
            if (resource == null) {
                return;
            }
            if (resourceDao.hasLoanHistory(resource.getId())) {
                ConsoleUtils.printError("Resource '" + resource.getCode()
                        + "' has loan history and cannot be deleted. "
                        + "Use the state 'Dado de Baja' instead.");
                ConsoleUtils.pause();
                return;
            }
            if (ConsoleUtils.confirm("Delete resource '" + resource.getCode() + "'?")) {
                if (resourceDao.delete(resource.getId())) {
                    ConsoleUtils.printSuccess("Resource deleted.");
                } else {
                    ConsoleUtils.printError("The resource could not be deleted.");
                }
            }
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not delete the resource.");
            LoggerUtil.logError("ResourceController.deleteResource", exception);
        }
    }

    // ------------------------------------------------------------------
    // Statistics (Stream API)
    // ------------------------------------------------------------------

    /** Inventory statistics: counts by state and by category. */
    public void showInventoryStats() {
        try {
            List<Resource> resources = resourceDao.findAll();
            if (resources.isEmpty()) {
                ConsoleUtils.printError("The inventory is empty.");
                ConsoleUtils.pause();
                return;
            }
            Map<Integer, String> categoryNames = categoryDao.findAll().stream()
                    .collect(Collectors.toMap(ResourceCategory::getId, ResourceCategory::getName));

            ConsoleUtils.printHeader("INVENTORY STATISTICS");
            System.out.println("Total resources: " + resources.size());

            Map<ResourceState, Long> byState = resources.stream()
                    .collect(Collectors.groupingBy(Resource::getState, Collectors.counting()));
            System.out.println();
            System.out.println("Count by state:");
            byState.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> System.out.printf("  %-18s %d%n",
                            entry.getKey().getDbValue(), entry.getValue()));

            Map<Integer, Long> byCategory = resources.stream()
                    .collect(Collectors.groupingBy(Resource::getCategoryId, Collectors.counting()));
            System.out.println();
            System.out.println("Count by category:");
            byCategory.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> System.out.printf("  %-28s %d%n",
                            categoryNames.getOrDefault(entry.getKey(), "Unknown category"),
                            entry.getValue()));
            ConsoleUtils.pause();
        } catch (SQLException exception) {
            ConsoleUtils.printError("Could not compute the inventory statistics.");
            LoggerUtil.logError("ResourceController.showInventoryStats", exception);
        }
    }

    // ------------------------------------------------------------------
    // Shared helpers
    // ------------------------------------------------------------------

    /** Prints a resource table and resolves category names for display. */
    private void printResourceTable(List<Resource> resources) throws SQLException {
        ConsoleUtils.printHeader("RESOURCE INVENTORY");
        if (resources.isEmpty()) {
            ConsoleUtils.printError("No resources match this filter.");
            ConsoleUtils.pause();
            return;
        }
        Map<Integer, String> categoryNames = categoryDao.findAll().stream()
                .collect(Collectors.toMap(ResourceCategory::getId, ResourceCategory::getName,
                        (first, second) -> first));
        System.out.printf("%-4s | %-12s | %-28s | %-24s | %-16s | %s%n",
                "ID", "CODE", "NAME", "CATEGORY", "STATE", "LOCATION");
        System.out.println("-------------------------------------------------------------------------------"
                + "---------------");
        resources.forEach(resource -> System.out.printf(
                "%-4d | %-12s | %-28s | %-24s | %-16s | %s%n",
                resource.getId(),
                resource.getCode(),
                resource.getName(),
                categoryNames.getOrDefault(resource.getCategoryId(), "Unknown"),
                resource.getState().getDbValue(),
                resource.getPhysicalLocation() == null ? "-" : resource.getPhysicalLocation()));
        ConsoleUtils.pause();
    }

    /** Interactive category picker; null when the user cancels. */
    private Integer pickCategoryId() throws SQLException {
        List<ResourceCategory> categories = categoryDao.findAll();
        if (categories.isEmpty()) {
            ConsoleUtils.printError("There are no resource categories registered yet.");
            return null;
        }
        System.out.println("Available categories:");
        categories.forEach(category -> System.out.printf("  %d. %s%n",
                category.getId(), category.getName()));
        int categoryId = ConsoleUtils.readInt("Category id (0 = back): ", 1, Integer.MAX_VALUE, 0);
        if (categoryId == 0) {
            return null;
        }
        boolean valid = categories.stream().anyMatch(category -> category.getId() == categoryId);
        if (!valid) {
            ConsoleUtils.printError("Category id " + categoryId + " does not exist.");
            return null;
        }
        return categoryId;
    }

    /** Interactive resource picker by id; null when the user cancels. */
    private Resource pickResource() throws SQLException {
        int resourceId = ConsoleUtils.readInt("Resource id (0 = back): ", 1, Integer.MAX_VALUE, 0);
        if (resourceId == 0) {
            return null;
        }
        Resource resource = resourceDao.findById(resourceId);
        if (resource == null) {
            ConsoleUtils.printError("Resource id " + resourceId + " does not exist.");
            return null;
        }
        return resource;
    }

    /** Interactive state picker (loan-managed 'Prestado' is excluded). */
    private ResourceState pickManagedState() {
        List<String> options = List.of(
                ResourceState.AVAILABLE.getDbValue(),
                ResourceState.IN_MAINTENANCE.getDbValue(),
                ResourceState.WRITTEN_OFF.getDbValue());
        String chosen = ConsoleUtils.readOption("New state", options);
        return ResourceState.fromDbValue(chosen);
    }

    /** Interactive state picker including every state. */
    private ResourceState pickResourceState() {
        List<String> options = List.of(ResourceState.AVAILABLE.getDbValue(),
                ResourceState.LOANED.getDbValue(), ResourceState.IN_MAINTENANCE.getDbValue(),
                ResourceState.WRITTEN_OFF.getDbValue());
        String chosen = ConsoleUtils.readOption("Filter by state", options);
        return ResourceState.fromDbValue(chosen);
    }
}
