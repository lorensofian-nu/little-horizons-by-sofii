package com.littlehorizons.erp;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.UserRole;
import com.littlehorizons.erp.util.InvoiceGenerator;
import com.littlehorizons.erp.util.PasswordHasher;
import com.littlehorizons.erp.util.PrivacyUtils;

import java.time.LocalDate;

/** Lightweight dependency-free regression tests for security and formatting rules. */
public final class UnitTest {

    private UnitTest() {
    }

    public static void main(String[] args) throws Exception {
        testPasswordHashing();
        testPrivacyMasking();
        testRolePolicy();
        testCufeDeterminism();
        testInvoicePathBoundary();
        System.out.println("Unit tests passed.");
    }

    private static void testPasswordHashing() {
        char[] password = "local-test-password".toCharArray();
        String hash = PasswordHasher.hash(password);
        assertTrue(PasswordHasher.verify(password, hash), "Password should verify");
        assertTrue(!PasswordHasher.verify("wrong-password".toCharArray(), hash),
                "Wrong password must fail");
    }

    private static void testPrivacyMasking() {
        assertTrue("******7890".equals(PrivacyUtils.maskIdentifier("1234567890")),
                "Identifier must be masked");
        assertTrue("-".equals(PrivacyUtils.maskIdentifier(null)),
                "Null identifier must be safe");
    }

    private static void testRolePolicy() {
        assertTrue(UserRole.ADMINISTRATOR.canViewSensitiveClientData(),
                "Administrator should see full invoice client data");
        assertTrue(!UserRole.SECRETARY.canViewSensitiveClientData(),
                "Secretary invoice output must redact client contact data");
        assertTrue(!UserRole.TEACHER.canViewFinancialData(),
                "Teacher must not see financial reports");
    }

    private static void testCufeDeterminism() {
        String first = InvoiceGenerator.computeCufe("FAC-000001", LocalDate.of(2026, 1, 1));
        String second = InvoiceGenerator.computeCufe("FAC-000001", LocalDate.of(2026, 1, 1));
        assertTrue(first.equals(second) && first.length() == 64,
                "CUFE must be deterministic SHA-256");
    }

    private static void testInvoicePathBoundary() {
        Invoice invoice = new Invoice();
        invoice.setFileName("../outside.txt");
        try {
            InvoiceGenerator.resolveInvoicePath(invoice);
            throw new AssertionError("Path traversal must be rejected");
        } catch (java.io.IOException expected) {
            // Expected security rejection.
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
