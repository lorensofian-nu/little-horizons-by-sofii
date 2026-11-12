package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An electronic invoice (stored as a formatted .txt file), mapped from the
 * {@code facturas} table. {@code fileName} / {@code filePath} are persisted in
 * the columns {@code nombre_archivo_txt} / {@code ruta_archivo}.
 */
public class Invoice {

    private int id;
    private String invoiceNumber;
    private int paymentId;
    private LocalDate issueDate;
    private String cufe;
    private String fileName;
    private String filePath;
    private LocalDateTime creationDate;

    public Invoice() {
    }

    public Invoice(int id, String invoiceNumber, int paymentId, LocalDate issueDate,
                   String cufe, String fileName, String filePath, LocalDateTime creationDate) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.paymentId = paymentId;
        this.issueDate = issueDate;
        this.cufe = cufe;
        this.fileName = fileName;
        this.filePath = filePath;
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getCufe() {
        return cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return String.format("Invoice{id=%d, number='%s', payment=%d, date=%s, file='%s'}",
                id, invoiceNumber, paymentId, issueDate, fileName);
    }
}
