package com.littlehorizons.erp.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Very small logging utility that appends error traces to {@code error_log.txt}
 * at the project root, as required by the project specification.
 */
public final class LoggerUtil {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            }
            Files.writeString(logFile, line, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Logging must never break the application flow.
        }
    }
}
