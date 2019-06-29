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
import com.aspectran.core.util.apon.ValueType;

public class ContextConfig extends AbstractParameters {

    private static final ParameterDefinition base;
    private static final ParameterDefinition root;
    private static final ParameterDefinition encoding;
    private static final ParameterDefinition resources;
    private static final ParameterDefinition scan;
    private static final ParameterDefinition profiles;
    private static final ParameterDefinition hybridLoad;
    private static final ParameterDefinition autoReload;
    private static final ParameterDefinition singleton;
    private static final ParameterDefinition parameters;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        base = new ParameterDefinition("base", ValueType.STRING);
        root = new ParameterDefinition("root", ValueType.STRING);
        encoding = new ParameterDefinition("encoding", ValueType.STRING);
        resources = new ParameterDefinition("resources", ValueType.STRING, true);
        scan = new ParameterDefinition("scan", ValueType.STRING, true);
        profiles = new ParameterDefinition("profiles", ContextProfilesConfig.class);
        hybridLoad = new ParameterDefinition("hybridLoad", ValueType.BOOLEAN);
        autoReload = new ParameterDefinition("autoReload", ContextAutoReloadConfig.class);
        singleton = new ParameterDefinition("singleton", ValueType.BOOLEAN);
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

    public ContextConfig setBasePath(String basePath) {
        putValue(base, basePath);
        return this;
    }

    public String getRootFile() {
        return getString(root);
    }

    public ContextConfig setRootFile(String rootFile) {
        putValue(root, rootFile);
        return this;
    }

    public String getEncoding() {
        return getString(encoding);
    }

    public ContextConfig setEncoding(String encoding) {
        putValue(ContextConfig.encoding, encoding);
        return this;
    }

    public String[] getResourceLocations() {
        return getStringArray(resources);
    }

    public ContextConfig addResourceLocation(String resourceLocation) {
        putValue(resources, resourceLocation);
        return this;
    }

    public String[] getBasePackages() {
        return getStringArray(scan);
    }

    public ContextConfig addBasePackage(String basePackage) {
        putValue(scan, basePackage);
        return this;
    }

    public ContextProfilesConfig getProfilesConfig() {
        return getParameters(profiles);
    }

    public ContextProfilesConfig newProfilesConfig() {
        return newParameters(profiles);
    }

    public ContextProfilesConfig touchProfilesConfig() {
        return touchParameters(profiles);
    }

    public boolean hasProfilesConfig() {
        return hasValue(profiles);
    }

    public boolean isHybridLoad() {
        return getBoolean(hybridLoad, false);
    }

    public ContextConfig setHybridLoad(boolean hybridLoad) {
        putValue(ContextConfig.hybridLoad, hybridLoad);
        return this;
    }

    public ContextAutoReloadConfig getAutoReloadConfig() {
        return getParameters(autoReload);
    }

    public ContextAutoReloadConfig newAutoReloadConfig() {
        return newParameters(autoReload);
    }

    public ContextAutoReloadConfig touchAutoReloadConfig() {
        return touchParameters(autoReload);
    }

    public boolean isSingleton() {
        return getBoolean(singleton, false);
    }

    public ContextConfig setSingleton(boolean singleton) {
        putValue(ContextConfig.singleton, singleton);
        return this;
    }

    public AspectranParameters getAspectranParameters() {
        return getParameters(parameters);
    }

    public AspectranParameters newAspectranParameters() {
        return newParameters(parameters);
    }

    public AspectranParameters touchAspectranParameters() {
        return touchParameters(parameters);
    }

    public boolean hasAspectranParameters() {
        return hasValue(parameters);
    }

}
