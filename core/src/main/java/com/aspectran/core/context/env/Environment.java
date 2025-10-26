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

import com.aspectran.core.activity.Activity;

import java.util.Iterator;

/**
 * Interface representing the environment in which the application is running.
 * It models two key aspects: <em>profiles</em> and <em>properties</em>.
 *
 * <p>A <b>profile</b> is a named, logical group of bean definitions to be
 * registered with the container only if the profile is active. The role of the
 * {@code Environment} is to determine which profiles are currently
 * {@linkplain #getActiveProfiles() active}, and which should be active by
 * {@linkplain #getDefaultProfiles() default}.
 *
 * <p><b>Properties</b> play an important role in almost all applications. The role of
 * the environment is to provide a convenient interface for resolving property values
 * from various sources.
 */
public interface Environment {

    /**
     * Returns the set of profiles that are essential for this environment.
     * These are always active internally but are not exposed as active profiles.
     * @return the set of essential profiles for this environment
     * @see #getActiveProfiles
     * @see EnvironmentProfiles#setBaseProfiles
     * @see EnvironmentProfiles#BASE_PROFILES_PROPERTY_NAME
     */
    String[] getBaseProfiles();

    /**
     * Returns the set of profiles to be active by default when no active profiles have
     * been set explicitly.
     * @return the set of default profiles
     * @see #getActiveProfiles
     * @see EnvironmentProfiles#setDefaultProfiles
     * @see EnvironmentProfiles#DEFAULT_PROFILES_PROPERTY_NAME
     */
    String[] getDefaultProfiles();

    /**
     * Returns the set of profiles explicitly made active for this environment.
     * <p>Profiles can be activated by setting the
     * {@link EnvironmentProfiles#ACTIVE_PROFILES_PROPERTY_NAME "aspectran.profiles.active"}
     * system property or by calling {@link #addActiveProfile(String)}.
     * <p>If no profiles are explicitly active, the {@linkplain #getDefaultProfiles() default
     * profiles} will be used.
     * @return the set of active profiles
     * @see #getDefaultProfiles
     * @see #addActiveProfile(String)
     * @see EnvironmentProfiles#ACTIVE_PROFILES_PROPERTY_NAME
     */
    String[] getActiveProfiles();

    /**
     * Returns the set of profiles to be used for the current environment.
     * If any profiles are active, they will be returned. Otherwise, the default
     * profiles will be returned.
     * @return the set of current profiles
     */
    String[] getCurrentProfiles();

    /**
     * Determines whether one of the given profile expressions matches the
     * {@linkplain #getActiveProfiles() active profiles} or, in the case of no
     * explicit active profiles, whether one of the given profile expressions
     * matches the {@linkplain #getDefaultProfiles() default profiles}.
     * <p>Profile expressions allow for complex, boolean profile logic to be
     * expressed &mdash; for example {@code "p1 & p2"}, {@code "(p1 & p2) | p3"},
     * etc. See {@link Profiles#of(String)} for details on the supported
     * expression syntax.
     * <p>This method is a convenient shortcut for
     * {@code env.acceptsProfiles(Profiles.of(profileExpression))}.
     * @param profileExpression the profile expression to match
     * @return {@code true} if the profile expression matches, {@code false} otherwise
     * @since 7.5.0
     * @see Profiles#of(String)
     * @see #acceptsProfiles(Profiles)
     */
    boolean matchesProfiles(String profileExpression);

    /**
     * Determines whether the given {@link Profiles} predicate matches the
     * {@linkplain #getActiveProfiles() active profiles} or, in the case of no
     * explicit active profiles, whether the given {@code Profiles} predicate
     * matches the {@linkplain #getDefaultProfiles() default profiles}.
     * <p>If you wish provide profile expressions directly as strings, use
     * {@link #matchesProfiles(String)} instead.
     * @param profiles the {@link Profiles} predicate to match
     * @return {@code true} if the predicate matches, {@code false} otherwise
     * @since 7.5.0
     * @see #matchesProfiles(String)
     * @see Profiles#of(String)
     */
    boolean acceptsProfiles(Profiles profiles);

    /**
     * Checks if any of the given profile conditions are met against the active
     * or default profiles.
     * <p>If a profile name is prefixed with '!', the logic is inverted. For example,
     * {@code env.acceptsProfiles("p1", "!p2")} will return {@code true} if profile
     * 'p1' is active OR if profile 'p2' is NOT active.
     * @param profiles the profiles to check
     * @return {@code true} if any of the profile conditions are met, {@code false} otherwise
     * @throws IllegalArgumentException if called with zero arguments or if any
     *      profile is {@code null}, empty, or whitespace-only
     * @see #getActiveProfiles
     * @see #getDefaultProfiles
     * @see #matchesProfiles(String)
     */
    boolean acceptsProfiles(String... profiles);

    /**
     * Adds a profile to the current set of active profiles.
     * @param profile the profile to add
     * @throws IllegalArgumentException if the profile is null, empty, or whitespace-only
     */
    void addActiveProfile(String profile);

    /**
     * Returns the value of the property with the given name.
     * <p>This method will return {@code null} if the property is not found.
     * @param <T> the expected type of the property value
     * @param name the name of the property to retrieve
     * @return the value of the property, or {@code null} if not found
     */
    <T> T getProperty(String name);

    /**
     * Returns the value of the property with the given name, evaluated within the
     * context of the specified activity.
     * <p>This method will return {@code null} if the property is not found.
     * @param <T> the expected type of the property value
     * @param name the name of the property to retrieve
     * @param activity the activity context for property evaluation
     * @return the value of the property, or {@code null} if not found
     */
    <T> T getProperty(String name, Activity activity);

    /**
     * Returns an iterator over the names of all properties available in this environment.
     * @return an iterator over the property names
     */
    Iterator<String> getPropertyNames();

}
