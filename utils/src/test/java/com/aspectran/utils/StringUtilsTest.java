/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

class StringUtilsTest {

    @Test
    void toHumanFriendlyByteSize() {
        for (long l : new long[] {1L, 1024L, 10000L, 1234567890L})
            System.out.println(l + " = " + StringUtils.toHumanFriendlyByteSize(l));
    }

    @Test
    void toMachineFriendlyByteSize() {
        for (String s : "1K, 1KB, 10M, 10MB, 1.2 GB, 2.4GB, 3.75MB, 1.28KB, 1024, 1024B".split(","))
            System.out.println(s.trim() + " = " + StringUtils.toMachineFriendlyByteSize(s));
    }

}
