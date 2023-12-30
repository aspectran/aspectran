package com.aspectran.core.context.env;

import com.aspectran.utils.Assert;
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

    private enum Operator { AND, OR }

    private enum Context { NONE, NEGATE, PARENTHESIS }

    private ProfilesParser() {
    }

    static Profiles parse(String expression) {
        Profiles parsed = parseExpression(expression);
        return new ParsedProfiles(expression, parsed);
    }

    private static Profiles parseExpression(String expression) {
        Assert.hasText(expression, () -> "Invalid profile expression [" + expression + "]: must contain text");
        StringTokenizer tokens = new StringTokenizer(expression, "()&|!", true);
        return parseTokens(expression, tokens);
    }

    private static Profiles parseTokens(String expression, StringTokenizer tokens) {
        return parseTokens(expression, tokens, Context.NONE);
    }

    private static Profiles parseTokens(String expression, StringTokenizer tokens, Context context) {
        List<Profiles> elements = new ArrayList<>();
        Operator operator = null;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();
            if (token.isEmpty()) {
                continue;
            }
            switch (token) {
                case "(":
                    Profiles contents = parseTokens(expression, tokens, Context.PARENTHESIS);
                    if (context == Context.NEGATE) {
                        return contents;
                    }
                    elements.add(contents);
                    break;
                case "&":
                    assertWellFormed(expression, operator == null || operator == Operator.AND);
                    operator = Operator.AND;
                    break;
                case "|":
                    assertWellFormed(expression, operator == null || operator == Operator.OR);
                    operator = Operator.OR;
                    break;
                case "!":
                    elements.add(not(parseTokens(expression, tokens, Context.NEGATE)));
                    break;
                case ")":
                    Profiles merged = merge(expression, elements, operator);
                    if (context == Context.PARENTHESIS) {
                        return merged;
                    }
                    elements.clear();
                    elements.add(merged);
                    operator = null;
                    break;
                default:
                    Profiles value = equals(token);
                    if (context == Context.NEGATE) {
                        return value;
                    }
                    elements.add(value);
            }
        }
        return merge(expression, elements, operator);
    }

    private static Profiles merge(String expression, List<Profiles> elements, @Nullable Operator operator) {
        assertWellFormed(expression, !elements.isEmpty());
        if (elements.size() == 1) {
            return elements.get(0);
        }
        Profiles[] profiles = elements.toArray(new Profiles[0]);
        return (operator == Operator.AND ? and(profiles) : or(profiles));
    }

    private static void assertWellFormed(String expression, boolean wellFormed) {
        Assert.isTrue(wellFormed, () -> "Malformed profile expression [" + expression + "]");
    }

    private static Profiles or(Profiles... profiles) {
        return activeProfile -> Arrays.stream(profiles).anyMatch(isMatch(activeProfile));
    }

    private static Profiles and(Profiles... profiles) {
        return activeProfile -> Arrays.stream(profiles).allMatch(isMatch(activeProfile));
    }

    private static Profiles not(Profiles profiles) {
        return activeProfile -> !profiles.matches(activeProfile);
    }

    private static Profiles equals(String profile) {
        return activeProfile -> activeProfile.test(profile);
    }

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
            if (other instanceof ParsedProfiles) {
                ParsedProfiles that = (ParsedProfiles)other;
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
