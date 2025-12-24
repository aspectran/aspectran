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

/**
 * Static utility methods for displaying durations in a human-readable format.
 */
public class DurationUtils {

    private static final long ONE_MICROSECOND_IN_NANOS = 1_000L;

    private static final long ONE_MILLISECOND_IN_NANOS = 1_000_000L;

    private static final long ONE_SECOND_IN_NANOS = 1_000_000_000L;

    private static final long ONE_MINUTE_IN_NANOS = 60 * ONE_SECOND_IN_NANOS;

    /**
     * This class cannot be instantiated.
     */
    private DurationUtils() {
    }

    /**
     * Converts a duration in nanoseconds to a human-readable string.
     * The format is dynamically chosen based on the duration (ns, µs, ms, s, m, h).
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
            return String.format("%.3fµs", nanos / (double)ONE_MICROSECOND_IN_NANOS);
        }
        if (nanos < ONE_SECOND_IN_NANOS) {
            if (nanos % ONE_MILLISECOND_IN_NANOS == 0) {
                return (nanos / ONE_MILLISECOND_IN_NANOS) + "ms";
            }
            long millis = nanos / ONE_MILLISECOND_IN_NANOS;
            long micros = (nanos % ONE_MILLISECOND_IN_NANOS) / ONE_MICROSECOND_IN_NANOS;
            return String.format("%d.%03dms", millis, micros);
        }
        if (nanos < ONE_MINUTE_IN_NANOS) {
            if (nanos % ONE_SECOND_IN_NANOS == 0) {
                return (nanos / ONE_SECOND_IN_NANOS) + "s";
            }
            long secs = nanos / ONE_SECOND_IN_NANOS;
            long millis = (nanos % ONE_SECOND_IN_NANOS) / ONE_MILLISECOND_IN_NANOS;
            return String.format("%d.%03ds", secs, millis);
        }

        long totalMinutes = nanos / ONE_MINUTE_IN_NANOS;
        long remainingNanosAfterMinutes = nanos % ONE_MINUTE_IN_NANOS;
        long seconds = remainingNanosAfterMinutes / ONE_SECOND_IN_NANOS;

        if (totalMinutes < 60) { // Less than 1 hour
            if (seconds == 0) {
                return totalMinutes + "m";
            }
            return String.format("%dm %ds", totalMinutes, seconds);
        }

        long hours = totalMinutes / 60;
        long remainingMinutes = totalMinutes % 60;

        if (remainingMinutes == 0 && seconds == 0) {
            return hours + "h";
        }
        if (seconds == 0) {
            return String.format("%dh %dm", hours, remainingMinutes);
        }
        return String.format("%dh %dm %ds", hours, remainingMinutes, seconds);
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

}
