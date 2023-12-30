package com.aspectran.core.context.env;

import java.util.function.Predicate;

/**
 * Profile predicate that may be {@linkplain Environment#acceptsProfiles(Profiles)
 * accepted} by an {@link Environment}.
 *
 * <p>May be implemented directly or, more usually, created using the
 * {@link #of(String) of()} factory method.
 *
 * @since 7.5.0
 * @see Environment#acceptsProfiles(Profiles)
 * @see Environment#matchesProfiles(String)
 */
@FunctionalInterface
public interface Profiles {

    /**
     * Test if this {@code Profiles} instance <em>matches</em> against the given
     * predicate.
     * @param isProfileActive a predicate that tests whether a given profile is
     * currently active
     */
    boolean matches(Predicate<String> isProfileActive);

    /**
     * Create a new {@link Profiles} instance that checks for matches against
     * the given <em>profile expressions</em>.
     * <p>The returned instance will {@linkplain Profiles#matches(Predicate) match}
     * if any one of the given profile expressions matches.
     * <p>A profile expression may contain a simple profile name (for example
     * {@code "production"}) or a compound expression. A compound expression allows
     * for more complicated profile logic to be expressed, for example
     * {@code "production & cloud"}.
     * <p>The following operators are supported in profile expressions.
     * <ul>
     * <li>{@code !} - A logical <em>NOT</em> of the profile name or compound expression</li>
     * <li>{@code &} - A logical <em>AND</em> of the profile names or compound expressions</li>
     * <li>{@code |} - A logical <em>OR</em> of the profile names or compound expressions</li>
     * </ul>
     * <p>Please note that the {@code &} and {@code |} operators may not be mixed
     * without using parentheses. For example, {@code "a & b | c"} is not a valid
     * expression: it must be expressed as {@code "(a & b) | c"} or
     * {@code "a & (b | c)"}.
     * <p>As of Spring Framework 5.1.17, two {@code Profiles} instances returned
     * by this method are considered equivalent to each other (in terms of
     * {@code equals()} and {@code hashCode()} semantics) if they are created
     * with identical <em>profile expressions</em>.
     * @param profileExpression the <em>profile expression</em> to include
     * @return a new {@link Profiles} instance
     */
    static Profiles of(String profileExpression) {
        return ProfilesParser.parse(profileExpression);
    }

}
