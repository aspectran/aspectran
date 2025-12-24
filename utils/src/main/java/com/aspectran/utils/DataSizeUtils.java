package com.aspectran.utils;

import org.jspecify.annotations.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for converting and formatting data sizes (byte counts).
 * Provides methods to convert byte counts to human-readable strings and vice versa,
 * supporting various units (B, KB, MB, GB, TB, PB, EB).
 */
public class DataSizeUtils {

    /**
     * Regular expression pattern for parsing human-friendly byte size strings.
     * It captures the numeric value and the optional unit (e.g., "100", "1KB", "1.5 MB").
     * Supports K, M, G, T, P, E prefixes, case-insensitively, with or without 'B' suffix,
     * and optional leading/trailing spaces.
     */
    private static final Pattern PATTERN = Pattern.compile("^\\s*(-?[0-9.]+)\\s*([KMGTPE]?)B?\\s*$", Pattern.CASE_INSENSITIVE);

    /**
     * Array of character prefixes for byte units (Kilobyte, Megabyte, Gigabyte, Terabyte, Petabyte, Exabyte).
     * Used for formatting human-readable byte sizes.
     * Index 0: K (Kilobyte)
     * Index 1: M (Megabyte)
     * Index 2: G (Gigabyte)
     * Index 3: T (Terabyte)
     * Index 4: P (Petabyte)
     * Index 5: E (Exabyte)
     */
    private static final String UNITS = "KMGTPE";

    /**
     * This class cannot be instantiated.
     */
    private DataSizeUtils() {
    }

    /**
     * Converts a byte size into a human-friendly format (e.g., 1024 -> "1.0 KB").
     * <p>This method uses binary prefixes (powers of 1024) but uses common SI symbols (KB, MB, GB).
     * For example, 1024 bytes is formatted as "1.0 KB".</p>
     * <pre>
     * DataSizeUtils.toHumanFriendlyByteSize(0L)     = "0 B"
     * DataSizeUtils.toHumanFriendlyByteSize(500L)    = "500 B"
     * DataSizeUtils.toHumanFriendlyByteSize(1024L)   = "1.0 KB"
     * DataSizeUtils.toHumanFriendlyByteSize(1536L)   = "1.5 KB"
     * DataSizeUtils.toHumanFriendlyByteSize(1048576L) = "1.0 MB"
     * DataSizeUtils.toHumanFriendlyByteSize(-1024L)  = "-1.0 KB"
     * DataSizeUtils.toHumanFriendlyByteSize(Long.MAX_VALUE) = "8.0 EB" // Approx.
     * </pre>
     * @param bytes the number of bytes
     * @return a human-friendly byte size string (includes units like B, KB, MB, GB, etc.)
     */
    @NonNull
    public static String toHumanFriendlyByteSize(long bytes) {
        if (bytes == Long.MIN_VALUE) {
            return "-8 EB"; // Approx. value for Long.MIN_VALUE, which is -2^63
        }
        if (bytes < 0) {
            return "-" + toHumanFriendlyByteSize(-bytes);
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        // This clever logic finds the right unit index (K=1, M=2, etc.)
        // by using the number of leading zeros of the binary representation of the number.
        int z = (63 - Long.numberOfLeadingZeros(bytes)) / 10;
        double value = (double)bytes / (1L << (z * 10));
        String format = (value % 1.0 == 0 ? "%.0f %cB" : "%.1f %cB");
        return String.format(format, value, UNITS.charAt(z - 1));
    }

    /**
     * Converts a human-friendly byte size string (e.g., "1KB", "10MB") into the number of bytes.
     * <p>The method supports various units (B, K, KB, M, MB, G, GB, T, TB, P, PB, E, EB)
     * and is case-insensitive. It also tolerates optional spaces between the number and the unit.
     * Decimal values are supported (e.g., "1.5MB").</p>
     * <pre>
     * DataSizeUtils.toMachineFriendlyByteSize("0")      = 0L
     * DataSizeUtils.toMachineFriendlyByteSize("500B")   = 500L
     * DataSizeUtils.toMachineFriendlyByteSize("1KB")    = 1024L
     * DataSizeUtils.toMachineFriendlyByteSize("1.5M")   = 1572864L
     * DataSizeUtils.toMachineFriendlyByteSize("10 gB")  = 10737418240L
     * DataSizeUtils.toMachineFriendlyByteSize("-1KB")   = -1024L
     * </pre>
     * @param bytes the human-friendly byte size string to parse
     * @return the number of bytes
     * @throws NumberFormatException if the string format is invalid or value exceeds long max
     */
    public static long toMachineFriendlyByteSize(@NonNull String bytes) {
        Matcher matcher = PATTERN.matcher(bytes.trim());
        if (!matcher.matches()) {
            String msg = "Size must be specified as bytes (B), " +
                    "kilobytes (K or KB), megabytes (M or MB), gigabytes (G or GB), " +
                    "terabytes (T or TB), petabytes (P or PB), or exabytes (E or EB). " +
                    "Examples: \"1024\", \"1KB\", \"10M\", \"10MB\", \"100G\", \"100GB\". " +
                    "Invalid format: \"" + bytes + "\"";
            throw new NumberFormatException(msg);
        }

        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();
        long multiplier = 1L;

        if (!unit.isEmpty()) {
            // The fall-through is intentional to calculate the multiplier
            switch (unit.charAt(0)) {
                case 'E': multiplier *= 1024L;
                case 'P': multiplier *= 1024L;
                case 'T': multiplier *= 1024L;
                case 'G': multiplier *= 1024L;
                case 'M': multiplier *= 1024L;
                case 'K': multiplier *= 1024L;
            }
        }

        return Math.round(value * multiplier);
    }

}
