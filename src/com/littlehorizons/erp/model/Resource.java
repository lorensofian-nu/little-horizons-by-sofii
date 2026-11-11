package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A physical resource of the school (library book, laboratory equipment,
 * sports gear, ...), mapped from table {@code recursos}.
 */
public class Resource {

    private int id;
    private String code;
    private String name;
    private int categoryId;
    private String description;
    private LocalDate acquisitionDate;
    private String physicalLocation;
    private ResourceState state;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

    public Resource() {
        // No-arg constructor required by the project conventions.
    }

    public Resource(int id, String code, String name, int categoryId, String description,
                    LocalDate acquisitionDate, String physicalLocation, ResourceState state,
                    LocalDateTime creationDate, LocalDateTime lastUpdateDate) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
        this.acquisitionDate = acquisitionDate;
        this.physicalLocation = physicalLocation;
        this.state = state;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public ResourceState getState() {
        return state;
    }

    public void setState(ResourceState state) {
        this.state = state;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String toString() {
        return "Resource{id=" + id + ", code='" + code + "', name='" + name
                + "', categoryId=" + categoryId + ", state=" + state + "}";
    }
}
