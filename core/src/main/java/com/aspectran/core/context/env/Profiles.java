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
     * {@code "(production, cloud)"}.
     * <p>The following operators are supported in profile expressions.
     * <ul>
     * <li>{@code !} - A logical <em>NOT</em> of the profile name or set of them</li>
     * <li>{@code ,} - A delimiter to separate profile names or sets of them</li>
     * </ul>
     * <p>The following parentheses are used for compound expressions:
     * <ul>
     * <li>{@code ()} - A logical AND operation is applied to all profile names
     *                  enclosed by this; aka AND set</li>
     * <li>{@code []} - A logical OR operation is applied to all profile names
     *                  enclosed by this; aka OR set, it can be omitted in compound
     *                  expressions that have only one OR set.</li>
     * </ul>
     * @param profileExpression the expression for profiles to include or exclude
     * @return a new {@link Profiles} instance
     */
    static Profiles of(String profileExpression) {
        return ProfilesParser.parse(profileExpression);
    }

}
