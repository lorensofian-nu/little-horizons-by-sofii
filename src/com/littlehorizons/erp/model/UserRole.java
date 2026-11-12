package com.littlehorizons.erp.model;

/** Roles supported by the console application. */
public enum UserRole {
    ADMINISTRATOR("Administrador"),
    TEACHER("Profesor"),
    SECRETARY("Secretaria");

    private final String databaseValue;

    UserRole(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public static UserRole fromDatabaseValue(String value) {
        for (UserRole role : values()) {
            if (role.databaseValue.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role: " + value);
    }

    /** Main-menu authorization policy. */
    public boolean canAccess(int option) {
        if (this == ADMINISTRATOR) {
            return option >= 1 && option <= 7;
        }
        if (this == SECRETARY) {
            return option == 1 || option == 3 || option == 5 || option == 6 || option == 7;
        }
        return option == 2 || option == 3 || option == 4 || option == 6 || option == 7;
    }

    /** Allows access to financial records and financial reports. */
    public boolean canViewFinancialData() {
        return this == ADMINISTRATOR || this == SECRETARY;
    }

    /** Allows a role to see full client contact data in a generated invoice. */
    public boolean canViewSensitiveClientData() {
        return this == ADMINISTRATOR;
    }
}
