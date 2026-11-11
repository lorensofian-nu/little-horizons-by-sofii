package com.littlehorizons.erp.model;

import java.time.LocalDateTime;

/**
 * A resource category (library / laboratory), mapped from table
 * {@code categorias_recurso}.
 */
public class ResourceCategory {

    private int id;
    private String name;
    private String description;
    private LocalDateTime creationDate;

    public ResourceCategory() {
        // No-arg constructor required by the project conventions.
    }

    public ResourceCategory(int id, String name, String description, LocalDateTime creationDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "ResourceCategory{id=" + id + ", name='" + name + "'}";
    }
}
