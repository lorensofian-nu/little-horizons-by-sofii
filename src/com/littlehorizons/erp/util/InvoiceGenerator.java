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
     * Generates the invoice file and returns the populated {@link Invoice}
     * (id = 0; the caller persists it and assigns the generated id).
     *
     * @param payment       confirmed payment (must already have its generated id)
     * @param concept       payment concept sold in this invoice
     * @param client        guardian used as invoice client (may be null)
     * @param invoiceNumber sequential number, e.g. {@code FAC-000001}
     * @throws IOException when the file cannot be created or written
     */
    public static Invoice generateInvoice(Payment payment, PaymentConcept concept,
                                          GuardianSummary client, String invoiceNumber)
            throws IOException {

        LocalDate issueDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now();
        String cufe = computeCufe(invoiceNumber, issueDate);

        String fileName = "factura_" + invoiceNumber.substring("FAC-".length()) + ".txt";

        Path folder = Paths.get(AppConstants.INVOICE_FOLDER);
        Files.createDirectories(folder);
        Path filePath = folder.resolve(fileName).toAbsolutePath();

        String content = buildContent(payment, concept, client, invoiceNumber, issueDate, cufe);
        Files.writeString(filePath, content, StandardCharsets.UTF_8);

        return new Invoice(0, invoiceNumber, payment.getId(), issueDate, cufe,
                fileName, filePath.toString(), LocalDateTime.now());
    }

    /**
     * Rebuilds the invoice document content without writing any file (used to
     * regenerate a lost file with the original number, date and CUFE).
     */
    public static String rebuildContent(Payment payment, PaymentConcept concept, GuardianSummary client,
                                        String invoiceNumber, LocalDate issueDate, String cufe) {
        return buildContent(payment, concept, client, invoiceNumber, issueDate, cufe);
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
                                       String invoiceNumber, LocalDate issueDate, String cufe) {
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
            lines.add("Dirección:   " + (client.getAddress() == null || client.getAddress().isBlank()
                    ? "No registrada" : client.getAddress()));
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
