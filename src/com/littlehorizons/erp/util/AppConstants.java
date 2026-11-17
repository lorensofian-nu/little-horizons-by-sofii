package com.littlehorizons.erp.util;

import com.littlehorizons.erp.validation.ValidationRules;

/**
 * Global constants for the Little Horizons ERP application.
 * Numeric and format limits live in {@link ValidationRules}; this class only
 * holds branding, folders and deployment constants.
 */
public final class AppConstants {

    /** College legal information used on electronic invoices. */
    public static final String COLLEGE_LEGAL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    public static final String COLLEGE_NIT = "NIT 901.456.789-1";
    public static final String COLLEGE_ADDRESS = "Calle 45 # 23-17, Barrio La Esperanza";
    public static final String COLLEGE_CITY = "Bogotá D.C., Colombia";
    public static final String COLLEGE_PHONE = "(601) 745 8899";
    public static final String COLLEGE_EMAIL = "facturacion@example.invalid";
    public static final String DIAN_RESOLUTION =
            "Resolución DIAN 18764000001234 de 2024 - Régimen de facturación electrónica "
            + "(habilitación simulada con fines académicos)";

    /** Application folders. */
    public static final String INVOICE_FOLDER = "invoices";
    public static final String ERROR_LOG_FILE = "error_log.txt";

    /** Academic limits. Single source of truth: ValidationRules. */
    public static final int MIN_YEAR = ValidationRules.MIN_YEAR;
    public static final int MAX_YEAR = ValidationRules.MAX_YEAR;

    /** Default number of records used to seed the DML script. */
    public static final int MIN_SEED_RECORDS = 30;

    private AppConstants() {
        // Prevent instantiation of the constants class.
    }
}
