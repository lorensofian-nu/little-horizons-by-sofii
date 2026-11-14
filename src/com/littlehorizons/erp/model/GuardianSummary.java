package com.littlehorizons.erp.model;

import com.littlehorizons.erp.util.PrivacyUtils;

/**
 * Lightweight read-only projection of a guardian, used by the financial module
 * to pick a guardian and to fill the client block of electronic invoices.
 */
public class GuardianSummary {

    private int id;
    private String documentType;
    private String documentNumber;
    private String fullName;
    private String address;

    public GuardianSummary() {
    }

    public GuardianSummary(int id, String documentType, String documentNumber,
                           String fullName, String address) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.fullName = fullName;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /** "CC 123456789" style document description. */
    public String getDocumentDescription() {
        return documentType + " " + documentNumber;
    }

    /** Document description safe for non-administrative console output. */
    public String getMaskedDocumentDescription() {
        return documentType + " " + PrivacyUtils.maskIdentifier(documentNumber);
    }

    @Override
    public String toString() {
        return String.format("#%d %s (%s)", id, fullName, getMaskedDocumentDescription());
    }
}
