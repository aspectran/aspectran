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
package com.aspectran.core.context.builder;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.resource.InvalidResourceException;
import com.aspectran.core.context.resource.SiblingClassLoader;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.service.CoreService;

import java.io.IOException;

/**
 * Defines the strategy for building and managing the lifecycle of an {@link com.aspectran.core.context.ActivityContext}.
 *
 * <p>This interface provides a blueprint for implementations that can parse configuration,
 * construct an {@code ActivityContext}, and handle its destruction. It supports various
 * configuration sources, including explicit rule files and {@link AspectranParameters}.
 *
 * @since 2.0.0
 */
public interface ActivityContextBuilder {

    /** The system property name for debug mode */
    String DEBUG_MODE_PROPERTY_NAME = "com.aspectran.core.context.builder.debugMode";

    /** The system property name for using APON to load XML configuration */
    String USE_APON_TO_LOAD_XML_PROPERTY_NAME = "com.aspectran.core.context.builder.useAponToLoadXml";

    /**
     * Returns the base path for the application.
     * @return the base path
     */
    String getBasePath();

    /**
     * Sets the base path for the application.
     * @param basePath the base path
     */
    void setBasePath(String basePath);

    /**
     * Returns whether the base path is owned by this context.
     * @return true if the base path is owned by this context, otherwise false
     */
    boolean hasOwnBasePath();

    /**
     * Returns the master service that this builder belongs to.
     * @return the master service
     */
    CoreService getMasterService();

    /**
     * Returns the context configuration.
     * @return the context configuration
     */
    ContextConfig getContextConfig();

    /**
     * Returns the Aspectran parameters used for configuration.
     * @return the Aspectran parameters
     */
    AspectranParameters getAspectranParameters();

    /**
     * Sets the Aspectran parameters to be used for configuration.
     * @param aspectranParameters the Aspectran parameters
     */
    void setAspectranParameters(AspectranParameters aspectranParameters);

    /**
     * Returns the paths to the context rule files.
     * @return the context rule file paths
     */
    String[] getContextRules();

    /**
     * Sets the paths to the context rule files.
     * @param contextRules the context rule file paths
     */
    void setContextRules(String[] contextRules);

    /**
     * Returns the character encoding for reading configuration files.
     * @return the character encoding
     */
    String getEncoding();

    /**
     * Sets the character encoding for reading configuration files.
     * @param encoding the character encoding
     */
    void setEncoding(String encoding);

    /**
     * Returns the locations of resources to be added to the classpath.
     * @return the resource locations
     */
    String[] getResourceLocations();

    /**
     * Sets the locations of resources to be added to the classpath.
     * @param resourceLocations the resource locations
     */
    void setResourceLocations(String... resourceLocations);

    /**
     * Returns the base packages to scan for annotated components.
     * @return the base packages
     */
    String[] getBasePackages();

    /**
     * Sets the base packages to scan for annotated components.
     * @param basePackages the base packages to scan
     */
    void setBasePackages(String... basePackages);

    /**
     * Returns the active profiles for the context.
     * @return the active profiles
     */
    String[] getActiveProfiles();

    /**
     * Sets the active profiles for the context.
     * @param activeProfiles the active profiles
     */
    void setActiveProfiles(String... activeProfiles);

    /**
     * Returns the default profiles for the context.
     * @return the default profiles
     */
    String[] getDefaultProfiles();

    /**
     * Sets the default profiles for the context.
     * @param defaultProfiles the default profiles
     */
    void setDefaultProfiles(String... defaultProfiles);

    /**
     * Adds a property item rule.
     * @param propertyItemRule the property item rule to add
     */
    void putPropertyItemRule(ItemRule propertyItemRule);

    /**
     * Returns the sibling class loader used by the context.
     * @return the sibling class loader
     */
    SiblingClassLoader getSiblingClassLoader();

    /**
     * Configures the builder using the specified context configuration object.
     * @param contextConfig the context configuration
     * @throws InvalidResourceException if a resource is invalid
     * @throws IOException if an I/O error occurs
     */
    void configure(ContextConfig contextConfig) throws InvalidResourceException, IOException;

    /**
     * Builds an {@link ActivityContext} using the provided {@link AspectranParameters}.
     * @param aspectranParameters the parameters defining the context configuration
     * @return a fully initialized {@link ActivityContext}
     * @throws ActivityContextBuilderException if the building process fails
     */
    ActivityContext build(AspectranParameters aspectranParameters) throws ActivityContextBuilderException;

    /**
     * Builds an {@link ActivityContext} from the specified context rule files.
     * @param contextRules an array of paths to the context rule files
     * @return a fully initialized {@link ActivityContext}
     * @throws ActivityContextBuilderException if the building process fails
     */
    ActivityContext build(String... contextRules) throws ActivityContextBuilderException;

    /**
     * Builds an {@link ActivityContext} using the configuration previously set on this builder.
     * @return a fully initialized {@link ActivityContext}
     * @throws ActivityContextBuilderException if the building process fails
     */
    ActivityContext build() throws ActivityContextBuilderException;

    /**
     * Destroys the {@link ActivityContext} that was built by this builder.
     */
    void destroy();

    /**
     * Returns whether to perform a hard reload.
     * A hard reload involves reloading all Java classes, resources, and the entire context configuration.
     * @return true to perform a hard reload, false for a soft reload (context configuration only)
     */
    boolean isHardReload();

    /**
     * Sets whether to perform a hard reload.
     * @param hardReload true to perform a hard reload, false for a soft reload
     */
    void setHardReload(boolean hardReload);

    /**
     * Checks if the {@link ActivityContext} is currently active.
     * @return true if the context is active, otherwise false
     */
    boolean isActive();

    /**
     * Sets whether to use APON for parsing XML configuration files.
     * @param useAponToLoadXml true to use APON, false otherwise
     */
    void setUseAponToLoadXml(boolean useAponToLoadXml);

    /**
     * Sets whether to run in debug mode.
     * @param debugMode true for debug mode, false otherwise
     */
    void setDebugMode(boolean debugMode);

    /**
     * Clears any system properties that were set by this builder.
     */
    void clear();

}
