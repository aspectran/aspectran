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
package com.aspectran.core.context.builder;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.resource.InvalidResourceException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.service.CoreService;

import java.io.IOException;

/**
 * Strategy interface for building ActivityContext.
 */
public interface ActivityContextBuilder {

    String DEBUG_MODE_PROPERTY_NAME = "com.aspectran.core.context.builder.debugMode";

    String USE_APON_TO_LOAD_XML_PROPERTY_NAME = "com.aspectran.core.context.builder.useAponToLoadXml";

    String getBasePath();

    void setBasePath(String basePath);

    boolean hasOwnBasePath();

    CoreService getMasterService();

    ContextConfig getContextConfig();

    AspectranParameters getAspectranParameters();

    void setAspectranParameters(AspectranParameters aspectranParameters);

    String[] getContextRules();

    void setContextRules(String[] contextRules);

    String getEncoding();

    void setEncoding(String encoding);

    String[] getResourceLocations();

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

    String[] getActiveProfiles();

    void setActiveProfiles(String... activeProfiles);

    String[] getDefaultProfiles();

    void setDefaultProfiles(String... defaultProfiles);

    void putPropertyItemRule(ItemRule propertyItemRule);

    /**
     * Returns whether to reload all Java classes, resources,
     * and activity context configurations.
     * @return false if only the activity context configuration
     *      is reloaded; true if all are reloaded
     */
    boolean isHardReload();

    void setHardReload(boolean hardReload);

    ClassLoader getClassLoader();

    void configure(ContextConfig contextConfig) throws InvalidResourceException, IOException;

    ActivityContext build(AspectranParameters aspectranParameters) throws ActivityContextBuilderException;

    ActivityContext build(String... contextRules) throws ActivityContextBuilderException;


    ActivityContext build() throws ActivityContextBuilderException;

    void destroy();

    void clear();

    boolean isActive();

    void setUseAponToLoadXml(boolean useAponToLoadXml);

    void setDebugMode(boolean debugMode);

}
