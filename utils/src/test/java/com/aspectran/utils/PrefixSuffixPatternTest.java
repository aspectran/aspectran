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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for PrefixSuffixPattern.
 */
class PrefixSuffixPatternTest {

    @Test
    @DisplayName("Test valid patterns that should create an instance")
    void testValidPatterns() {
        PrefixSuffixPattern p1 = PrefixSuffixPattern.of("pre*suf");
        assertNotNull(p1);
        assertEquals("pre", p1.getPrefix());
        assertEquals("suf", p1.getSuffix());

        PrefixSuffixPattern p2 = PrefixSuffixPattern.of("*suf");
        assertNotNull(p2);
        assertEquals("", p2.getPrefix());
        assertEquals("suf", p2.getSuffix());

        PrefixSuffixPattern p3 = PrefixSuffixPattern.of("pre*");
        assertNotNull(p3);
        assertEquals("pre", p3.getPrefix());
        assertEquals("", p3.getSuffix());

        PrefixSuffixPattern p4 = PrefixSuffixPattern.of("*");
        assertNotNull(p4);
        assertEquals("", p4.getPrefix());
        assertEquals("", p4.getSuffix());
    }

    @Test
    @DisplayName("Test invalid inputs that should return null")
    void testNullReturnCases() {
        assertNull(PrefixSuffixPattern.of(null));
        assertNull(PrefixSuffixPattern.of(""));
        assertNull(PrefixSuffixPattern.of("no-separator"));
    }

    @Test
    @DisplayName("Test ambiguous patterns that should throw IllegalArgumentException")
    void testExceptionCases() {
        assertThrows(IllegalArgumentException.class, () -> {
            PrefixSuffixPattern.of("pre*in*suf");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PrefixSuffixPattern.of("pre**suf");
        });
    }

    @Test
    @DisplayName("Test the enclose method with various patterns")
    void testEnclose() {
        PrefixSuffixPattern p1 = PrefixSuffixPattern.of("pre*suf");
        assertNotNull(p1);
        assertEquals("preINsuf", p1.enclose("IN"));

        PrefixSuffixPattern p2 = PrefixSuffixPattern.of("*suf");
        assertNotNull(p2);
        assertEquals("INsuf", p2.enclose("IN"));

        PrefixSuffixPattern p3 = PrefixSuffixPattern.of("pre*");
        assertNotNull(p3);
        assertEquals("preIN", p3.enclose("IN"));

        PrefixSuffixPattern p4 = PrefixSuffixPattern.of("*");
        assertNotNull(p4);
        assertEquals("IN", p4.enclose("IN"));
    }

}
