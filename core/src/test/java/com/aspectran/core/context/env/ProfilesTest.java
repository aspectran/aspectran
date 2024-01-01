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
package com.aspectran.core.context.env;

import com.aspectran.utils.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created: 12/30/23</p>
 */
class ProfilesTest {

    @Test
    void malformedExpressions() {
        assertMalformed(() -> Profiles.of("!"));
        assertMalformed(() -> Profiles.of("abc!"));
        assertMalformed(() -> Profiles.of("("));
        assertMalformed(() -> Profiles.of(")"));
        assertMalformed(() -> Profiles.of("()"));
        assertMalformed(() -> Profiles.of("!()"));
        assertMalformed(() -> Profiles.of("a & b | c"));
        assertMalformed(() -> Profiles.of("(a, b), c)"));
        assertMalformed(() -> Profiles.of("(a, b), c]"));
        assertMalformed(() -> Profiles.of("[(a, b), c)"));
        assertMalformed(() -> Profiles.of("((a, b), c]"));
    }

    private static void assertMalformed(Supplier<Profiles> supplier) {
        try {
            supplier.get();
        } catch (IllegalArgumentException e) {
            System.out.println(e);
            assertTrue(e.getMessage().startsWith("Malformed profile expression"));
        }
    }

    @Test
    void ofSingleElement() {
        Profiles profiles = Profiles.of("aspectran");
        assertTrue(profiles.matches(activeProfiles("aspectran")));
        assertFalse(profiles.matches(activeProfiles("framework")));
    }

    @Test
    void ofSingleInvertedElement() {
        Profiles profiles = Profiles.of("!aspectran");
        assertFalse(profiles.matches(activeProfiles("aspectran")));
        assertTrue(profiles.matches(activeProfiles("framework")));
    }

    @Test
    void ofMultipleElements() {
        Profiles profiles = Profiles.of("aspectran, framework");
        assertTrue(profiles.matches(activeProfiles("aspectran")));
        assertTrue(profiles.matches(activeProfiles("framework")));
        assertFalse(profiles.matches(activeProfiles("java")));
    }

    @Test
    void ofMultipleElementsWithInverted() {
        Profiles profiles = Profiles.of("!aspectran, framework");
        assertFalse(profiles.matches(activeProfiles("aspectran")));
        assertTrue(profiles.matches(activeProfiles("aspectran", "framework")));
        assertTrue(profiles.matches(activeProfiles("framework")));
        assertTrue(profiles.matches(activeProfiles("java")));
    }

    @Test
    void ofMultipleElementsAllInverted() {
        Profiles profiles = Profiles.of("!aspectran, !framework");
        assertTrue(profiles.matches(activeProfiles("aspectran")));
        assertTrue(profiles.matches(activeProfiles("framework")));
        assertTrue(profiles.matches(activeProfiles("java")));
        assertFalse(profiles.matches(activeProfiles("aspectran", "framework")));
        assertFalse(profiles.matches(activeProfiles("aspectran", "framework", "java")));
    }

    @Test
    void ofSingleExpression() {
        Profiles profiles = Profiles.of("(aspectran)");
        assertTrue(profiles.matches(activeProfiles("aspectran")));
        assertFalse(profiles.matches(activeProfiles("framework")));
    }

    @Test
    void ofSingleExpressionInverted() {
        Profiles profiles = Profiles.of("!(aspectran)");
        assertFalse(profiles.matches(activeProfiles("aspectran")));
        assertTrue(profiles.matches(activeProfiles("framework")));
    }

    @Test
    void ofSingleInvertedExpression() {
        Profiles profiles = Profiles.of("(!aspectran)");
        assertFalse(profiles.matches(activeProfiles("aspectran")));
        assertTrue(profiles.matches(activeProfiles("framework")));
    }

    @Test
    void ofOrExpression() {
        Profiles profiles = Profiles.of("[aspectran, framework]");
        assertOrExpression(profiles);
    }

    @Test
    void ofOrExpressionWithoutSpaces() {
        Profiles profiles = Profiles.of("[aspectran,framework]");
        assertOrExpression(profiles);
    }

