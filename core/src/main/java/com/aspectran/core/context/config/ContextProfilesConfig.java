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
package com.aspectran.core.context.config;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class ContextProfilesConfig extends AbstractParameters {

    private static final ParameterKey activeProfiles;
    private static final ParameterKey defaultProfiles;

    private static final ParameterKey[] parameterKeys;

    static {
        activeProfiles = new ParameterKey("active", ValueType.STRING, true);
        defaultProfiles = new ParameterKey("default", ValueType.STRING, true);

        parameterKeys = new ParameterKey[] {
                activeProfiles,
                defaultProfiles
        };
    }

    public ContextProfilesConfig() {
        super(parameterKeys);
    }

    public String[] getActiveProfiles() {
        return getStringArray(activeProfiles);
    }

    public ContextProfilesConfig setActiveProfiles(String[] activeProfiles) {
        removeValue(ContextProfilesConfig.activeProfiles);
        putValue(ContextProfilesConfig.activeProfiles, activeProfiles);
        return this;
    }

    public ContextProfilesConfig addActiveProfile(String activeProfile) {
        putValue(ContextProfilesConfig.activeProfiles, activeProfile);
        return this;
    }

    public String[] getDefaultProfiles() {
        return getStringArray(defaultProfiles);
    }

    public ContextProfilesConfig setDefaultProfiles(String[] defaultProfiles) {
        removeValue(ContextProfilesConfig.defaultProfiles);
        putValue(ContextProfilesConfig.defaultProfiles, defaultProfiles);
        return this;
    }

    public ContextProfilesConfig addDefaultProfile(String defaultProfile) {
        putValue(ContextProfilesConfig.defaultProfiles, defaultProfile);
        return this;
    }

}
