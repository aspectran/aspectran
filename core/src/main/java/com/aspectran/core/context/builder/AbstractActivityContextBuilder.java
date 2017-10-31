/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
import com.aspectran.core.adapter.BasicApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.context.builder.config.ContextAutoReloadConfig;
import com.aspectran.core.context.builder.config.ContextConfig;
import com.aspectran.core.context.builder.config.ContextProfilesConfig;
import com.aspectran.core.context.builder.reload.ActivityContextReloadingTimer;
import com.aspectran.core.context.builder.resource.AspectranClassLoader;
import com.aspectran.core.context.builder.resource.InvalidResourceException;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.service.ServiceController;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public abstract class AbstractActivityContextBuilder implements ActivityContextBuilder {

    protected final Log log = LogFactory.getLog(getClass());

    private final ApplicationAdapter applicationAdapter;

    private ContextConfig contextConfig;

    private AspectranParameters aspectranParameters;

    private String basePath;

    private String rootConfigLocation;

    private String encoding;

    private String[] resourceLocations;

    private String[] activeProfiles;

    private String[] defaultProfiles;

    private boolean hybridLoad;

    private boolean hardReload;

    private boolean autoReloadStartup;

    private int scanIntervalSeconds;

    private ActivityContextReloadingTimer reloadingTimer;

    private ServiceController serviceController;

    private AspectranClassLoader aspectranClassLoader;

    public AbstractActivityContextBuilder(ApplicationAdapter applicationAdapter) {
        if (applicationAdapter == null) {
            throw new IllegalArgumentException("Argument 'applicationAdapter' must not be null");
        }
        this.applicationAdapter = applicationAdapter;
        this.basePath = applicationAdapter.getBasePath();
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    @Override
    public ContextConfig getContextConfig() {
        return contextConfig;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public void setBasePath(String basePath) {
        if (applicationAdapter instanceof BasicApplicationAdapter) {
            this.basePath = basePath;
            ((BasicApplicationAdapter)applicationAdapter).setBasePath(basePath);
        } else {
            throw new UnsupportedOperationException("Does not allow the base path change of ApplicationAdapter " + applicationAdapter);
        }
    }

    @Override
    public AspectranParameters getAspectranParameters() {
        return aspectranParameters;
    }

    @Override
    public void setAspectranParameters(AspectranParameters aspectranParameters) {
        this.aspectranParameters = aspectranParameters;
        this.rootConfigLocation = null;
    }

    @Override
    public String getRootConfigLocation() {
        return rootConfigLocation;
    }

    @Override
    public void setRootConfigLocation(String rootConfigLocation) {
        this.rootConfigLocation = rootConfigLocation;
        this.aspectranParameters = null;
    }

    @Override
    public String getEncoding() {
        return (encoding == null ? ActivityContext.DEFAULT_ENCODING : encoding);
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String[] getResourceLocations() {
        return resourceLocations;
    }

    @Override
    public void setResourceLocations(String[] resourceLocations) throws InvalidResourceException {
        if (resourceLocations != null) {
            aspectranClassLoader.setResourceLocations(resourceLocations);
        }
    }

    @Override
    public String[] getActiveProfiles() {
        return activeProfiles;
    }

    @Override
    public void setActiveProfiles(String... activeProfiles) {
        this.activeProfiles = activeProfiles;
    }

    @Override
    public String[] getDefaultProfiles() {
        return defaultProfiles;
    }

    @Override
    public void setDefaultProfiles(String... defaultProfiles) {
        this.defaultProfiles = defaultProfiles;
    }

    @Override
    public boolean isHybridLoad() {
        return hybridLoad;
    }

    @Override
    public void setHybridLoad(boolean hybridLoad) {
        this.hybridLoad = hybridLoad;
    }

    @Override
    public boolean isHardReload() {
        return hardReload;
    }

    @Override
    public void setHardReload(boolean hardReload) {
        this.hardReload = hardReload;
    }

    @Override
    public ServiceController getServiceController() {
        return serviceController;
    }

    @Override
    public void setServiceController(ServiceController serviceController) {
        this.serviceController = serviceController;
    }

    @Override
    public AspectranClassLoader getAspectranClassLoader() {
        return aspectranClassLoader;
    }

    protected void newAspectranClassLoader() throws InvalidResourceException {
        if (aspectranClassLoader == null || hardReload) {
            // The major packages in Aspectran are excluded because they
            // are already loaded by the parent class loader running Aspectran.
            String[] excludePackageNames = new String[] {
                    "com.aspectran.core",
                    "com.aspectran.scheduler",
                    "com.aspectran.embed",
                    "com.aspectran.shell",
                    "com.aspectran.shell-jline",
                    "com.aspectran.web",
                    "com.aspectran.with.jetty"
            };

            AspectranClassLoader acl = new AspectranClassLoader();
            acl.excludePackage(excludePackageNames);
            if (resourceLocations != null && resourceLocations.length > 0) {
                acl.setResourceLocations(resourceLocations);
            }
            aspectranClassLoader = acl;
            applicationAdapter.setClassLoader(acl);
        }
    }

    @Override
    public void initialize(ContextConfig contextConfig) throws InvalidResourceException {
        this.contextConfig = contextConfig;

        String basePath = contextConfig.getString(ContextConfig.base);
        if (basePath != null) {
            setBasePath(basePath);
        }

        this.rootConfigLocation = contextConfig.getString(ContextConfig.root);

        AspectranParameters aspectranParameters = contextConfig.getParameters(ContextConfig.parameters);
        if (aspectranParameters != null) {
            this.aspectranParameters = aspectranParameters;
        }

        this.encoding = contextConfig.getString(ContextConfig.encoding);

        String[] resourceLocations = contextConfig.getStringArray(ContextConfig.resources);
        this.resourceLocations = AspectranClassLoader.checkResourceLocations(resourceLocations, applicationAdapter.getBasePath());

        ContextProfilesConfig contextProfilesConfig = contextConfig.getParameters(ContextConfig.profiles);
        if (contextProfilesConfig != null) {
            this.activeProfiles = contextProfilesConfig.getStringArray(ContextProfilesConfig.activeProfiles);
            this.defaultProfiles = contextProfilesConfig.getStringArray(ContextProfilesConfig.defaultProfiles);
        }

        this.hybridLoad = contextConfig.getBoolean(ContextConfig.hybridLoad, false);

        ContextAutoReloadConfig contextAutoReloadConfig = contextConfig.getParameters(ContextConfig.autoReload);
        if (contextAutoReloadConfig != null) {
            String reloadMode = contextAutoReloadConfig.getString(ContextAutoReloadConfig.reloadMode);
            int scanIntervalSeconds = contextAutoReloadConfig.getInt(ContextAutoReloadConfig.scanIntervalSeconds, -1);
            boolean autoReloadStartup = contextAutoReloadConfig.getBoolean(ContextAutoReloadConfig.startup, false);
            this.hardReload = "hard".equals(reloadMode);
            this.autoReloadStartup = autoReloadStartup;
            this.scanIntervalSeconds = scanIntervalSeconds;
        }
        if (this.autoReloadStartup && (this.resourceLocations == null || this.resourceLocations.length == 0)) {
            this.autoReloadStartup = false;
        }
        if (this.autoReloadStartup) {
            if (this.scanIntervalSeconds == -1) {
                this.scanIntervalSeconds = 10;
                String contextAutoReloadingParamName = AspectranConfig.context.getName() + "." + ContextConfig.autoReload.getName();
                log.info("'" + contextAutoReloadingParamName + "' is not specified, defaulting to 10 seconds");
            }
        }
    }

    public void startReloadingTimer() {
        if (autoReloadStartup && aspectranClassLoader != null) {
            reloadingTimer = new ActivityContextReloadingTimer(serviceController, aspectranClassLoader.extractResources());
            reloadingTimer.start(scanIntervalSeconds);
        }
    }

    public void stopReloadingTimer() {
        if (reloadingTimer != null) {
            reloadingTimer.cancel();
            reloadingTimer = null;
        }
    }

}