    private void assertOrExpression(Profiles profiles) {
        assertTrue(profiles.matches(activeProfiles("aspectran")));
        assertTrue(profiles.matches(activeProfiles("framework")));
        assertTrue(profiles.matches(activeProfiles("aspectran", "framework")));
        assertFalse(profiles.matches(activeProfiles("java")));
    }

    @Test
    void ofAndExpression() {
        Profiles profiles = Profiles.of("(aspectran, framework)");
        assertAndExpression(profiles);
    }

    @Test
    void ofAndExpressionWithoutSpaces() {
        Profiles profiles = Profiles.of("(aspectran,framework)");
        assertAndExpression(profiles);
    }

    private void assertAndExpression(Profiles profiles) {
        assertFalse(profiles.matches(activeProfiles("aspectran")));
        assertFalse(profiles.matches(activeProfiles("framework")));
        assertTrue(profiles.matches(activeProfiles("aspectran", "framework")));
        assertFalse(profiles.matches(activeProfiles("java")));
    }

    @Test
    void ofNotAndExpression() {
        Profiles profiles = Profiles.of("!(aspectran, framework)");
        assertOfNotAndExpression(profiles);
    }

    @Test
    void ofNotAndExpressionWithoutSpaces() {
        Profiles profiles = Profiles.of("!(aspectran,framework)");
        assertOfNotAndExpression(profiles);
    }

    private void assertOfNotAndExpression(Profiles profiles) {
        assertTrue(profiles.matches(activeProfiles("aspectran")));
        assertTrue(profiles.matches(activeProfiles("framework")));
        assertFalse(profiles.matches(activeProfiles("aspectran", "framework")));
        assertTrue(profiles.matches(activeProfiles("java")));
    }

    @Test
    void ofAndExpressionWithInvertedSingleElement() {
        Profiles profiles = Profiles.of("(!aspectran, framework)");
        assertOfAndExpressionWithInvertedSingleElement(profiles);
    }

    @Test
    void ofAndExpressionWithInBracketsInvertedSingleElement() {
        Profiles profiles = Profiles.of("((!aspectran), framework)");
        assertOfAndExpressionWithInvertedSingleElement(profiles);
    }

    @Test
    void ofAndExpressionWithInvertedSingleElementInBrackets() {
        Profiles profiles = Profiles.of("(!(aspectran), framework)");
        assertOfAndExpressionWithInvertedSingleElement(profiles);
    }

    @Test
    void ofAndExpressionWithInvertedSingleElementInBracketsWithoutSpaces() {
        Profiles profiles = Profiles.of("(!(aspectran),framework)");
        assertOfAndExpressionWithInvertedSingleElement(profiles);
    }

    @Test
    void ofAndExpressionWithInvertedSingleElementWithoutSpaces() {
        Profiles profiles = Profiles.of("(!aspectran,framework)");
        assertOfAndExpressionWithInvertedSingleElement(profiles);
    }

    private void assertOfAndExpressionWithInvertedSingleElement(Profiles profiles) {
        assertTrue(profiles.matches(activeProfiles("framework")));
        assertFalse(profiles.matches(activeProfiles("java")));
        assertFalse(profiles.matches(activeProfiles("aspectran", "framework")));
        assertFalse(profiles.matches(activeProfiles("aspectran")));
    }

    @Test
    void ofOrExpressionWithInvertedSingleElementWithoutSpaces() {
        Profiles profiles = Profiles.of("[!aspectran,framework]");
        assertOfOrExpressionWithInvertedSingleElement(profiles);
    }

    private void assertOfOrExpressionWithInvertedSingleElement(Profiles profiles) {
        assertTrue(profiles.matches(activeProfiles("framework")));
        assertTrue(profiles.matches(activeProfiles("java")));
        assertTrue(profiles.matches(activeProfiles("aspectran", "framework")));
        assertFalse(profiles.matches(activeProfiles("aspectran")));
    }

    @Test
    void ofNotOrExpression() {
        Profiles profiles = Profiles.of("![aspectran, framework]");
        assertOfNotOrExpression(profiles);
    }

    @Test
    void ofNotOrExpressionWithoutSpaces() {
        Profiles profiles = Profiles.of("![aspectran,framework]");
        assertOfNotOrExpression(profiles);
    }

