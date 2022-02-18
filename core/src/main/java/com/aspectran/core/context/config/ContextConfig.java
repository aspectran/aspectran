/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

public class ContextConfig extends AbstractParameters {

    private static final ParameterKey base;
    private static final ParameterKey root;
    private static final ParameterKey encoding;
    private static final ParameterKey resources;
    private static final ParameterKey scan;
    private static final ParameterKey profiles;
    private static final ParameterKey autoReload;
    private static final ParameterKey singleton;
    private static final ParameterKey parameters;

    private static final ParameterKey[] parameterKeys;

    static {
        base = new ParameterKey("base", ValueType.STRING);
        root = new ParameterKey("root", ValueType.STRING);
        encoding = new ParameterKey("encoding", ValueType.STRING);
        resources = new ParameterKey("resources", ValueType.STRING, true);
        scan = new ParameterKey("scan", ValueType.STRING, true);
        profiles = new ParameterKey("profiles", ContextProfilesConfig.class);
        autoReload = new ParameterKey("autoReload", ContextAutoReloadConfig.class);
        singleton = new ParameterKey("singleton", ValueType.BOOLEAN);
        parameters = new ParameterKey("parameters", AspectranParameters.class);

        parameterKeys = new ParameterKey[] {
                base,
                root,
                encoding,
                resources,
                scan,
                profiles,
                autoReload,
                singleton,
                parameters
        };
    }

    public ContextConfig() {
        super(parameterKeys);
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
