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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataSizeUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0 B",
            "500, 500 B",
            "1023, 1023 B",
            "1024, 1 KB",
            "1536, 1.5 KB",
            "1048576, 1 MB",
            "1234567890, 1.1 GB",
            "-1024, -1 KB",
            " -1536, -1.5 KB"
    })
    void toHumanFriendlyByteSize(long input, String expected) {
        assertEquals(expected, DataSizeUtils.toHumanFriendlyByteSize(input));
    }

    @ParameterizedTest
    @CsvSource({
            "1k, 1024",
            "1KB, 1024",
            "10m, 10485760",
            "10MB, 10485760",
            "1.2 GB, 1288490189",
            "2.4GB, 2576980378",
            "3.75MB, 3932160",
            "1.28KB, 1311",
            "1024, 1024",
            "1024B, 1024",
            "  1 G  , 1073741824",
            "-2kb, -2048"
    })
    void toMachineFriendlyByteSize(String input, long expected) {
        assertEquals(expected, DataSizeUtils.toMachineFriendlyByteSize(input));
    }

    @Test
    void toMachineFriendlyByteSize_withInvalidInput() {
        assertThrows(NumberFormatException.class, () -> DataSizeUtils.toMachineFriendlyByteSize("1.2.3 GB"));
        assertThrows(NumberFormatException.class, () -> DataSizeUtils.toMachineFriendlyByteSize("1 ZB"));
        assertThrows(NumberFormatException.class, () -> DataSizeUtils.toMachineFriendlyByteSize("KB"));
        assertThrows(NumberFormatException.class, () -> DataSizeUtils.toMachineFriendlyByteSize(""));
        assertThrows(NumberFormatException.class, () -> DataSizeUtils.toMachineFriendlyByteSize("  "));
    }

}
