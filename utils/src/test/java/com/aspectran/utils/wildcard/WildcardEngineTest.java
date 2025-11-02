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
package com.aspectran.utils.wildcard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integrated test case for the wildcard pattern engine.
 */
class WildcardEngineTest {

    // A record to hold the data for each parameterized test case.
    static record WildcardTestCase(String pattern, String input, char separator, boolean expectedMatch, String expectedMask, String description) {
        WildcardTestCase(String pattern, String input, boolean expectedMatch, String description) {
            this(pattern, input, '/', expectedMatch, null, description);
        }
    }

    static Stream<WildcardTestCase> wildcardCases() {
        return Stream.of(
                // Cases from WildcardMatcherTest (Ant-style paths)
                new WildcardTestCase("/static/**", "/static/a/b/c/", true, "Double asterisk matches multiple segments"),
                new WildcardTestCase("/static/**/", "/static/a/b/", true, "Trailing slash in pattern matches directory"),
                new WildcardTestCase("/static/*", "/static/a.jpg", true, "Single asterisk for a filename"),
                new WildcardTestCase("/static/*", "/static/a/test.jpg", false, "Single asterisk does not cross separators"),
                new WildcardTestCase("/static*/*", "/static123/test.jpg", true, "Asterisk in the middle of a segment"),
                new WildcardTestCase("/static*/*", "/static12/a/test.jpg", false, "Segmented asterisk does not cross separators"),
                new WildcardTestCase("/static*/**/b/*", "/static/a/b/c.jpg", true, "Combined asterisks for complex path"),
                new WildcardTestCase("**/static/**", "a/b/static/a/b/c.jpg", true, "Leading double asterisk matches any prefix"),
                new WildcardTestCase("/static-?/**", "/static-a/a.jpg", true, "Question mark for single character"),
                new WildcardTestCase("/static-?/???.jpg", "/static-a/abc.jpg", true, "Multiple question marks for fixed length"),
                new WildcardTestCase("", null, true, "Empty pattern matches null input"),
                new WildcardTestCase("null", null, false, "Non-empty pattern does not match null input"),
                new WildcardTestCase("**.*", "MyClass", '.', false, null, "'**.*' should not match a string without the separator"),

                // New edge cases
                new WildcardTestCase("/a/+/c", "/a/b/c", true, "Plus wildcard should match one or more characters"),
                new WildcardTestCase("/a/+/c", "/a//c", false, "Plus wildcard should not match an empty segment"),
                new WildcardTestCase("a\\*b", "a*b", true, "Escaped star should match literal star"),
                new WildcardTestCase("a\\?b", "a?b", true, "Escaped question mark should match literal question mark"),
                new WildcardTestCase("a/**", "a/", true, "Double star should match trailing separator"),
                new WildcardTestCase("**/a", "/a", true, "Double star should match leading separator"),

                // Cases from WildcardMaskerTest (Java packages)
                new WildcardTestCase("**.*", "com.aspectran.core.embedded.ABean", '.', true, "com.aspectran.core.embedded.ABean", "Masking with '**.*' should return full string"),
                new WildcardTestCase("**", "..com.aspectran.core.embedded.ABean", '.', true, "com.aspectran.core.embedded.ABean", "Masking with '**' should return trimmed string"),
                new WildcardTestCase("com.aspectran.core.**.*", "com.aspectran.core.embedded.ABean", '.', true, "embedded.ABean", "Masking with leading package and wildcards"),
                new WildcardTestCase("com.aspectran.core.embedded.*", "com.aspectran.core.embedded.ABean", '.', true, "ABean", "Masking with single trailing asterisk"),
                new WildcardTestCase("com.aspectran.core.embedded.**", "com.aspectran.core.embedded.ABean", '.', true, "ABean", "Masking with double trailing asterisk"),
                new WildcardTestCase("com.aspectran.core.embedded.**.*", "com.aspectran.core.embedded.ABean", '.', true, "ABean", "Masking with combined trailing wildcards")
        );
    }

    @ParameterizedTest
    @MethodSource("wildcardCases")
    void testWildcardMatchingAndMasking(WildcardTestCase tc) {
        WildcardPattern pattern = WildcardPattern.compile(tc.pattern, tc.separator);

        // Test matching
        assertEquals(tc.expectedMatch, pattern.matches(tc.input), "Match failed: " + tc.description);

        // Test masking if an expected mask is provided
        if (tc.expectedMask != null) {
            assertEquals(tc.expectedMask, pattern.mask(tc.input), "Mask failed: " + tc.description);
        }
    }

    @Test
    void testIncludeExcludePatterns() {
        String[] includePatterns = {
                "/aaa/b*/**",
                "/aaa/c*"
        };
        String[] excludePatterns = {
                "/aaa/bb*",
                "/aaa/cc*"
        };

        IncludeExcludeWildcardPatterns patterns = IncludeExcludeWildcardPatterns.of(includePatterns, excludePatterns, '/');
        assertTrue(patterns.matches("/aaa/bbb/ccc"), "Should include '/aaa/bbb/ccc'");
        assertFalse(patterns.matches("/aaa/ccc"), "Should exclude '/aaa/ccc' due to exclude pattern");
        assertTrue(patterns.matches("/aaa/bcd/ccc"), "Should include '/aaa/bcd/ccc'");
        assertTrue(patterns.matches("/aaa/cde"), "Should include '/aaa/cde'");
        assertFalse(patterns.matches("/aaa/bba"), "Should exclude '/aaa/bba'");
    }

    @Test
    void testPatternWeight() {
        WildcardPattern specificPattern = new WildcardPattern("/dashboard/12/34", '/');
        WildcardPattern generalPattern = new WildcardPattern("/**", '/');

        assertEquals(34.4f, specificPattern.getWeight());
        assertTrue(specificPattern.getWeight() > generalPattern.getWeight(), "More specific patterns should have a higher weight");
    }

}
