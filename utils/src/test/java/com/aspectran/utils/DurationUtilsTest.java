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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DurationUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0ns",
            "999, 999ns",
            "1000, 1µs",
            "1001, 1.001µs",
            "999999, 999.999µs",
            "1000000, 1ms",
            "999999999, 999.999ms",
            "1000000000, 1s",
            "1500000000, 1.500s",
            "59999999999, 59.999s",
            "60000000000, 1m",
            "61500000000, 1m 1s",
            "3599000000000, 59m 59s",
            "3600000000000, 1h",
            "3661000000000, 1h 1m 1s",
            "86400000000000, 1d",
            "90000000000000, 1d 1h",
            "90060000000000, 1d 1h 1m",
            "90061000000000, 1d 1h 1m 1s",
            "-1, 0ns"
    })
    void toHumanReadableNanos(long input, String expected) {
        assertEquals(expected, DurationUtils.toHumanReadableNanos(input));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0ms",
            "1, 1ms",
            "1500, 1.500s",
            "60000, 1m",
            "3600000, 1h",
            "86400000, 1d",
            "-1, 0ms"
    })
    void toHumanReadableMillis(long input, String expected) {
        assertEquals(expected, DurationUtils.toHumanReadableMillis(input));
    }

    @Test
    void toHumanReadableDuration() {
        assertEquals("0ns", DurationUtils.toHumanReadable(null));
        assertEquals("5s", DurationUtils.toHumanReadable(Duration.ofSeconds(5)));
        assertEquals("1m 30s", DurationUtils.toHumanReadable(Duration.ofSeconds(90)));
        assertEquals("2h", DurationUtils.toHumanReadable(Duration.ofHours(2)));
    }

    @ParameterizedTest
    @CsvSource({
            "500ns, 500",
            "10us, 10000",
            "10µs, 10000",
            "500ms, 500000000",
            "1s, 1000000000",
            "1.5s, 1500000000",
            "1m, 60000000000",
            "1h, 3600000000000",
            "1d, 86400000000000"
    })
    void parseDuration(String input, long expectedNanos) {
        Duration duration = DurationUtils.parseDuration(input);
        assertEquals(expectedNanos, duration.toNanos());
    }

    @Test
    void parseDurationWithDefaultUnit() {
        assertEquals(Duration.ofSeconds(10), DurationUtils.parseDuration("10", ChronoUnit.SECONDS));
        assertEquals(Duration.ofMillis(100), DurationUtils.parseDuration("100", ChronoUnit.MILLIS));
        assertEquals(Duration.ofMinutes(5), DurationUtils.parseDuration("5", ChronoUnit.MINUTES));
    }

    @Test
    void parseDurationIso8601() {
        assertEquals(Duration.ofMinutes(15), DurationUtils.parseDuration("PT15M"));
        assertEquals(Duration.ofDays(1), DurationUtils.parseDuration("P1D"));
        assertEquals(Duration.ofSeconds(30), DurationUtils.parseDuration("PT30S"));
    }

    @Test
    void toMillisAndToNanos() {
        assertEquals(5000, DurationUtils.toMillis("5s"));
        assertEquals(1500, DurationUtils.toMillis("1.5s"));
        assertEquals(60000, DurationUtils.toMillis("1m"));

        assertEquals(1000000000L, DurationUtils.toNanos("1s"));
        assertEquals(500L, DurationUtils.toNanos("500ns"));
    }

    @Test
    void testConstants() {
        assertEquals(1_000L, DurationUtils.ONE_MICROSECOND_IN_NANOS);
        assertEquals(1_000_000L, DurationUtils.ONE_MILLISECOND_IN_NANOS);
        assertEquals(1_000_000_000L, DurationUtils.ONE_SECOND_IN_NANOS);
        assertEquals(60_000_000_000L, DurationUtils.ONE_MINUTE_IN_NANOS);
        assertEquals(3_600_000_000_000L, DurationUtils.ONE_HOUR_IN_NANOS);
        assertEquals(86_400_000_000_000L, DurationUtils.ONE_DAY_IN_NANOS);
    }

    @Test
    void parseDuration_withInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> DurationUtils.parseDuration(""));
        assertThrows(IllegalArgumentException.class, () -> DurationUtils.parseDuration("   "));
        assertThrows(IllegalArgumentException.class, () -> DurationUtils.parseDuration("10xyz"));
        assertThrows(IllegalArgumentException.class, () -> DurationUtils.parseDuration("abc"));
        assertThrows(IllegalArgumentException.class, () -> DurationUtils.parseDuration("PT_INVALID"));
    }

}
