package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A catalog entry of the payment concepts table {@code conceptos_pago}.
 */
public class PaymentConcept {

    private int id;
    private String conceptCode;
    private String conceptName;
    private String description;
    private BigDecimal baseAmount;
    private boolean recurring;
    private boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    public PaymentConcept() {
    }

    public PaymentConcept(int id, String conceptCode, String conceptName, String description,
                          BigDecimal baseAmount, boolean recurring, boolean active,
                          LocalDateTime creationDate, LocalDateTime updateDate) {
        this.id = id;
        this.conceptCode = conceptCode;
        this.conceptName = conceptName;
        this.description = description;
        this.baseAmount = baseAmount;
        this.recurring = recurring;
        this.active = active;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return String.format("PaymentConcept{id=%d, code='%s', name='%s', base=%s, recurring=%s, active=%s}",
                id, conceptCode, conceptName, baseAmount, recurring, active);
    }
}
