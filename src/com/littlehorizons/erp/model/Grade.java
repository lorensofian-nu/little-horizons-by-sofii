package com.littlehorizons.erp.model;

/**
 * Grade entity mapped from the read-only {@code grados} table
 * (Preescolar = level 0, Primero = 1, ... Once = 11).
 */
public class Grade {

    private int id;
    private String name;
    private int numericLevel;

    public Grade() {
        // No-arg constructor.
    }

    public Grade(int id, String name, int numericLevel) {
        this.id = id;
        this.name = name;
        this.numericLevel = numericLevel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumericLevel() {
        return numericLevel;
    }

    public void setNumericLevel(int numericLevel) {
        this.numericLevel = numericLevel;
    }

    @Override
    public String toString() {
        return String.format("ID %-4d | %-20s | Level %d", id, name, numericLevel);
    }
}
