/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utility methods for formatting and parsing durations.
 * Supports converting nanoseconds and milliseconds into human-readable strings,
 * as well as parsing human-friendly duration strings into {@link Duration} instances.
 */
public class DurationUtils {

    /** Number of nanoseconds in one microsecond. */
    public static final long ONE_MICROSECOND_IN_NANOS = 1_000L;

    /** Number of nanoseconds in one millisecond. */
    public static final long ONE_MILLISECOND_IN_NANOS = 1_000_000L;

    /** Number of nanoseconds in one second. */
    public static final long ONE_SECOND_IN_NANOS = 1_000_000_000L;

    /** Number of nanoseconds in one minute. */
    public static final long ONE_MINUTE_IN_NANOS = 60 * ONE_SECOND_IN_NANOS;

    /** Number of nanoseconds in one hour. */
    public static final long ONE_HOUR_IN_NANOS = 60 * ONE_MINUTE_IN_NANOS;

    /** Number of nanoseconds in one day. */
    public static final long ONE_DAY_IN_NANOS = 24 * ONE_HOUR_IN_NANOS;

    /** Pattern for parsing duration strings (e.g. "500ms", "10s", "1.5m", "2h", "1d"). */
    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "^\\s*([+-]?[0-9]+(?:\\.[0-9]+)?)\\s*([a-zA-Zµμ]*)\\s*$"
    );

    /**
     * This class cannot be instantiated.
     */
    private DurationUtils() {
    }

    /**
     * Parses a human-readable duration string into a {@link Duration}.
     * Supports formats like "500ms", "10s", "1.5m", "2h", "1d", "100us", "500ns",
     * as well as standard ISO-8601 duration formats (e.g. "PT15M").
     * If no unit is specified, milliseconds are assumed by default.
     * @param text the duration string to parse
     * @return the parsed {@link Duration}
     * @throws IllegalArgumentException if the format is invalid
     */
    @NonNull
    public static Duration parseDuration(@NonNull String text) {
        return parseDuration(text, ChronoUnit.MILLIS);
    }

    /**
     * Parses a human-readable duration string into a {@link Duration},
     * using the specified {@code defaultUnit} when no unit is present.
     * @param text the duration string to parse
     * @param defaultUnit the unit to use if no unit is present in the text (defaults to MILLIS if null)
     * @return the parsed {@link Duration}
     * @throws IllegalArgumentException if the format is invalid
     */
    @NonNull
    public static Duration parseDuration(@NonNull String text, @Nullable ChronoUnit defaultUnit) {
        Assert.notNull(text, "text must not be null");
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Duration text must not be empty");
        }

        // Support ISO-8601 format (e.g., "PT15M", "P1D")
        if (trimmed.startsWith("P") || trimmed.startsWith("p") ||
                trimmed.startsWith("+P") || trimmed.startsWith("+p") ||
                trimmed.startsWith("-P") || trimmed.startsWith("-p")) {
            try {
                return Duration.parse(trimmed);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid ISO-8601 duration format: " + text, ex);
            }
        }

        Matcher matcher = DURATION_PATTERN.matcher(trimmed);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration string: \"" + text + "\"");
        }

        double value = Double.parseDouble(matcher.group(1));
        String unitStr = matcher.group(2).toLowerCase(Locale.ROOT);

        ChronoUnit unit = resolveUnit(unitStr, defaultUnit != null ? defaultUnit : ChronoUnit.MILLIS);
        long nanos;
        switch (unit) {
            case NANOS -> nanos = Math.round(value);
            case MICROS -> nanos = Math.round(value * ONE_MICROSECOND_IN_NANOS);
            case MILLIS -> nanos = Math.round(value * ONE_MILLISECOND_IN_NANOS);
            case SECONDS -> nanos = Math.round(value * ONE_SECOND_IN_NANOS);
            case MINUTES -> nanos = Math.round(value * ONE_MINUTE_IN_NANOS);
            case HOURS -> nanos = Math.round(value * ONE_HOUR_IN_NANOS);
            case DAYS -> nanos = Math.round(value * ONE_DAY_IN_NANOS);
            default -> throw new IllegalArgumentException("Unsupported duration unit: " + unit);
        }

        return Duration.ofNanos(nanos);
    }

    /**
     * Parses a duration string and returns the value in milliseconds.
     * @param text the duration string to parse
     * @return the duration in milliseconds
     * @throws IllegalArgumentException if the format is invalid
     */
    public static long toMillis(@NonNull String text) {
        return parseDuration(text, ChronoUnit.MILLIS).toMillis();
    }

    /**
     * Parses a duration string and returns the value in nanoseconds.
     * @param text the duration string to parse
     * @return the duration in nanoseconds
     * @throws IllegalArgumentException if the format is invalid
     */
    public static long toNanos(@NonNull String text) {
        return parseDuration(text, ChronoUnit.NANOS).toNanos();
    }

    /**
     * Converts a {@link Duration} to a human-readable string.
     * @param duration the duration to format (may be {@code null})
     * @return a human-readable string representation of the duration
     */
    @NonNull
    public static String toHumanReadable(@Nullable Duration duration) {
        if (duration == null) {
            return "0ns";
        }
        return toHumanReadableNanos(duration.toNanos());
    }

    /**
     * Converts a duration in nanoseconds to a human-readable string.
     * The format is dynamically chosen based on the duration (ns, µs, ms, s, m, h, d).
     * @param nanos the duration in nanoseconds
     * @return a human-readable string representation of the duration
     */
    @NonNull
    public static String toHumanReadableNanos(long nanos) {
        if (nanos < 0) {
            nanos = 0;
        }

        if (nanos < ONE_MICROSECOND_IN_NANOS) {
            return nanos + "ns";
        }
        if (nanos < ONE_MILLISECOND_IN_NANOS) {
            if (nanos % ONE_MICROSECOND_IN_NANOS == 0) {
                return (nanos / ONE_MICROSECOND_IN_NANOS) + "µs";
            }
            return String.format(Locale.ROOT, "%.3fµs", nanos / (double)ONE_MICROSECOND_IN_NANOS);
        }
        if (nanos < ONE_SECOND_IN_NANOS) {
            if (nanos % ONE_MILLISECOND_IN_NANOS == 0) {
                return (nanos / ONE_MILLISECOND_IN_NANOS) + "ms";
            }
            long millis = nanos / ONE_MILLISECOND_IN_NANOS;
            long micros = (nanos % ONE_MILLISECOND_IN_NANOS) / ONE_MICROSECOND_IN_NANOS;
            return String.format(Locale.ROOT, "%d.%03dms", millis, micros);
        }
        if (nanos < ONE_MINUTE_IN_NANOS) {
            if (nanos % ONE_SECOND_IN_NANOS == 0) {
                return (nanos / ONE_SECOND_IN_NANOS) + "s";
            }
            long secs = nanos / ONE_SECOND_IN_NANOS;
            long millis = (nanos % ONE_SECOND_IN_NANOS) / ONE_MILLISECOND_IN_NANOS;
            return String.format(Locale.ROOT, "%d.%03ds", secs, millis);
        }

        long totalMinutes = nanos / ONE_MINUTE_IN_NANOS;
        long remainingNanosAfterMinutes = nanos % ONE_MINUTE_IN_NANOS;
        long seconds = remainingNanosAfterMinutes / ONE_SECOND_IN_NANOS;

        if (totalMinutes < 60) { // Less than 1 hour
            if (seconds == 0) {
                return totalMinutes + "m";
            }
            return String.format(Locale.ROOT, "%dm %ds", totalMinutes, seconds);
        }

        long totalHours = totalMinutes / 60;
        long remainingMinutes = totalMinutes % 60;

        if (totalHours < 24) { // Less than 1 day
            if (remainingMinutes == 0 && seconds == 0) {
                return totalHours + "h";
            }
            if (seconds == 0) {
                return String.format(Locale.ROOT, "%dh %dm", totalHours, remainingMinutes);
            }
            return String.format(Locale.ROOT, "%dh %dm %ds", totalHours, remainingMinutes, seconds);
        }

        long days = totalHours / 24;
        long remainingHours = totalHours % 24;

        if (remainingHours == 0 && remainingMinutes == 0 && seconds == 0) {
            return days + "d";
        }
        if (remainingMinutes == 0 && seconds == 0) {
            return String.format(Locale.ROOT, "%dd %dh", days, remainingHours);
        }
        if (seconds == 0) {
            return String.format(Locale.ROOT, "%dd %dh %dm", days, remainingHours, remainingMinutes);
        }
        return String.format(Locale.ROOT, "%dd %dh %dm %ds", days, remainingHours, remainingMinutes, seconds);
    }

    /**
     * Converts a duration in milliseconds to a human-readable string.
     * This method converts milliseconds to nanoseconds and then calls
     * {@link #toHumanReadableNanos(long)}.
     * @param millis the duration in milliseconds
     * @return a human-readable string representation of the duration
     */
    @NonNull
    public static String toHumanReadableMillis(long millis) {
        if (millis < 0) {
            millis = 0;
        }
        if (millis == 0) {
            return "0ms";
        }
        return toHumanReadableNanos(millis * ONE_MILLISECOND_IN_NANOS);
    }

    @NonNull
    private static ChronoUnit resolveUnit(@NonNull String unitStr, @NonNull ChronoUnit defaultUnit) {
        if (unitStr.isEmpty()) {
            return defaultUnit;
        }
        return switch (unitStr) {
            case "ns", "nano", "nanos", "nanosecond", "nanoseconds" -> ChronoUnit.NANOS;
            case "µs", "μs", "us", "micro", "micros", "microsecond", "microseconds" -> ChronoUnit.MICROS;
            case "ms", "milli", "millis", "millisecond", "milliseconds" -> ChronoUnit.MILLIS;
            case "s", "sec", "secs", "second", "seconds" -> ChronoUnit.SECONDS;
            case "m", "min", "mins", "minute", "minutes" -> ChronoUnit.MINUTES;
            case "h", "hr", "hrs", "hour", "hours" -> ChronoUnit.HOURS;
            case "d", "day", "days" -> ChronoUnit.DAYS;
            default -> throw new IllegalArgumentException("Unknown duration unit: \"" + unitStr + "\"");
        };
    }

}
