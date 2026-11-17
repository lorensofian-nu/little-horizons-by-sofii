package com.littlehorizons.erp.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Very small logging utility that appends error traces to {@code error_log.txt}
 * at the project root, as required by the project specification.
 *
 * <p>The file is size-bounded: when it exceeds {@link #MAX_LOG_BYTES} it is
 * rotated to {@code error_log.1.txt} (replacing the previous rotation), so the
 * log can never grow without limit. Entries never contain exception messages,
 * SQL or personal data.</p>
 */
public final class LoggerUtil {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Maximum size of the active log file before rotation (~1 MiB). */
    private static final long MAX_LOG_BYTES = 1024 * 1024;
    private static final String ROTATED_SUFFIX = ".1.txt";

    private LoggerUtil() {
        // Utility class must not be instantiated.
    }

    /** Appends an error entry to the log file. Never throws. */
    public static void logError(String context, Throwable error) {
        // Exception messages may contain SQL fragments or personal data.
        write(context + " | " + error.getClass().getSimpleName());
    }

    /** Appends an informational entry to the log file. Never throws. */
    public static void logInfo(String message) {
        write(message);
    }

    private static synchronized void write(String message) {
        String line = LocalDateTime.now().format(TIMESTAMP_FORMAT) + " | " + message
                + System.lineSeparator();
        try {
            Path logFile = Paths.get(AppConstants.ERROR_LOG_FILE);
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            } else {
                rotateIfTooLarge(logFile);
            }
            Files.writeString(logFile, line, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Logging must never break the application flow.
        }
    }

    /** Moves an oversized log file to {@code error_log.1.txt} (one generation). */
    private static void rotateIfTooLarge(Path logFile) throws IOException {
        if (Files.size(logFile) < MAX_LOG_BYTES) {
            return;
        }
        Path rotated = Paths.get(AppConstants.ERROR_LOG_FILE + ROTATED_SUFFIX);
        Files.move(logFile, rotated, StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }
}
