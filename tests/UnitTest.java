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
import com.littlehorizons.erp.validation.FormatValidators;
import com.littlehorizons.erp.validation.PasswordPolicy;

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
        testFormatValidators();
        testAmountPattern();
        testPasswordPolicy();
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

    private static void testFormatValidators() {
        // Identity documents per type
        assertTrue(FormatValidators.isValidDocumentNumber("TI", "1090123456"),
                "TI with 10 digits is valid");
        assertTrue(FormatValidators.isValidDocumentNumber("CC", "1018427734"),
                "CC with 10 digits is valid");
        assertTrue(!FormatValidators.isValidDocumentNumber("CC", "111111111"),
                "CC with repeated digits must be rejected");
        assertTrue(!FormatValidators.isValidDocumentNumber("TI", "12345"),
                "TI with 5 digits must be rejected");
        assertTrue(!FormatValidators.isValidDocumentNumber("CC", "abc123"),
                "CC must be digits only");
        assertTrue(FormatValidators.isValidDocumentNumber("CE", "AB1234"),
                "CE alphanumeric is valid");
        assertTrue(FormatValidators.isValidDocumentNumber("Pasaporte", "AB12345678"),
                "Passport alphanumeric is valid");
        assertTrue(FormatValidators.isValidDocumentNumber("PPT", "PPT12345"),
                "PPT alphanumeric is valid");

        // Phones
        assertTrue(FormatValidators.isValidPhone("3014567890"), "Mobile is valid");
        assertTrue(FormatValidators.isValidPhone("6017458899"), "Landline is valid");
        assertTrue(!FormatValidators.isValidPhone("123"), "Short phone is invalid");
        assertTrue(!FormatValidators.isValidPhone("5112345678"), "Foreign-style phone is invalid");
        assertTrue(FormatValidators.isValidOptionalPhone(null),
                "Null optional phone passes");
        assertTrue(!FormatValidators.isValidOptionalPhone("123"),
                "Invalid optional phone must fail");

        // E-mails
        assertTrue(FormatValidators.isValidEmail("ana.maria@colegio.edu.co"),
                "Common email is valid");
        assertTrue(!FormatValidators.isValidEmail("no..dots@mail.com"),
                "Consecutive dots must be rejected");
        assertTrue(!FormatValidators.isValidEmail("plainaddress"),
                "Email without domain must be rejected");

        // Name normalization
        assertTrue("Ana María Pérez".equals(FormatValidators.normalizeName("  ana   maría  PÉREZ ")),
                "Names are collapsed and capitalized");
        assertTrue("Jose de la Cruz".equals(FormatValidators.normalizeName("jose de la cruz")),
                "Name particles stay lowercase");

        // Receipt format
        assertTrue(FormatValidators.isValidReceiptNumber("REC-2026-000123"),
                "Automatic receipt format is valid");
        assertTrue(!FormatValidators.isValidReceiptNumber("recibo de la plaza"),
                "Free-form receipts must be rejected");
    }

    private static void testAmountPattern() {
        assertTrue(FormatValidators.isAmountText("150000"), "Plain amount is valid");
        assertTrue(FormatValidators.isAmountText("1500.50"), "Dot decimals are valid");
        assertTrue(FormatValidators.isAmountText("1500,50"), "Comma decimals are valid");
        assertTrue(!FormatValidators.isAmountText("1E5"), "Scientific notation must be rejected");
        assertTrue(!FormatValidators.isAmountText("1.000,50"), "Two separators must be rejected");
        assertTrue(!FormatValidators.isAmountText("-50"), "Signs must be rejected");
        assertTrue(!FormatValidators.isAmountText("123456789012"),
                "Amounts beyond the limit must be rejected");
    }

    private static void testPasswordPolicy() {
        assertTrue(PasswordPolicy.violation("short12".toCharArray(), "sofi").isPresent(),
                "Short passwords are rejected");
        assertTrue(PasswordPolicy.violation("onlyletterspassword".toCharArray(), "sofi").isPresent(),
                "All-letter passwords are rejected");
        assertTrue(PasswordPolicy.violation("123456789012".toCharArray(), "sofi").isPresent(),
                "All-digit passwords are rejected");
        assertTrue(PasswordPolicy.violation("sofiMyPassword12".toCharArray(), "sofi").isPresent(),
                "Passwords containing the username are rejected");
        assertTrue(PasswordPolicy.violation("password123456".toCharArray(), "sofi").isPresent(),
                "Well-known passwords are rejected");
        assertTrue(PasswordPolicy.violation("Escuela#2026Horizons".toCharArray(), "sofi").isEmpty(),
                "A strong mixed password passes");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