    private void assertOfNotOrExpression(Profiles profiles) {
        assertFalse(profiles.matches(activeProfiles("aspectran")));
        assertFalse(profiles.matches(activeProfiles("framework")));
        assertFalse(profiles.matches(activeProfiles("aspectran", "framework")));
        assertTrue(profiles.matches(activeProfiles("java")));
    }

    @Test
    void ofComplexExpression() {
        Profiles profiles = Profiles.of("[(aspectran, framework), (aspectran, java)]");
        assertComplexExpression(profiles);
    }

    @Test
    void ofComplexExpressionWithoutSpaces() {
        Profiles profiles = Profiles.of("[(aspectran,framework),(aspectran,java)]");
        assertComplexExpression(profiles);
    }

    @Test
    void ofComplexExpressionEnclosedInParentheses() {
        Profiles profiles = Profiles.of("([(aspectran,framework),(aspectran,java)])");
        assertComplexExpression(profiles);
    }

    private void assertComplexExpression(Profiles profiles) {
        assertFalse(profiles.matches(activeProfiles("aspectran")));
        assertTrue(profiles.matches(activeProfiles("aspectran", "framework")));
        assertTrue(profiles.matches(activeProfiles("aspectran", "java")));
        assertFalse(profiles.matches(activeProfiles("java", "framework")));
    }

    @Test
    void sensibleToString() {
        assertEquals(Profiles.of("aspectran").toString(), "aspectran");
        assertEquals(Profiles.of("[(aspectran, framework), (aspectran, java)]").toString(),
            "[(aspectran, framework), (aspectran, java)]");
        assertEquals(Profiles.of("[(aspectran,framework),(aspectran,java)]").toString(),
            "[(aspectran,framework),(aspectran,java)]");
    }

    @Test
    void toStringGeneratesValidCompositeProfileExpression() {
        assertThatToStringGeneratesValidCompositeProfileExpression("aspectran");
        assertThatToStringGeneratesValidCompositeProfileExpression("[(aspectran, kotlin), (aspectran, java)]");
    }

    private static void assertThatToStringGeneratesValidCompositeProfileExpression(String profileExpression) {
        Profiles profiles = Profiles.of(profileExpression);
        assertTrue(profiles.matches(activeProfiles("aspectran", "java")));
        assertFalse(profiles.matches(activeProfiles("kotlin")));

        Profiles compositeProfiles = Profiles.of(profiles.toString());
        assertTrue(compositeProfiles.matches(activeProfiles("aspectran", "java")));
        assertFalse(compositeProfiles.matches(activeProfiles("kotlin")));
    }

    @Test
    void equalsAndHashCodeAreNotBasedOnLogicalStructureOfNodesWithinExpressionTree() {
        Profiles profiles1 = Profiles.of("[A, B]");
        Profiles profiles2 = Profiles.of("[B, A]");

        assertTrue(profiles1.matches(activeProfiles("A")));
        assertTrue(profiles1.matches(activeProfiles("B")));
        assertTrue(profiles2.matches(activeProfiles("A")));
        assertTrue(profiles2.matches(activeProfiles("B")));

        assertNotEquals(profiles1, profiles2);
        assertNotEquals(profiles2, profiles1);
        assertNotEquals(profiles1.hashCode(), profiles2.hashCode());
    }

    private static Predicate<String> activeProfiles(String... profiles) {
        return new MockActiveProfiles(profiles);
    }

    private static class MockActiveProfiles implements Predicate<String> {

        private final List<String> activeProfiles;

        MockActiveProfiles(String[] activeProfiles) {
            this.activeProfiles = Arrays.asList(activeProfiles);
        }

        @Override
        public boolean test(String profile) {
            // The following if-condition (which basically mimics
            // AbstractEnvironment#validateProfile(String)) is necessary in order
            // to ensure that the Profiles implementation returned by Profiles.of()
            // never passes an invalid (parsed) profile name to the active profiles
            // predicate supplied to Profiles#matches(Predicate<String>).
            if (!StringUtils.hasText(profile) || profile.charAt(0) == '!') {
                throw new IllegalArgumentException("Invalid profile [" + profile + "]");
            }
            return this.activeProfiles.contains(profile);
        }

    }

}
