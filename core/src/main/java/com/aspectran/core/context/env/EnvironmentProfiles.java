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
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.SystemUtils;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Environment profiles provide baseline information for applications to recognize
 * different execution environments. e.g. development, test, production etc.
 */
public class EnvironmentProfiles {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentProfiles.class);

    /**
     * Name of property to set to specify active profiles: {@value}. Value may be comma-delimited.
     */
    public static final String ACTIVE_PROFILES_PROPERTY_NAME = "aspectran.profiles.active";

    /**
     * Name of property to set to specify profiles active by default: {@value}. Value may
     * be comma-delimited.
     */
    public static final String DEFAULT_PROFILES_PROPERTY_NAME = "aspectran.profiles.default";

    private final Set<String> activeProfiles = new LinkedHashSet<>();

    private final Set<String> defaultProfiles = new LinkedHashSet<>();

    public String[] getActiveProfiles() {
        return StringUtils.toStringArray(doGetActiveProfiles());
    }

    /**
     * Returns the set of active profiles as explicitly set through
     * {@link #setActiveProfiles} or if the current set of active profiles
     * is empty, check for the presence of the {@value #ACTIVE_PROFILES_PROPERTY_NAME}
     * property and assign its value to the set of active profiles.
     * @see #getActiveProfiles()
     * @see #ACTIVE_PROFILES_PROPERTY_NAME
     */
    private Set<String> doGetActiveProfiles() {
        synchronized (activeProfiles) {
            if (activeProfiles.isEmpty()) {
                String[] profiles = doGetActiveProfilesProperty();
                if (profiles != null) {
                    setActiveProfiles(profiles);
                }
            }
            return activeProfiles;
        }
    }

    private String[] doGetActiveProfilesProperty() {
        return getProfilesFromSystemProperty(ACTIVE_PROFILES_PROPERTY_NAME);
    }

    /**
     * Specify the set of profiles active for this {@code Environment}.
     * Profiles are evaluated during the ActivityContext configuration to determine
     * whether configuration settings or rules should be registered.
     * <p>Any existing active profiles will be replaced with the given arguments; call
     * with zero arguments to clear the current set of active profiles.</p>
     * @param profiles the set of profiles active
     * @see #setDefaultProfiles
     * @throws IllegalArgumentException if any profile is null, empty or whitespace-only
     */
    public void setActiveProfiles(String... profiles) {
        Assert.notNull(profiles, "profiles must not be null");
        logger.info("Activating profiles [" + StringUtils.joinCommaDelimitedList(profiles) + "]");
        synchronized (activeProfiles) {
            activeProfiles.clear();
            for (String profile : profiles) {
                validateProfile(profile);
                activeProfiles.add(profile);
            }
        }
    }

    public void addActiveProfile(String profile) {
        logger.info("Activating profile '" + profile + "'");
        validateProfile(profile);
        doGetActiveProfiles();
        synchronized (this.activeProfiles) {
            this.activeProfiles.add(profile);
        }
    }

    public String[] getDefaultProfiles() {
        return StringUtils.toStringArray(doGetDefaultProfiles());
    }

    /**
     * Returns the set of default profiles explicitly set via
     * {@link #setDefaultProfiles(String...)}, then check for the presence of the
     * {@value #DEFAULT_PROFILES_PROPERTY_NAME} property and assign its value (if any)
     * to the set of default profiles.
     */
    private Set<String> doGetDefaultProfiles() {
        synchronized (defaultProfiles) {
            if (defaultProfiles.isEmpty()) {
                String[] profiles = doGetDefaultProfilesProperty();
                if (profiles != null) {
                    setDefaultProfiles(profiles);
                }
            }
            return defaultProfiles;
        }
    }

    private String[] doGetDefaultProfilesProperty() {
        return getProfilesFromSystemProperty(DEFAULT_PROFILES_PROPERTY_NAME);
    }

    /**
     * Specify the set of profiles to be made active by default if no other profiles
     * are explicitly made active through {@link #setActiveProfiles}.
     * <p>Calling this method removes overrides any reserved default profiles
     * that may have been added during construction of the environment.</p>
     * @param profiles the set of profiles to be made active by default
     */
    public void setDefaultProfiles(String... profiles) {
        Assert.notNull(profiles, "profiles must not be null");
        logger.info("Default profiles [" + StringUtils.joinCommaDelimitedList(profiles) + "]");
        synchronized (defaultProfiles) {
            defaultProfiles.clear();
            for (String profile : profiles) {
                validateProfile(profile);
                defaultProfiles.add(profile);
            }
        }
    }

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
     * {@code env.acceptsProfiles(Profiles.of(profileExpressions))}.
     * @since 7.5.0
     * @see Profiles#of(String)
     * @see #acceptsProfiles(Profiles)
     */
    public boolean matchesProfiles(String profileExpression) {
        return acceptsProfiles(Profiles.of(profileExpression));
    }

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
    public boolean acceptsProfiles(Profiles profiles) {
        return (profiles == null || profiles.matches(this::isProfileActive));
    }

    /**
     * Determine whether one or more of the given profiles is active &mdash; or
     * in the case of no explicit {@linkplain #getActiveProfiles() active profiles},
     * whether one or more of the given profiles is included in the set of
     * {@linkplain #getDefaultProfiles() default profiles}.
     * <p>If a profile begins with '!' the logic is inverted, meaning this method
     * will return {@code true} if the given profile is <em>not</em> active. For
     * example, {@code env.acceptsProfiles("p1", "!p2")} will return {@code true}
     * if profile 'p1' is active or 'p2' is not active.
     * @throws IllegalArgumentException if called with a {@code null} array, an
     * empty array, zero arguments or if any profile is {@code null}, empty, or
     * whitespace only
     * @see #getActiveProfiles
     * @see #getDefaultProfiles
     * @see #matchesProfiles(String)
     * @see #acceptsProfiles(Profiles)
     */
    public boolean acceptsProfiles(String... profiles) {
        if (profiles == null || profiles.length == 0) {
            return true;
        }
        for (String profile : profiles) {
            if (StringUtils.hasLength(profile) && profile.charAt(0) == '!') {
                if (!isProfileActive(profile.substring(1))) {
                    return true;
                }
            } else {
                if (isProfileActive(profile)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the given profile is active, or if active profiles are empty
     * whether the profile should be active by default.
     * @throws IllegalArgumentException per {@link #validateProfile(String)}
     */
    private boolean isProfileActive(String profile) {
        validateProfile(profile);
        Set<String> currentActiveProfiles = doGetActiveProfiles();
        return (currentActiveProfiles.contains(profile) ||
                (currentActiveProfiles.isEmpty() && doGetDefaultProfiles().contains(profile)));
    }

    /**
     * Validate the given profile, called internally prior to adding to the set of
     * active or default profiles.
     * <p>Subclasses may override to impose further restrictions on profile syntax.</p>
     * @param profile the given profile
     * @throws IllegalArgumentException if the profile is null, empty, whitespace-only or
     *      begins with the profile NOT operator (!)
     * @see #acceptsProfiles
     * @see #setDefaultProfiles
     */
    private void validateProfile(String profile) {
        if (!StringUtils.hasText(profile)) {
            throw new IllegalArgumentException("Invalid profile [" + profile + "]: must contain text");
        }
        if (profile.charAt(0) == '!') {
            throw new IllegalArgumentException("Invalid profile [" + profile + "]: must not begin with ! operator");
        }
    }

    @Nullable
    private String[] getProfilesFromSystemProperty(String propName) {
        String profilesProp = SystemUtils.getProperty(propName);
        if (profilesProp != null) {
            String[] profiles = StringUtils.splitCommaDelimitedString(profilesProp);
            if (profiles.length > 0) {
                return profiles;
            }
        }
        return null;
    }

}
