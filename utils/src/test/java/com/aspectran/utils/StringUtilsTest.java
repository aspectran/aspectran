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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Nested
    class CheckTests {
        @Test
        void isEmpty() {
            assertTrue(StringUtils.isEmpty(null));
            assertTrue(StringUtils.isEmpty(""));
            assertFalse(StringUtils.isEmpty(" "));
            assertFalse(StringUtils.isEmpty("hello"));
        }

        @Test
        void hasLength() {
            assertFalse(StringUtils.hasLength(null));
            assertFalse(StringUtils.hasLength(""));
            assertTrue(StringUtils.hasLength(" "));
            assertTrue(StringUtils.hasLength("hello"));
        }

        @Test
        void hasText() {
            assertFalse(StringUtils.hasText(null));
            assertFalse(StringUtils.hasText(""));
            assertFalse(StringUtils.hasText(" "));
            assertTrue(StringUtils.hasText(" hello "));
        }
    }

    @Nested
    class TrimTests {
        @Test
        void trimWhitespace() {
            assertEquals("hello", StringUtils.trimWhitespace("  hello  "));
            assertEquals("hello world", StringUtils.trimWhitespace("  hello world  "));
            assertEquals("", StringUtils.trimWhitespace("   "));
            assertNull(StringUtils.trimWhitespace(null));
        }

        @Test
        void trimAllWhitespace() {
            assertEquals("helloworld", StringUtils.trimAllWhitespace("  hello world  "));
            assertEquals("helloworld", StringUtils.trimAllWhitespace("hello world"));
            assertEquals("", StringUtils.trimAllWhitespace("   "));
        }

        @ParameterizedTest
        @CsvSource({
                "'  hello  ', ',', '  hello  '",
                "'--hello--', '-', 'hello--'",
                "'__hello__', '_', 'hello__'"
        })
        void trimLeadingCharacter(String input, char trimChar, String expected) {
            assertEquals(expected, StringUtils.trimLeadingCharacter(input, trimChar));
        }

        @ParameterizedTest
        @CsvSource({
                "'  hello  ', ',', '  hello  '",
                "'--hello--', '-', '--hello'",
                "'__hello__', '_', '__hello'"
        })
        void trimTrailingCharacter(String input, char trimChar, String expected) {
            assertEquals(expected, StringUtils.trimTrailingCharacter(input, trimChar));
        }
    }

    @Nested
    class SplitAndJoinTests {
        @Test
        void split() {
            assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a,b,c", ","));
            assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.split("a,,b,c", ","));
            assertArrayEquals(new String[]{"a", "b", "c", ""}, StringUtils.split("a,b,c,", ","));
            assertArrayEquals(new String[]{"", "a", "b", "c"}, StringUtils.split(",a,b,c", ","));
        }

        @Test
        void splitWithMultiCharDelimiter() {
            assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a||b||c", "||"));
            assertArrayEquals(new String[]{"a", "", "c"}, StringUtils.split("a||||c", "||"));
        }

        @Test
        void splitWithComma() {
            assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.splitWithComma(" a, b, c "));
        }

        @Test
        void tokenize() {
            assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.tokenize("a,b;c", ",;"));
            // StringTokenizer skips empty tokens
            assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.tokenize("a,,b,c", ","));
        }

        @Test
        void join() {
            assertEquals("a,b,c", StringUtils.join(new String[]{"a", "b", "c"}, ","));
            assertEquals("a", StringUtils.join(new String[]{"a"}, ","));
            assertEquals("", StringUtils.join(new String[]{}, ","));
        }

        @Test
        void joinWithCommas() {
            assertEquals("a, b, c", StringUtils.joinWithCommas(new String[]{"a", "b", "c"}));
            assertEquals("a, b, c", StringUtils.joinWithCommas(Arrays.asList("a", "b", "c")));
        }
    }

    @Nested
    class ReplaceTests {
        @Test
        void replace() {
            assertEquals("he--o", StringUtils.replace("hello", "l", "-"));
            assertEquals("axbyc", StringUtils.replace("abc", new String[]{"a", "b"}, new String[]{"ax", "by"}));
        }

        @Test
        void replaceWithOverlappingResults() {
            assertEquals("axbyc", StringUtils.replace("abc", new String[]{"a", "b"}, new String[]{"ax", "by"}));
            // After 'a' -> 'b', the string becomes "bbc". The next replacement for 'b' should apply to both.
            assertEquals("xxc", StringUtils.replace("abc", new String[]{"a", "b"}, new String[]{"b", "x"}));
        }

        @Test
        void replaceLast() {
            assertEquals("helloo", StringUtils.replaceLast("hello-o", "-", ""));
        }
    }

    @Nested
    class SearchTests {
        @ParameterizedTest
        @CsvSource({
                "hello, l, 2",
                "hello, o, 1",
                "aspectran, a, 2"
        })
        void search(String input, String search, int expected) {
            assertEquals(expected, StringUtils.search(input, search));
        }

        @ParameterizedTest
        @CsvSource({
                "aaaaa, aa, 2", // non-overlapping
                "ababab, aba, 1"
        })
        void searchWithNonOverlappingPatterns(String input, String search, int expected) {
            assertEquals(expected, StringUtils.search(input, search));
        }
    }

    @Nested
    class ConversionTests {
        @Test
        void nullAndEmpty() {
            assertEquals("", StringUtils.nullToEmpty(null));
            assertEquals("hi", StringUtils.nullToEmpty("hi"));
            assertNull(StringUtils.emptyToNull(""));
            assertNull(StringUtils.emptyToNull(null));
            assertEquals("hi", StringUtils.emptyToNull("hi"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void toStringArray_withEmptyOrNullCollections(Collection<String> input) {
            assertEquals(0, StringUtils.toStringArray(input).length);
        }

        @Test
        void toStringArray_withValidCollection() {
            assertArrayEquals(new String[]{"a", "b"}, StringUtils.toStringArray(Arrays.asList("a", "b")));
        }
    }

}
