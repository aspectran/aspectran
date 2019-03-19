/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class ContextConfig extends AbstractParameters {

    public static final ParameterDefinition base;
    public static final ParameterDefinition root;
    public static final ParameterDefinition encoding;
    public static final ParameterDefinition resources;
    public static final ParameterDefinition scan;
    public static final ParameterDefinition profiles;
    public static final ParameterDefinition hybridLoad;
    public static final ParameterDefinition autoReload;
    public static final ParameterDefinition singleton;
    public static final ParameterDefinition parameters;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        base = new ParameterDefinition("base", ParameterValueType.STRING);
        root = new ParameterDefinition("root", ParameterValueType.STRING);
        encoding = new ParameterDefinition("encoding", ParameterValueType.STRING);
        resources = new ParameterDefinition("resources", ParameterValueType.STRING, true);
        scan = new ParameterDefinition("scan", ParameterValueType.STRING, true);
        profiles = new ParameterDefinition("profiles", ContextProfilesConfig.class);
        hybridLoad = new ParameterDefinition("hybridLoad", ParameterValueType.BOOLEAN);
        autoReload = new ParameterDefinition("autoReload", ContextAutoReloadConfig.class);
        singleton = new ParameterDefinition("singleton", ParameterValueType.BOOLEAN);
        parameters = new ParameterDefinition("parameters", AspectranParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                base,
                root,
                encoding,
                resources,
                scan,
                profiles,
                hybridLoad,
                autoReload,
                singleton,
                parameters
        };
    }

    public ContextConfig() {
        super(parameterDefinitions);
    }

    public String getBasePath() {
        return getString(base);
    }

    public void setBasePath(String basePath) {
        putValue(base, basePath);
    }

    public String getRootFile() {
        return getString(root);
    }

    public void setRootFile(String rootFile) {
        putValue(root, rootFile);
    }

    public String getEncoding() {
        return getString(encoding);
    }

    public void setEncoding(String encoding) {
        putValue(ContextConfig.encoding, encoding);
    }

    public String[] getResourceLocations() {
        return getStringArray(resources);
    }

    public void addResourceLocation(String resourceLocation) {
        putValue(resources, resourceLocation);
    }

    public String[] getBasePackages() {
        return getStringArray(scan);
    }

    public void addBasePackage(String basePackage) {
        putValue(scan, basePackage);
    }

    public String[] getActiveProfiles() {
        ContextProfilesConfig contextProfilesConfig = getParameters(profiles);
        if (contextProfilesConfig != null) {
            return contextProfilesConfig.getActiveProfiles();
        } else {
            return null;
        }
    }

    public void addActivieProfile(String activeProfile) {
        ContextProfilesConfig contextProfilesConfig = touchParameters(profiles);
        contextProfilesConfig.addActiveProfile(activeProfile);
    }

    public String[] getDefaultProfiles() {
        ContextProfilesConfig contextProfilesConfig = getParameters(profiles);
        if (contextProfilesConfig != null) {
            return contextProfilesConfig.getDefaultProfiles();
        } else {
            return null;
        }
    }

    public void addDefaultProfile(String defaultProfile) {
        ContextProfilesConfig contextProfilesConfig = touchParameters(profiles);
        contextProfilesConfig.addDefaultProfile(defaultProfile);
    }

    public boolean hasProfiles() {
        return hasParameter(profiles);
    }

    public boolean isHybridLoad() {
        return getBoolean(hybridLoad, false);
    }

    public void setHybridLoad(boolean hybridLoad) {
        putValue(ContextConfig.hybridLoad, hybridLoad);
    }

    public ContextAutoReloadConfig getContextAutoReloadConfig() {
        return getParameters(autoReload);
    }

    public boolean isSingleton() {
        return getBoolean(singleton, false);
    }

    public void setSingleton(boolean singleton) {
        putValue(ContextConfig.singleton, singleton);
    }

    public AspectranParameters getAspectranParameters() {
        return getParameters(parameters);
    }

    public boolean hasAspectranParameters() {
        return hasParameter(parameters);
    }

    public AspectranParameters newAspectranParameters() {
        return newParameters(parameters);
    }

}
