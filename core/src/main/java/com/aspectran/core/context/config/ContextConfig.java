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

import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Contains the core settings for the ActivityContext.
 * <p>This class holds essential information for building the context,
 * such as the application's base path, configuration rule files,
 * component scan packages, profiles, and auto-reloading settings.</p>
 */
public class ContextConfig extends AbstractParameters {

    /** The name of the context. */
    private static final ParameterKey name;

    /** The base path of the application. */
    private static final ParameterKey base;

    /** The list of context rule files. */
    private static final ParameterKey rules;

    /** The default character encoding. */
    private static final ParameterKey encoding;

    /** The locations of resource files. */
    private static final ParameterKey resources;

    /** The base packages to scan for components. */
    private static final ParameterKey scan;

    /** The configuration for active profiles. */
    private static final ParameterKey profiles;

    /** The configuration for asynchronous processing. */
    private static final ParameterKey async;

    /** The configuration for automatic context reloading. */
    private static final ParameterKey autoReload;

    /** Whether the context is a singleton. */
    private static final ParameterKey singleton;

    /** The parameters for the Aspectran context. */
    private static final ParameterKey parameters;

    private static final ParameterKey[] parameterKeys;

    static {
        name = new ParameterKey("name", ValueType.STRING);
        base = new ParameterKey("base", ValueType.STRING);
        rules = new ParameterKey("rules", ValueType.STRING, true);
        encoding = new ParameterKey("encoding", ValueType.STRING);
        resources = new ParameterKey("resources", ValueType.STRING, true);
        scan = new ParameterKey("scan", ValueType.STRING, true);
        profiles = new ParameterKey("profiles", ContextProfilesConfig.class);
        async = new ParameterKey("async", AsyncConfig.class);
        autoReload = new ParameterKey("autoReload", ContextAutoReloadConfig.class);
        singleton = new ParameterKey("singleton", ValueType.BOOLEAN);
        parameters = new ParameterKey("parameters", AspectranParameters.class);

        parameterKeys = new ParameterKey[] {
                name,
                base,
                rules,
                encoding,
                resources,
                scan,
                profiles,
                async,
                autoReload,
                singleton,
                parameters
        };
    }

    /**
     * Instantiates a new ContextConfig.
     */
    public ContextConfig() {
        super(parameterKeys);
    }

    /**
     * Returns whether the context name is set.
     * @return true if the context name is set, false otherwise
     */
    public boolean hasName() {
        return hasValue(name);
    }

    /**
     * Returns the name of the context.
     * @return the context name
     */
    public String getName() {
        return getString(name);
    }

    /**
     * Sets the name of the context.
     * @param name the context name
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig setName(String name) {
        if (StringUtils.hasText(name)) {
            putValue(ContextConfig.name, name);
        } else if (hasValue(name)) {
            removeValue(ContextConfig.name);
        }
        return this;
    }

    /**
     * Returns whether the base path is set.
     * @return true if the base path is set, false otherwise
     */
    public boolean hasBasePath() {
        return hasValue(base);
    }

    /**
     * Returns the base path of the application.
     * @return the base path
     */
    public String getBasePath() {
        return getString(base);
    }

    /**
     * Sets the base path of the application.
     * @param basePath the base path
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig setBasePath(String basePath) {
        if (StringUtils.hasText(basePath)) {
            putValue(base, basePath);
        } else if (hasValue(base)) {
            removeValue(base);
        }
        return this;
    }

    /**
     * Returns the list of context rule files.
     * @return the context rule files
     */
    public String[] getContextRules() {
        if (isAssigned(rules)) {
            return getStringArray(rules);
        } else {
            return null;
        }
    }

    /**
     * Sets the list of context rule files.
     * @param contextRules the context rule files
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig setContextRules(String[] contextRules) {
        removeValue(rules);
        putValue(rules, contextRules);
        return this;
    }

    /**
     * Adds a context rule file to the list.
     * @param contextRule the context rule file
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig addContextRule(String contextRule) {
        putValue(rules, contextRule);
        return this;
    }

    /**
     * Returns the default character encoding.
     * @return the character encoding
     */
    public String getEncoding() {
        return getString(encoding);
    }

    /**
     * Sets the default character encoding.
     * @param encoding the character encoding
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig setEncoding(String encoding) {
        putValue(ContextConfig.encoding, encoding);
        return this;
    }

    /**
     * Returns the locations of resource files.
     * @return the resource locations
     */
    public String[] getResourceLocations() {
        if (isAssigned(resources)) {
            return getStringArray(resources);
        } else {
            return null;
        }
    }

