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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for WildcardEngine.
 */
class WildcardEngineTest {

    @ParameterizedTest
    @MethodSource("wildcardCases")
    @DisplayName("Comprehensive wildcard matching and masking tests")
    void testWildcardMatchingAndMasking(WildcardTestCase testCase) {
        WildcardPattern pattern = WildcardPattern.compile(testCase.pattern, testCase.separator);
        boolean matches = pattern.matches(testCase.input);
        assertEquals(testCase.expectedMatch, matches, "Match failed: " + testCase.description);

        if (matches && testCase.expectedMask != null) {
            String mask = pattern.mask(testCase.input);
            assertEquals(testCase.expectedMask, mask, "Mask failed: " + testCase.description);
        }
    }

    static Stream<WildcardTestCase> wildcardCases() {
        return Stream.of(
                // Ant-style paths (Separator: '/')
                new WildcardTestCase("/static/**", "/static/a/b/c/", '/', true, "a/b/c/", "Double asterisk matches multiple segments"),
                new WildcardTestCase("/static/**/", "/static/a/b/", '/', true, "a/b", "Trailing slash matches directory"),
                new WildcardTestCase("/static/*", "/static/a.jpg", '/', true, "a.jpg", "Single asterisk for a filename"),
                new WildcardTestCase("/static/*", "/static/a/test.jpg", '/', false, null, "Single asterisk does not cross separators"),
                new WildcardTestCase("/static*/*", "/static123/test.jpg", '/', true, "123/test.jpg", "Asterisk in middle of segment"),
                new WildcardTestCase("**/static/**", "a/b/static/a/b/c.jpg", '/', true, "a/b/a/b/c.jpg", "Leading double asterisk"),
                new WildcardTestCase("/a/+/c", "/a/b/c", '/', true, "b", "Plus matches exactly one char"),
                new WildcardTestCase("/a/+/c", "/a/bb/c", '/', false, null, "Plus does not match multiple chars"),
                new WildcardTestCase("/a/+/c", "/a//c", '/', false, null, "Plus requires at least 1 char"),
                new WildcardTestCase("a/**", "a/", '/', true, "", "Double star matches trailing separator"),
                new WildcardTestCase("a/**", "a", '/', true, "", "Double star matches without trailing separator"),
                new WildcardTestCase("**/a", "/a", '/', true, "", "Double star matches leading separator"),
                new WildcardTestCase("**/a", "a", '/', true, "", "Double star matches without separator"),

                // Java Packages (Separator: '.')
                new WildcardTestCase("com.**.Test", "com.aspectran.Test", '.', true, "aspectran", "Strict segment matching"),
                new WildcardTestCase("com.**.Test", "com.aspectranTest", '.', false, null, "Separator missing after double star"),
                new WildcardTestCase("**.Test", "beanTest", '.', false, null, "Separator required before literal"),
                new WildcardTestCase("**.*Test", "beanTest", '.', true, "bean", "Asterisk matches if leading double star is swallowed"),
                new WildcardTestCase("com.aspectran.**.Test*Bean?", "com.aspectran.bean.DependsOnBeanTest$TestBean", '.', false, null, "The bug case: should not match"),

                // Plus (+) vs Question (?) vs Star (*)
                new WildcardTestCase("abc+", "abcd", '.', true, "d", "Plus matches one"),
                new WildcardTestCase("abc+", "abc", '.', false, null, "Plus requires one"),
                new WildcardTestCase("abc?", "abcd", '.', true, "d", "Question matches one"),
                new WildcardTestCase("abc?", "abc", '.', true, "", "Question matches zero"),
                new WildcardTestCase("abc*", "abcde", '.', true, "de", "Star matches many"),
                new WildcardTestCase("abc*", "abc", '.', true, "", "Star matches zero"),

                // Escaping
                new WildcardTestCase("a\\*b", "a*b", true, "", "Escaped star"),
                new WildcardTestCase("a\\?b", "a?b", true, "", "Escaped question mark"),

                // Edge cases
                new WildcardTestCase("", null, true, "", "Empty pattern matches null"),
                new WildcardTestCase("null", null, false, null, "Pattern 'null' does not match null"),

                // Masking cases from real world
                new WildcardTestCase("com.aspectran.core.sample.**.*", "com.aspectran.core.sample.test.TestAdvice", '.', true, "test.TestAdvice", "Real world example 1"),
                new WildcardTestCase("/WEB-INF/views/**/*.jsp", "/WEB-INF/views/user/profile.jsp", '/', true, "user/profile", "Real world example 2"),

                // New complex cases
                new WildcardTestCase("a/**/b", "a/x/y/b", '/', true, "x/y", "Double star between separators"),
                new WildcardTestCase("a/**/b", "a/b", '/', true, "", "Double star between separators matches empty segment"),
                new WildcardTestCase("**/a/**/b", "x/a/y/b", '/', true, "x/y", "Multiple double stars"),
                new WildcardTestCase("**/a/**/b", "a/b", '/', true, "", "Multiple double stars matching nothing"),
                new WildcardTestCase("*/*/*", "a/b/c", '/', true, "a/b/c", "Multiple single stars"),
                new WildcardTestCase("?/?/?", "a/b/c", '/', true, "a/b/c", "Multiple questions"),
                new WildcardTestCase("a**b", "axxxb", '/', true, "xxx", "Double star without separators"),
                new WildcardTestCase("a**b", "ab", '/', true, "", "Double star matching nothing"),
                new WildcardTestCase("**", "a/b/c", '/', true, "a/b/c", "Double star matches everything"),
                new WildcardTestCase("**", "", '/', true, "", "Double star matches empty string"),
                new WildcardTestCase("*", "", '/', true, "", "Single star matches empty string"),
                new WildcardTestCase("+", "a", '/', true, "a", "Plus matches one char"),
                new WildcardTestCase("a+b", "a/b", '/', false, null, "Plus does not match separator"),

                // Overlapping and backtracking
                new WildcardTestCase("*a*b", "ab", true, "", "Backtracking with stars 1"),
                new WildcardTestCase("*a*b", "aab", true, "a", "Backtracking with stars 2"),
                new WildcardTestCase("*a*b", "abab", true, "ba", "Backtracking with stars 3"),
                new WildcardTestCase("**a**b", "aaab", true, "aa", "Backtracking with double stars"),

                // Empty segments
                new WildcardTestCase("a//b", "a//b", '/', true, "", "Literal empty segment"),
                new WildcardTestCase("a/*/b", "a//b", '/', true, "", "Star matching empty segment")
        );
    }

    @Test
    void testPatternWeight() {
        WildcardPattern specificPattern = new WildcardPattern("/dashboard/12/34", '/');
        WildcardPattern generalPattern = new WildcardPattern("/**", '/');

        assertEquals(34.4f, specificPattern.getWeight());
        assertTrue(specificPattern.getWeight() > generalPattern.getWeight(), "More specific patterns should have a higher weight");
    }

    private static class WildcardTestCase {
        final String pattern;
        final String input;
        final char separator;
        final boolean expectedMatch;
        final String expectedMask;
        final String description;

        WildcardTestCase(String pattern, String input, boolean expectedMatch, String expectedMask, String description) {
            this(pattern, input, Character.MIN_VALUE, expectedMatch, expectedMask, description);
        }

        WildcardTestCase(String pattern, String input, char separator, boolean expectedMatch, String expectedMask, String description) {
            this.pattern = pattern;
            this.input = input;
            this.separator = separator;
            this.expectedMatch = expectedMatch;
            this.expectedMask = expectedMask;
            this.description = description;
        }

        @Override
        public String toString() {
            return description + " (Pattern: " + pattern + ", Input: " + input + ")";
        }
    }

}
