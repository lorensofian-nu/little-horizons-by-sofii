package com.littlehorizons.erp.util;

import com.littlehorizons.erp.model.GuardianSummary;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds the simulated Colombian electronic invoice as a formatted .txt file
 * (no external libraries). The simulated CUFE is a SHA-256 digest of
 * {@code numeroFactura|NIT|fechaEmision}, and the "QR" is a deterministic
 * ASCII block pattern derived from the same digest bits.
 */
public final class InvoiceGenerator {

    private static final NumberFormat MONEY_FORMAT =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));

    private static final String HEAVY_LINE = "═";
    private static final String THIN_LINE  = "─";

    static {
        MONEY_FORMAT.setMinimumFractionDigits(2);
        MONEY_FORMAT.setMaximumFractionDigits(2);
    }

    private InvoiceGenerator() {
        // Utility class must not be instantiated.
    }

    /** Formats a monetary amount in Colombian style: {@code $350.000,00}. */
    public static String formatMoney(double amount) {
        return "$" + MONEY_FORMAT.format(amount);
    }

    /** Formats a monetary amount in Colombian style from a BigDecimal. */
    public static String formatMoney(java.math.BigDecimal amount) {
        return formatMoney(amount == null ? 0.0 : amount.doubleValue());
    }

    /**
     * Builds the {@link Invoice} record (number, CUFE, file name and target
     * path) without touching the database or the file system. The caller
     * persists it in the same transaction as its payment and then writes the
     * file with {@link #writeInvoiceFile}.
     *
     * @param payment       confirmed payment (its generated id is optional here)
     * @param concept       payment concept sold in this invoice
     * @param client        guardian used as invoice client (may be null)
     * @param invoiceNumber sequential number, e.g. {@code FAC-000001}
     */
    public static Invoice buildInvoice(Payment payment, PaymentConcept concept,
                                       GuardianSummary client, String invoiceNumber) {

        LocalDate issueDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now();
        String cufe = computeCufe(invoiceNumber, issueDate);

        String fileName = "factura_" + invoiceNumber.substring("FAC-".length()) + ".txt";

        Path folder = Paths.get(AppConstants.INVOICE_FOLDER);
        Path filePath = folder.resolve(fileName).toAbsolutePath();

        return new Invoice(0, invoiceNumber, payment.getId(), issueDate, cufe,
                fileName, filePath.toString(), LocalDateTime.now());
    }

    /**
     * Writes the invoice .txt file for an already built and persisted invoice.
     * Kept outside the database transaction on purpose: if the file cannot be
     * written the payment and invoice rows stay consistent and the file can be
     * regenerated later from the financial menu.
     *
     * @throws IOException when the file cannot be created or written
     */
    public static void writeInvoiceFile(Invoice invoice, Payment payment, PaymentConcept concept,
                                        GuardianSummary client) throws IOException {
        writeInvoiceFile(invoice, payment, concept, client, true);
    }

    /** Writes an invoice with optional client contact details. */
    public static void writeInvoiceFile(Invoice invoice, Payment payment, PaymentConcept concept,
                                        GuardianSummary client,
                                        boolean includeSensitiveClientData) throws IOException {
        Path folder = Paths.get(AppConstants.INVOICE_FOLDER).toAbsolutePath().normalize();
        Files.createDirectories(folder);
        restrictToOwner(folder, true);
        String content = buildContent(payment, concept, client, invoice.getInvoiceNumber(),
                invoice.getIssueDate(), invoice.getCufe(), includeSensitiveClientData);
        writePrivateText(resolveInvoicePath(invoice), content);
    }

    /** Writes private invoice content and applies owner-only permissions where supported. */
    public static void writePrivateText(Path path, String content) throws IOException {
        Path normalized = path.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent == null) {
            throw new IOException("Invoice path has no parent directory.");
        }
        Files.createDirectories(parent);
        if (Files.isSymbolicLink(normalized)) {
            throw new IOException("Refusing to write through a symbolic link.");
        }
        Path temporary = Files.createTempFile(parent, ".invoice-", ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            restrictToOwner(temporary, false);
            Files.move(temporary, normalized, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            restrictToOwner(normalized, false);
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(temporary, normalized, StandardCopyOption.REPLACE_EXISTING);
            restrictToOwner(normalized, false);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void restrictToOwner(Path path, boolean directory) {
        try {
            java.util.Set<PosixFilePermission> permissions = new java.util.HashSet<>();
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            if (directory) {
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
            }
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows and other non-POSIX file systems use their native ACLs.
        }
    }

    /** Resolves only file names inside the application invoice directory. */
    public static Path resolveInvoicePath(Invoice invoice) throws IOException {
        Path folder = Paths.get(AppConstants.INVOICE_FOLDER).toAbsolutePath().normalize();
        Path target = folder.resolve(invoice.getFileName()).normalize();
        if (!target.startsWith(folder)) {
            throw new IOException("Invoice path is outside the invoice directory.");
        }
        return target;
    }

    /**
     * Rebuilds the invoice document content without writing any file (used to
     * regenerate a lost file with the original number, date and CUFE).
     */
    public static String rebuildContent(Payment payment, PaymentConcept concept, GuardianSummary client,
                                        String invoiceNumber, LocalDate issueDate, String cufe) {
        return rebuildContent(payment, concept, client, invoiceNumber, issueDate, cufe, true);
    }

    /** Rebuilds content while applying the current user's client-data policy. */
    public static String rebuildContent(Payment payment, PaymentConcept concept, GuardianSummary client,
                                        String invoiceNumber, LocalDate issueDate, String cufe,
                                        boolean includeSensitiveClientData) {
        return buildContent(payment, concept, client, invoiceNumber, issueDate, cufe,
                includeSensitiveClientData);
    }

    /** Simulated CUFE: SHA-256 of {@code numeroFactura|NIT|fechaEmision(ISO)}. */
    public static String computeCufe(String invoiceNumber, LocalDate issueDate) {
        String input = invoiceNumber + "|" + AppConstants.COLLEGE_NIT + "|" + issueDate;
        return sha256Hex(input);
    }

    /** Hexadecimal SHA-256 digest of the given text. */
    public static String sha256Hex(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 is mandatory in every JDK; this branch is unreachable.
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }

    // ------------------------------------------------------------------
    // Invoice document layout.
    // ------------------------------------------------------------------

    private static String buildContent(Payment payment, PaymentConcept concept, GuardianSummary client,
                                       String invoiceNumber, LocalDate issueDate, String cufe,
                                       boolean includeSensitiveClientData) {
        List<String> lines = new ArrayList<>();
        String width64 = repeat(HEAVY_LINE, 64);

        lines.add(width64);
        lines.add(center(AppConstants.COLLEGE_LEGAL_NAME, 64));
        lines.add(center("FACTURA ELECTRÓNICA DE VENTA (simulada)", 64));
        lines.add(width64);
        lines.add("NIT:        " + AppConstants.COLLEGE_NIT);
        lines.add("Dirección:  " + AppConstants.COLLEGE_ADDRESS);
        lines.add("Ciudad:     " + AppConstants.COLLEGE_CITY);
        lines.add("Teléfono:   " + AppConstants.COLLEGE_PHONE);
        lines.add("Correo:     " + AppConstants.COLLEGE_EMAIL);
        lines.add("Resolución: " + AppConstants.DIAN_RESOLUTION);
        lines.add(repeat(THIN_LINE, 64));
        lines.add("Factura No.:     " + invoiceNumber);
        lines.add("Fecha emisión:   " + issueDate);
        lines.add("CUFE (simulado): " + cufe);
        lines.add(repeat(THIN_LINE, 64));
        lines.add("CLIENTE");
        if (client != null) {
            lines.add("Nombre:      " + client.getFullName());
            lines.add("Documento:   " + client.getDocumentDescription());
            String address = client.getAddress() == null || client.getAddress().isBlank()
                    ? "No registrada" : client.getAddress();
            lines.add("Dirección:   " + (includeSensitiveClientData ? address : "[REDACTADA]"));
        } else {
            lines.add("Nombre:      (acudiente no registrado en el pago)");
            lines.add("Documento:   N/A");
            lines.add("Dirección:   N/A");
        }
        lines.add(repeat(THIN_LINE, 64));
        lines.add("DETALLE");

        String description = (concept.getConceptCode() != null ? concept.getConceptCode() + " - " : "")
                + concept.getConceptName();
        int quantity = 1;
        String unitValue = formatMoney(payment.getAmountPaid());
        String subtotalValue = formatMoney(payment.getAmountPaid());

        lines.add(String.format("%-34s %5s %14s %14s", "DESCRIPCIÓN", "CANT", "VALOR UNIT.", "SUBTOTAL"));
        lines.add(String.format("%-34s %5d %14s %14s",
                truncate(description, 34), quantity, unitValue, subtotalValue));

        double subtotal = payment.getAmountPaid().doubleValue();
        double iva = subtotal * 0.0; // School services are VAT-exempt in Colombia.

        lines.add(repeat(THIN_LINE, 64));
        lines.add(padLeft("Subtotal:", 48) + padLeft(formatMoney(subtotal), 18));
        lines.add(padLeft("IVA (0%):", 48) + padLeft(formatMoney(iva), 18));
        lines.add(padLeft("TOTAL:", 48) + padLeft(formatMoney(subtotal), 18));
        lines.add(repeat(THIN_LINE, 64));
        lines.add("Forma de pago:  " + (payment.getMethod() == null ? "N/A" : payment.getMethod().getLabel()));
        lines.add("Recibo No.:     " + (payment.getReceiptNumber() == null ? "N/A" : payment.getReceiptNumber()));
        lines.add("Año académico:  " + (payment.getAcademicYear() == null ? "N/A" : payment.getAcademicYear())
                + "   Periodo: " + (payment.getPeriod() == null ? "N/A" : payment.getPeriod().getLabel()));
        if (payment.getNotes() != null && !payment.getNotes().isBlank()) {
            lines.add("Notas:          " + payment.getNotes());
        }
        lines.add(repeat(THIN_LINE, 64));
        lines.add("CÓDIGO QR SIMULADO");
        lines.add(buildSimulatedQr(cufe));
        lines.add(repeat(THIN_LINE, 64));
        lines.add(center("Documento simulado con fines académicos - no es factura real.", 64));
        lines.add(center("Generado por ERP Little Horizons el "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 64));
        lines.add(width64);
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }

    /** Deterministic 13x13 ASCII block pattern derived from the CUFE hash bits. */
    private static String buildSimulatedQr(String cufe) {
        int size = 13;
        byte[] hash = sha256Hex(cufe).getBytes(StandardCharsets.US_ASCII);
        StringBuilder builder = new StringBuilder();

        // Top border.
        builder.append("    ").append(repeat("██", size + 2)).append(System.lineSeparator());
        for (int row = 0; row < size; row++) {
            builder.append("    ").append("██");
            for (int col = 0; col < size; col++) {
                boolean dark = isFinderModule(row, col, size)
                        || ((hash[(row * size + col) % hash.length] >> ((row + col) % 8)) & 1) == 1;
                builder.append(dark ? "██" : "  ");
            }
            builder.append("██").append(System.lineSeparator());
        }
        // Bottom border.
        builder.append("    ").append(repeat("██", size + 2));
        return builder.toString();
    }

    /** Simulated QR "finder patterns" in three corners (7x7 square targets). */
    private static boolean isFinderModule(int row, int col, int size) {
        int[][] origins = {{0, 0}, {0, size - 7}, {size - 7, 0}};
        for (int[] origin : origins) {
            int r = row - origin[0];
            int c = col - origin[1];
            if (r >= 0 && r < 7 && c >= 0 && c < 7) {
                boolean border = r == 0 || r == 6 || c == 0 || c == 6;
                boolean core = r >= 2 && r <= 4 && c >= 2 && c <= 4;
                return border || core;
            }
        }
        return false;
    }

    private static String center(String text, int width) {
        String value = text.length() > width ? truncate(text, width) : text;
        int padding = Math.max(0, (width - value.length()) / 2);
        return repeat(" ", padding) + value;
    }

    private static String padLeft(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return repeat(" ", width - text.length()) + text;
    }

    private static String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }

    private static String repeat(String token, int times) {
        return token.repeat(Math.max(0, times));
    }
}
