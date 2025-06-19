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

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @Test
    void toHumanFriendlyByteSize() {
        long[] sizes = new long[] {1L, 1024L, 10000L, 1234567890L};
        String[] expected = new String[] {"1 B", "1 KB", "9.8 KB", "1.1 GB"};

        for (int i = 0; i < sizes.length; i++) {
            String actual = StringUtils.toHumanFriendlyByteSize(sizes[i]);
            assertEquals(expected[i], actual);
        }
    }

    @Test
    void toMachineFriendlyByteSize() {
        String[] sizes = new String[] {"1K", "1KB", "10M", "10MB", "1.2 GB", "2.4GB", "3.75MB", "1.28KB", "1024", "1024B"};
        long[] expected = new long[] {1024, 1024, 10485760, 10485760, 1288490189, 2576980378L, 3932160, 1310, 1024, 1024};

        for (int i = 0; i < sizes.length; i++) {
            long actual = StringUtils.toMachineFriendlyByteSize(sizes[i]);
            assertEquals(expected[i], actual);
        }
    }

}
