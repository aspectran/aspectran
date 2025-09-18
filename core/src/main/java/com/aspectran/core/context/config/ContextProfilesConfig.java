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
package com.aspectran.core.context.config;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Configuration for environment profiles.
 * <p>Allows for the registration of beans and properties to be conditional
 * based on the active, default, or base profiles.</p>
 */
public class ContextProfilesConfig extends AbstractParameters {

    /** The base profiles that are always active. */
    private static final ParameterKey baseProfiles;

    /** The default profiles to use when no active profiles are specified. */
    private static final ParameterKey defaultProfiles;

    /** The currently active profiles. */
    private static final ParameterKey activeProfiles;

    private static final ParameterKey[] parameterKeys;

    static {
        baseProfiles = new ParameterKey("base", ValueType.STRING, true);
        defaultProfiles = new ParameterKey("default", ValueType.STRING, true);
        activeProfiles = new ParameterKey("active", ValueType.STRING, true);

        parameterKeys = new ParameterKey[] {
                baseProfiles,
                defaultProfiles,
                activeProfiles
        };
    }

    /**
     * Instantiates a new ContextProfilesConfig.
     */
    public ContextProfilesConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the base profiles.
     * @return the base profiles
     */
    public String[] getBaseProfiles() {
        return getStringArray(baseProfiles);
    }

    /**
     * Sets the base profiles.
     * @param baseProfiles the base profiles
     * @return this {@code ContextProfilesConfig} instance
     */
    public ContextProfilesConfig setBaseProfiles(String[] baseProfiles) {
        removeValue(ContextProfilesConfig.baseProfiles);
        putValue(ContextProfilesConfig.baseProfiles, baseProfiles);
        return this;
    }

    /**
     * Adds a base profile.
     * @param baseProfile the base profile to add
     * @return this {@code ContextProfilesConfig} instance
     */
    public ContextProfilesConfig addBaseProfile(String baseProfile) {
        putValue(ContextProfilesConfig.baseProfiles, baseProfile);
        return this;
    }

    /**
     * Returns the default profiles.
     * @return the default profiles
     */
    public String[] getDefaultProfiles() {
        return getStringArray(defaultProfiles);
    }

    /**
     * Sets the default profiles.
     * @param defaultProfiles the default profiles
     * @return this {@code ContextProfilesConfig} instance
     */
    public ContextProfilesConfig setDefaultProfiles(String[] defaultProfiles) {
        removeValue(ContextProfilesConfig.defaultProfiles);
        putValue(ContextProfilesConfig.defaultProfiles, defaultProfiles);
        return this;
    }

    /**
     * Adds a default profile.
     * @param defaultProfile the default profile to add
     * @return this {@code ContextProfilesConfig} instance
     */
    public ContextProfilesConfig addDefaultProfile(String defaultProfile) {
        putValue(ContextProfilesConfig.defaultProfiles, defaultProfile);
        return this;
    }

    /**
     * Returns the active profiles.
     * @return the active profiles
     */
    public String[] getActiveProfiles() {
        return getStringArray(activeProfiles);
    }

    /**
     * Sets the active profiles.
     * @param activeProfiles the active profiles
     * @return this {@code ContextProfilesConfig} instance
     */
    public ContextProfilesConfig setActiveProfiles(String[] activeProfiles) {
        removeValue(ContextProfilesConfig.activeProfiles);
        putValue(ContextProfilesConfig.activeProfiles, activeProfiles);
        return this;
    }

    /**
     * Adds an active profile.
     * @param activeProfile the active profile to add
     * @return this {@code ContextProfilesConfig} instance
     */
    public ContextProfilesConfig addActiveProfile(String activeProfile) {
        putValue(ContextProfilesConfig.activeProfiles, activeProfile);
        return this;
    }

}
