package com.littlehorizons.erp.validation;

import java.math.BigDecimal;

/**
 * Single source of truth for every numeric, length and format limit used by
 * the application. Console prompts, validators and the database CHECK
 * constraints must agree with these values.
 */
public final class ValidationRules {

    /** Academic year bounds (also enforced by database CHECK constraints). */
    public static final int MIN_YEAR = 2000;
    public static final int MAX_YEAR = 2100;

    /** Grade values stored as DECIMAL(2,1) (also enforced by a CHECK). */
    public static final BigDecimal MIN_GRADE = new BigDecimal("0.0");
    public static final BigDecimal MAX_GRADE = new BigDecimal("5.0");
    public static final double MIN_GRADE_DOUBLE = MIN_GRADE.doubleValue();
    public static final double MAX_GRADE_DOUBLE = MAX_GRADE.doubleValue();
    /** Colombian passing threshold used to label the averages. */
    public static final double PASSING_GRADE = 3.0;

    /** Maximum monetary amount accepted (COP), two decimals. */
    public static final BigDecimal MAX_MONEY = new BigDecimal("99999999.99");

    /** Weekly subject hours (also enforced by application validations). */
    public static final int MIN_WEEKLY_HOURS = 1;
    public static final int MAX_WEEKLY_HOURS = 40;

    /** Server-side pagination ceiling. */
    public static final int MAX_PAGE_SIZE = 100;

    /** Password policy. */
    public static final int MIN_PASSWORD_LENGTH = 12;
    /** Days after which a password must be rotated at login. */
    public static final int PASSWORD_EXPIRY_DAYS = 180;

    /** Plausible ages for students of an ordinary school. */
    public static final int MIN_STUDENT_AGE_YEARS = 3;
    public static final int MAX_STUDENT_AGE_YEARS = 25;

    /**
     * Age model for the grade-vs-age coherence check: a child entering
     * Preescolar (numeric level 0) is about 5 years old, and each later
     * grade adds one year.
     */
    public static final int GRADE_BASE_AGE_YEARS = 5;
    /** How many years younger the grade expects. */
    public static final int GRADE_AGE_MIN_TOLERANCE = 2;
    /** How many years older the grade tolerates ( repeaters, migrations ). */
    public static final int GRADE_AGE_MAX_TOLERANCE = 4;

    /** Longest resource loan before it must be renewed. */
    public static final int MAX_LOAN_DAYS = 30;

    private ValidationRules() {
        // Constants class must not be instantiated.
    }
}
