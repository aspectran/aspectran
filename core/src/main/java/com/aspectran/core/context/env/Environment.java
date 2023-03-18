/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

/**
 * Interface representing the environment in which the current application is running.
 * Models two key aspects of the application environment: <em>profiles</em> and <em>properties</em>.
 */
public interface Environment {

    /**
     * Returns the set of profiles explicitly made active for this environment.
     * @return the set of profiles explicitly made active
     */
    String[] getActiveProfiles();

    /**
     * Returns the set of profiles to be active by default when no active profiles have
     * been set explicitly.
     * @return the set of profiles to be active by default
     */
    String[] getDefaultProfiles();

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

}
