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
package com.aspectran.core.context.env;

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.function.Predicate;

/**
 * A predicate for checking whether a given profile or profile expression is active.
 *
 * <p>This is a {@link FunctionalInterface} that can be implemented directly or,
 * more commonly, created using the {@link #of(String)} factory method.
 * It allows for sophisticated profile matching logic using a flexible
 * expression language.
 *
 * <p>Profile expressions support the following operators:
 * <ul>
 *     <li>{@code !}: Logical NOT</li>
 *     <li>{@code ,}: Delimiter for separating profiles</li>
 *     <li>{@code ()}: Logical AND for grouping profiles</li>
 *     <li>{@code []}: Logical OR for grouping profiles (can be omitted for a single OR set)</li>
 * </ul>
 *
 * <p>Examples:
 * <ul>
 *     <li>{@code "production"}: Matches if the "production" profile is active.</li>
 *     <li>{@code "!production"}: Matches if the "production" profile is not active.</li>
 *     <li>{@code "(p1, p2)"}: Matches if both "p1" and "p2" profiles are active.</li>
 *     <li>{@code "[p1, p2]"}: Matches if either "p1" or "p2" profile is active.</li>
 *     <li>{@code "(!p1, p2)"}: Matches if "p1" is not active and "p2" is active.</li>
 * </ul>
 *
 * @since 7.5.0
 * @see Environment#acceptsProfiles(Profiles)
 * @see Environment#matchesProfiles(String)
 * @see ProfilesParser
 */
@FunctionalInterface
public interface Profiles {

    /**
     * Tests if this {@code Profiles} instance matches against the given predicate.
     * @param isProfileActive a predicate that tests whether a given profile is
     *      currently active
     * @return {@code true} if the profile is active, {@code false} otherwise
     */
    boolean matches(Predicate<String> isProfileActive);

    /**
     * Creates a new {@link Profiles} instance that checks for matches against
     * the given profile expression.
     * <p>The returned instance will {@linkplain Profiles#matches(Predicate) match}
     * if the given profile expression is satisfied.
     * <p>A profile expression may contain a simple profile name (e.g., {@code "production"})
     * or a compound expression with logical operators (e.g., {@code "(production, cloud)"}).
     * @param profileExpression the expression for profiles to include or exclude
     * @return a new {@link Profiles} instance for the given expression
     * @see ProfilesParser#parse(String)
     */
    @NonNull
    static Profiles of(String profileExpression) {
        return ProfilesParser.parse(profileExpression);
    }

}
