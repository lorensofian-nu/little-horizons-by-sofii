package com.littlehorizons.erp.util;

/**
 * Global constants for the Little Horizons ERP application.
 */
public final class AppConstants {

    /** College legal information used on electronic invoices. */
    public static final String COLLEGE_LEGAL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    public static final String COLLEGE_NIT = "NIT 901.456.789-1";
    public static final String COLLEGE_ADDRESS = "Calle 45 # 23-17, Barrio La Esperanza";
    public static final String COLLEGE_CITY = "Bogotá D.C., Colombia";
    public static final String COLLEGE_PHONE = "(601) 745 8899";
    public static final String COLLEGE_EMAIL = "facturacion@littlehorizons.edu.co";
    public static final String DIAN_RESOLUTION =
            "Resolución DIAN 18764000001234 de 2024 - Régimen de facturación electrónica "
            + "(habilitación simulada con fines académicos)";

    /** Application folders. */
    public static final String INVOICE_FOLDER = "invoices";
    public static final String ERROR_LOG_FILE = "error_log.txt";

    /** Academic limits. */
    public static final double MIN_GRADE = 0.0;
    public static final double MAX_GRADE = 5.0;
    public static final int MIN_YEAR = 2000;
    public static final int MAX_YEAR = 2100;

    /** Default number of records used to seed the DML script. */
    public static final int MIN_SEED_RECORDS = 30;

    private AppConstants() {
        // Prevent instantiation of the constants class.
    }
}
