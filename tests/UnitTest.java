package com.littlehorizons.erp;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.GuardianSummary;
import com.littlehorizons.erp.model.GuardianDocumentType;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
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
        testGuardianDocumentTypes();
        testRolePolicy();
        testNonAdministratorInvoiceMasking();
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

    private static void testGuardianDocumentTypes() {
        assertTrue(GuardianDocumentType.fromDisplayValue("Pasaporte")
                        == GuardianDocumentType.PASSPORT,
                "Passport must be accepted for guardians");
        assertTrue(GuardianDocumentType.fromDisplayValue("PPT")
                        == GuardianDocumentType.PPT,
                "PPT must be accepted for guardians");
        assertTrue(GuardianDocumentType.fromDisplayValue("cedula de extranjeria")
                        == GuardianDocumentType.CE,
                "Foreign ID label must be accepted without accents");
        assertTrue("CE".equals(GuardianDocumentType.CE.getDatabaseValue()),
                "Existing foreign ID database values must remain compatible");
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

    private static void testNonAdministratorInvoiceMasking() {
        GuardianSummary guardian = new GuardianSummary(1, "CC", "1234567890",
                "Test Guardian", "TEST_ADDRESS");
        Payment payment = new Payment();
        payment.setAmountPaid(java.math.BigDecimal.ONE);
        PaymentConcept concept = new PaymentConcept();
        concept.setConceptName("Test concept");
        String content = InvoiceGenerator.rebuildContent(payment, concept, guardian,
                "FAC-000001", LocalDate.of(2026, 1, 1), "test-cufe", false);
        assertTrue(!content.contains("CC 1234567890"),
                "Non-administrator invoice must not expose the guardian document");
        assertTrue(content.contains("CC ******7890"),
                "Non-administrator invoice must contain a masked guardian document");
        String legacyContent = InvoiceGenerator.redactSensitiveClientData(
                "Documento:   CC 1234567890\nDirección:   TEST_ADDRESS");
        assertTrue(!legacyContent.contains("1234567890")
                        && !legacyContent.contains("TEST_ADDRESS"),
                "Legacy invoice display must redact both document and address");
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
