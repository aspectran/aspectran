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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            "-1, 0ms"
    })
    void toHumanReadableMillis(long input, String expected) {
        assertEquals(expected, DurationUtils.toHumanReadableMillis(input));
    }

}