    /**
     * Sets the locations of resource files.
     * @param resourceLocations the resource locations
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig setResourceLocations(String[] resourceLocations) {
        removeValue(resources);
        putValue(resources, resourceLocations);
        return this;
    }

    /**
     * Adds a resource location to the list.
     * @param resourceLocation the resource location
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig addResourceLocation(String resourceLocation) {
        putValue(resources, resourceLocation);
        return this;
    }

    /**
     * Returns the base packages to scan for components.
     * @return the base packages
     */
    public String[] getBasePackages() {
        if (isAssigned(scan)) {
            return getStringArray(scan);
        } else {
            return null;
        }
    }

    /**
     * Sets the base packages to scan for components.
     * @param basePackages the base packages
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig setBasePackage(String[] basePackages) {
        removeValue(scan);
        putValue(scan, basePackages);
        return this;
    }

    /**
     * Adds a base package to scan for components.
     * @param basePackage the base package
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig addBasePackage(String basePackage) {
        putValue(scan, basePackage);
        return this;
    }

    /**
     * Returns the configuration for active profiles.
     * @return the {@code ContextProfilesConfig} instance
     */
    public ContextProfilesConfig getProfilesConfig() {
        return getParameters(profiles);
    }

    /**
     * Creates a new configuration for active profiles.
     * @return the new {@code ContextProfilesConfig} instance
     */
    public ContextProfilesConfig newProfilesConfig() {
        return newParameters(profiles);
    }

    /**
     * Returns the existing or a new configuration for active profiles.
     * @return a non-null {@code ContextProfilesConfig} instance
     */
    public ContextProfilesConfig touchProfilesConfig() {
        return touchParameters(profiles);
    }

    /**
     * Returns whether the profiles configuration section exists.
     * @return true if the profiles configuration exists, false otherwise
     */
    public boolean hasProfilesConfig() {
        return hasValue(profiles);
    }

    /**
     * Returns the configuration for asynchronous processing.
     * @return the {@code AsyncConfig} instance
     */
    public AsyncConfig getAsyncConfig() {
        return getParameters(async);
    }

    /**
     * Creates a new configuration for asynchronous processing.
     * @return the new {@code AsyncConfig} instance
     */
    public AsyncConfig newAsyncConfig() {
        return newParameters(async);
    }

    /**
     * Returns the existing or a new configuration for asynchronous processing.
     * @return a non-null {@code AsyncConfig} instance
     */
    public AsyncConfig touchAsyncConfig() {
        return touchParameters(async);
    }

    /**
     * Returns the configuration for automatic context reloading.
     * @return the {@code ContextAutoReloadConfig} instance
     */
    public ContextAutoReloadConfig getAutoReloadConfig() {
        return getParameters(autoReload);
    }

    /**
     * Sets the configuration for automatic context reloading.
     * @param autoReloadConfig the {@code ContextAutoReloadConfig} instance
     */
    public void setAutoReloadConfig(ContextAutoReloadConfig autoReloadConfig) {
        putValue(autoReload, autoReloadConfig);
    }

    /**
     * Creates a new configuration for automatic context reloading.
     * @return the new {@code ContextAutoReloadConfig} instance
     */
    public ContextAutoReloadConfig newAutoReloadConfig() {
        return newParameters(autoReload);
    }

    /**
     * Returns the existing or a new configuration for automatic context reloading.
     * @return a non-null {@code ContextAutoReloadConfig} instance
     */
    public ContextAutoReloadConfig touchAutoReloadConfig() {
        return touchParameters(autoReload);
    }

    /**
     * Returns whether the context is a singleton.
     * @return true if the context is a singleton, false otherwise
     */
    public boolean isSingleton() {
        return getBoolean(singleton, false);
    }

    /**
     * Sets whether the context is a singleton.
     * @param singleton true for a singleton context, false otherwise
     * @return this {@code ContextConfig} instance
     */
    public ContextConfig setSingleton(boolean singleton) {
        putValue(ContextConfig.singleton, singleton);
        return this;
    }

    /**
     * Returns whether the Aspectran parameters section exists.
     * @return true if the Aspectran parameters section exists, otherwise false
     */
    public boolean hasAspectranParameters() {
        return hasValue(parameters);
    }

    /**
     * Returns the Aspectran parameters containing the context rules.
     * @return the {@code AspectranParameters} instance
     */
    public AspectranParameters getAspectranParameters() {
        return getParameters(parameters);
    }

    /**
     * Creates a new Aspectran parameters section for context rules.
     * @return the new {@code AspectranParameters} instance
     */
    public AspectranParameters newAspectranParameters() {
        return newParameters(parameters);
    }

    /**
     * Returns the existing Aspectran parameters section or creates a new one if it does not exist.
     * @return a non-null {@code AspectranParameters} instance
     */
    public AspectranParameters touchAspectranParameters() {
        return touchParameters(parameters);
    }

}
