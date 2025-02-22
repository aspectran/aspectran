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

import com.aspectran.core.activity.Activity;

import java.util.Iterator;

/**
 * Interface representing the environment in which the current application is running.
 * Models two key aspects of the application environment: <em>profiles</em> and <em>properties</em>.
 */
public interface Environment {

    EnvironmentProfiles getEnvironmentProfiles();

    /**
     * Return the set of profiles explicitly made active for this environment. Profiles
     * are used for creating logical groupings of bean definitions to be registered
     * conditionally, for example based on deployment environment. Profiles can be
     * activated by setting {@linkplain EnvironmentProfiles#ACTIVE_PROFILES_PROPERTY_NAME
     * "aspectran.profiles.active"} as a system property or by calling
     * {@link EnvironmentProfiles#setActiveProfiles(String...)}.
     * <p>If no profiles have explicitly been specified as active, then any
     * {@linkplain #getDefaultProfiles() default profiles} will automatically be activated.
     * @see #getDefaultProfiles
     * @see EnvironmentProfiles#setActiveProfiles
     * @see EnvironmentProfiles#ACTIVE_PROFILES_PROPERTY_NAME
     */
    String[] getActiveProfiles();

    /**
     * Return the set of profiles to be active by default when no active profiles have
     * been set explicitly.
     * @see #getActiveProfiles
     * @see EnvironmentProfiles#setDefaultProfiles
     * @see EnvironmentProfiles#DEFAULT_PROFILES_PROPERTY_NAME
     */
    String[] getDefaultProfiles();


    /**
     * Return the set of profiles explicitly made active for this environment.
     * If no active profile is explicitly set, returns the set of profiles to be
     * active by default.
     * @see #getActiveProfiles
     * @see EnvironmentProfiles#getActiveProfiles
     * @see #getDefaultProfiles
     * @see EnvironmentProfiles#getDefaultProfiles
     */
    String[] getCurrentProfiles();

    /**
     * Determine whether one of the given profile expressions matches the
     * {@linkplain #getActiveProfiles() active profiles} &mdash; or in the case
     * of no explicit active profiles, whether one of the given profile expressions
     * matches the {@linkplain #getDefaultProfiles() default profiles}.
     * <p>Profile expressions allow for complex, boolean profile logic to be
     * expressed &mdash; for example {@code "p1 & p2"}, {@code "(p1 & p2) | p3"},
     * etc. See {@link Profiles#of(String)} for details on the supported
     * expression syntax.
     * <p>This method is a convenient shortcut for
     * {@code env.acceptsProfiles(Profiles.of(profileExpression))}.
     * @since 7.5.0
     * @see Profiles#of(String)
     * @see #acceptsProfiles(Profiles)
     */
    boolean matchesProfiles(String profileExpression);

    /**
     * Determine whether the given {@link Profiles} predicate matches the
     * {@linkplain #getActiveProfiles() active profiles} &mdash; or in the case
     * of no explicit active profiles, whether the given {@code Profiles} predicate
     * matches the {@linkplain #getDefaultProfiles() default profiles}.
     * <p>If you wish provide profile expressions directly as strings, use
     * {@link #matchesProfiles(String)} instead.
     * @since 7.5.0
     * @see #matchesProfiles(String)
     * @see Profiles#of(String)
     */
    boolean acceptsProfiles(Profiles profiles);

    /**
     * Return whether one or more of the given profiles is active or, in the case of no
     * explicit active profiles, whether one or more of the given profiles is included in
     * the set of default profiles. If a profile begins with '!' the logic is inverted,
     * i.e. the method will return true if the given profile is <em>not</em> active.
     * For example, <pre class="code">env.acceptsProfiles("p1", "!p2")</pre> will
     * return {@code true} if profile 'p1' is active or 'p2' is not active.
     * @param profiles the given profiles
     * @return true if the given profile is active; false otherwise
     * @throws IllegalArgumentException if called with zero arguments
     * or if any profile is {@code null}, empty or whitespace-only
     * @see #getActiveProfiles
     * @see #getDefaultProfiles
     */
    boolean acceptsProfiles(String... profiles);

    /**
     * Add a profile to the current set of active profiles.
     * @throws IllegalArgumentException if the profile is null, empty or whitespace-only
     */
    void addActiveProfile(String profile);

    /**
     * Returns the value of the property on environment via the currently available activity.
     * @param <T> the type of the value
     * @param name the given property name
     * @return the value of the property on environment
     */
    <T> T getProperty(String name);

    /**
     * Returns the value of the property on environment via the specified activity.
     * @param <T> the type of the value
     * @param name the given property name
     * @param activity the activity
     * @return the value of the property on environment
     */
    <T> T getProperty(String name, Activity activity);

    Iterator<String> getPropertyNames();

}
