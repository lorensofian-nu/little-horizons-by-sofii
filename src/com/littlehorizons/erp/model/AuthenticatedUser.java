package com.littlehorizons.erp.model;

/** Minimal authenticated identity kept in memory for the current process. */
public final class AuthenticatedUser {

    private final int id;
    private final String username;
    private final UserRole role;
    private final boolean mustChangePassword;

    public AuthenticatedUser(int id, String username, UserRole role, boolean mustChangePassword) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean mustChangePassword() {
        return mustChangePassword;
    }
}
