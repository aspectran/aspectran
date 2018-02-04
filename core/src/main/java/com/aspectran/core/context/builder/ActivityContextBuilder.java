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
package com.aspectran.core.context.builder;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.context.resource.InvalidResourceException;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.service.ServiceController;

/**
 * Strategy interface for building ActivityContext.
 */
public interface ActivityContextBuilder {

    ApplicationAdapter getApplicationAdapter();

    ContextConfig getContextConfig();

    String getBasePath();

    void setBasePath(String basePath);

    AspectranParameters getAspectranParameters();

    void setAspectranParameters(AspectranParameters aspectranParameters);

    String getRootConfigLocation();

    void setRootConfigLocation(String rootConfigLocation);

    String getEncoding();

    void setEncoding(String encoding);

    String[] getResourceLocations();

    void setResourceLocations(String... resourceLocations);

    String[] getActiveProfiles();

    void setActiveProfiles(String... activeProfiles);

    String[] getDefaultProfiles();

    void setDefaultProfiles(String... defaultProfiles);

    ItemRuleMap getPropertyItemRuleMap();

    void setPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap);

    void addPropertyItemRuleMap(ItemRuleMap propertyItemRuleMap);

    boolean isHybridLoad();

    void setHybridLoad(boolean hybridLoad);

    boolean isHardReload();

    void setHardReload(boolean hardReload);

    ServiceController getServiceController();

    void setServiceController(ServiceController serviceController);

    AspectranClassLoader getAspectranClassLoader();

    void setContextConfig(ContextConfig contextConfig) throws InvalidResourceException;

    ActivityContext build(AspectranParameters aspectranParameters) throws ActivityContextBuilderException;

    ActivityContext build(String rootConfigLocation) throws ActivityContextBuilderException;

    ActivityContext build() throws ActivityContextBuilderException;

    void destroy();

    boolean isActive();

}
