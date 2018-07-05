/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.SystemUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Abstract base class for {@link Environment} implementations.
 */
public abstract class AbstractEnvironment implements Environment {

    /**
     * Name of property to set to specify active profiles: {@value}. Value may be comma
     * delimited.
     */
    private static final String ACTIVE_PROFILES_PROPERTY_NAME = "aspectran.profiles.active";

    /**
     * Name of property to set to specify profiles active by default: {@value}. Value may
     * be comma delimited.
     */
    private static final String DEFAULT_PROFILES_PROPERTY_NAME = "aspectran.profiles.default";

    private final Set<String> activeProfiles = new LinkedHashSet<>();

    private final Set<String> defaultProfiles = new LinkedHashSet<>();

    private ItemRuleMap propertyItemRuleMap;

    @Override
    public String[] getActiveProfiles() {
        return activeProfiles.toArray(new String[0]);
    }

    /**
     * Returns the set of active profiles as explicitly set through
     * {@link #setActiveProfiles} or if the current set of active profiles
     * is empty, check for the presence of the {@value #ACTIVE_PROFILES_PROPERTY_NAME}
     * property and assign its value to the set of active profiles.
     *
     * @see #getActiveProfiles()
     * @see #ACTIVE_PROFILES_PROPERTY_NAME
     */
    private Set<String> doGetActiveProfiles() {
        synchronized (activeProfiles) {
            if (activeProfiles.isEmpty()) {
                String[] profiles = getProfilesFromSystemProperty(ACTIVE_PROFILES_PROPERTY_NAME);
                if (profiles != null) {
                    setActiveProfiles(profiles);
                }
            }
            return activeProfiles;
        }
    }

    /**
     * Specify the set of profiles active for this {@code Environment}.
     * Profiles are evaluated during the ActivityContext configuration to determine
     * whether configuration settings or rules should be registered.
     * <p>Any existing active profiles will be replaced with the given arguments; call
     * with zero arguments to clear the current set of active profiles.</p>
     *
     * @param profiles the set of profiles active
     * @see #setDefaultProfiles
     * @throws IllegalArgumentException if any profile is null, empty or whitespace-only
     */
    public void setActiveProfiles(String... profiles) {
        if (profiles == null) {
            throw new IllegalArgumentException("Argument 'profiles' must not be null");
        }
        synchronized (activeProfiles) {
            activeProfiles.clear();
            for (String profile : profiles) {
                validateProfile(profile);
                activeProfiles.add(profile);
            }
        }
    }

    @Override
    public String[] getDefaultProfiles() {
        return defaultProfiles.toArray(new String[0]);
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
                String[] profiles = getProfilesFromSystemProperty(DEFAULT_PROFILES_PROPERTY_NAME);
                if (profiles != null) {
                    setDefaultProfiles(profiles);
                }
            }
            return defaultProfiles;
        }
    }

    /**
     * Specify the set of profiles to be made active by default if no other profiles
     * are explicitly made active through {@link #setActiveProfiles}.
     * <p>Calling this method removes overrides any reserved default profiles
     * that may have been added during construction of the environment.</p>
     *
     * @param profiles the set of profiles to be made active by default
     */
    public void setDefaultProfiles(String... profiles) {
        if (profiles == null) {
            throw new IllegalArgumentException("Argument 'profiles' must not be null");
        }
        synchronized (defaultProfiles) {
            defaultProfiles.clear();
            for (String profile : profiles) {
                validateProfile(profile);
                defaultProfiles.add(profile);
            }
        }
    }

    @Override
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
     *
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
     *
     * @param profile the given profile
     * @throws IllegalArgumentException if the profile is null, empty, whitespace-only or
     *      begins with the profile NOT operator (!)
     * @see #acceptsProfiles
     * @see #setDefaultProfiles
     */
    protected void validateProfile(String profile) {
        if (!StringUtils.hasText(profile)) {
            throw new IllegalArgumentException("Invalid profile [" + profile + "]: must contain text");
        }
        if (profile.charAt(0) == '!') {
            throw new IllegalArgumentException("Invalid profile [" + profile + "]: must not begin with ! operator");
        }
    }

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

    public ItemRuleMap getPropertyItemRuleMap() {
        return propertyItemRuleMap;
    }

    public void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        this.propertyItemRuleMap = propertyItemRuleMap;
    }

    public void addPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap) {
        if (this.propertyItemRuleMap == null) {
            this.propertyItemRuleMap = new ItemRuleMap(propertyItemRuleMap);
        } else {
            this.propertyItemRuleMap.putAll(propertyItemRuleMap);
        }
    }

    @Override
    public <T> T getProperty(String name, Activity activity) {
        if (propertyItemRuleMap == null) {
            return null;
        }

        ItemRule itemRule = propertyItemRuleMap.get(name);
        if (itemRule == null) {
            return null;
        }

        ItemEvaluator evaluator = new ItemExpressionParser(activity);
        return evaluator.evaluate(itemRule);
    }

}
