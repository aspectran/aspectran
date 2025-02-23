/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Predicate;

/**
 * Internal parser used by {@link Profiles#of}.
 *
 * @since 7.5.0
 */
final class ProfilesParser {

    private enum Operator { NONE, AND, OR, NEGATE }

    private ProfilesParser() {
    }

    @NonNull
    static Profiles parse(String expression) {
        Profiles parsed = parseExpression(expression);
        return new ParsedProfiles(expression, parsed);
    }

    private static Profiles parseExpression(String expression) {
        Assert.hasText(expression, () -> "Invalid profile expression \"" + expression + "\": must contain text");
        StringTokenizer tokens = new StringTokenizer(expression, "()[]!, ", true);
        return parseTokens(expression, tokens);
    }

    private static Profiles parseTokens(String expression, StringTokenizer tokens) {
        return parseTokens(expression, tokens, Operator.NONE);
    }

    private static Profiles parseTokens(String expression, @NonNull StringTokenizer tokens, Operator operator) {
        List<Profiles> elements = new ArrayList<>();
        String token = null;
        int count = 0;
        while (tokens.hasMoreTokens()) {
            token = tokens.nextToken().trim();
            if (token.isEmpty()) {
                continue;
            }
            switch (token) {
                case "(":
                case "[":
                    Operator nested = "(".equals(token) ? Operator.AND : Operator.OR;
                    Profiles contents = parseTokens(expression, tokens, nested);
                    if (operator == Operator.NEGATE) {
                        return contents;
                    }
                    assertWellFormed(expression, elements, count);
                    elements.add(contents);
                    break;
                case ")":
                    assertWellFormed(expression, operator, Operator.AND, token);
                    return merge(expression, elements, operator);
                case "]":
                    assertWellFormed(expression, operator, Operator.OR, token);
                    return merge(expression, elements, operator);
                case "!":
                    elements.add(not(parseTokens(expression, tokens, Operator.NEGATE)));
                    break;
                case ",":
                    count++;
                    break;
                default:
                    Profiles value = equals(token);
                    if (operator == Operator.NEGATE) {
                        return value;
                    }
                    assertWellFormed(expression, elements, count);
                    elements.add(value);
            }
        }
        assertWellFormed(expression, Operator.NONE, operator, token);
        return merge(expression, elements, Operator.OR);
    }

    private static Profiles merge(String expression, @NonNull List<Profiles> elements, Operator operator) {
        assertWellFormed(expression, !elements.isEmpty(), "");
        if (elements.size() == 1) {
            return elements.get(0);
        } else {
            Profiles[] profiles = elements.toArray(new Profiles[0]);
            return (operator == Operator.AND ? and(profiles) : or(profiles));
        }
    }

    private static void assertWellFormed(String expression, Operator expected, Operator actual, String token) {
        Assert.isTrue(expected == actual, () -> {
            String message = null;
            if (actual == Operator.NEGATE) {
                message = "inappropriate use of negation operator; ‘!’ cannot be used alone";
            } else if (actual == Operator.AND && expected == Operator.OR) {
                message = "missing closing parenthesis of " + expected + " set; must be closed with ']', but ')'";
            } else if (actual == Operator.OR && expected == Operator.AND) {
                message = "missing closing parenthesis of " + expected + " set; must be closed with ')', but ']'";
            } else if (token != null) {
                message = "unnecessary operator '" + token + "'";
            }
            return "Malformed profile expression \"" + expression + "\"" + (message != null ? ": " + message : "");
        });
    }

    private static void assertWellFormed(String expression, @NonNull List<Profiles> elements, int size) {
        assertWellFormed(expression, elements.size() == size,
                "each profile or set of them must be separated by commas (',')");
    }

    private static void assertWellFormed(String expression, boolean wellFormed, String message) {
        Assert.isTrue(wellFormed, () -> "Malformed profile expression \"" + expression + "\": " + message);
    }

    @NonNull
    private static Profiles or(Profiles... profiles) {
        return activeProfile -> Arrays.stream(profiles).anyMatch(isMatch(activeProfile));
    }

    @NonNull
    private static Profiles and(Profiles... profiles) {
        return activeProfile -> Arrays.stream(profiles).allMatch(isMatch(activeProfile));
    }

    @NonNull
    private static Profiles not(Profiles profiles) {
        return activeProfile -> !profiles.matches(activeProfile);
    }

    @NonNull
    private static Profiles equals(String profile) {
        return activeProfile -> activeProfile.test(profile);
    }

    @NonNull
    private static Predicate<Profiles> isMatch(Predicate<String> activeProfiles) {
        return profiles -> profiles.matches(activeProfiles);
    }

    private static class ParsedProfiles implements Profiles {

        private final String expression;

        private final Profiles parsed;

        ParsedProfiles(String expression, Profiles parsed) {
            this.expression = expression;
            this.parsed = parsed;
        }

        @Override
        public boolean matches(Predicate<String> activeProfiles) {
            return parsed.matches(activeProfiles);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof ParsedProfiles that) {
                return expression.equals(that.expression);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return expression.hashCode();
        }

        @Override
        public String toString() {
            return expression;
        }

    }

}
