/*
 * Copyright 2008-2017 Juho Jeong
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
import com.aspectran.core.context.builder.config.AspectranContextAutoReloadConfig;
import com.aspectran.core.context.builder.config.AspectranContextConfig;
import com.aspectran.core.context.builder.config.AspectranContextProfilesConfig;
import com.aspectran.core.context.builder.reload.ActivityContextReloadingTimer;
import com.aspectran.core.context.builder.resource.AspectranClassLoader;
import com.aspectran.core.context.builder.resource.InvalidResourceException;
import com.aspectran.core.context.parser.apon.params.AspectranParameters;
import com.aspectran.core.service.AspectranServiceController;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

public abstract class AbstractActivityContextBuilder implements ActivityContextBuilder {

    protected final Log log = LogFactory.getLog(getClass());

    private final ApplicationAdapter applicationAdapter;

    private AspectranContextConfig aspectranContextConfig;

    private AspectranParameters aspectranParameters;

    private String basePath;

    private String rootContext;

    private String encoding;

    private String[] resourceLocations;

    private String[] activeProfiles;

    private String[] defaultProfiles;

    private boolean hybridLoad;

    private boolean hardReload;

    private boolean autoReloadStartup;

    private int scanIntervalSeconds;

    private ActivityContextReloadingTimer reloadingTimer;

    private AspectranServiceController aspectranServiceController;

    private AspectranClassLoader aspectranClassLoader;

    public AbstractActivityContextBuilder(ApplicationAdapter applicationAdapter) {
        if (applicationAdapter == null) {
            throw new IllegalArgumentException("The applicationAdapter argument must not be null.");
        }
        this.applicationAdapter = applicationAdapter;
        this.basePath = applicationAdapter.getBasePath();
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    @Override
    public AspectranContextConfig getAspectranContextConfig() {
        return aspectranContextConfig;
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
        this.rootContext = null;
    }

    @Override
    public String getRootContext() {
        return rootContext;
    }

    @Override
    public void setRootContext(String rootContext) {
        this.rootContext = rootContext;
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
    public AspectranServiceController getAspectranServiceController() {
        return aspectranServiceController;
    }

    @Override
    public void setAspectranServiceController(AspectranServiceController aspectranServiceController) {
        this.aspectranServiceController = aspectranServiceController;
    }

    @Override
    public AspectranClassLoader getAspectranClassLoader() {
        return aspectranClassLoader;
    }

    protected void newAspectranClassLoader() throws InvalidResourceException {
        if (aspectranClassLoader == null || hardReload) {
            String[] excludePackageNames = new String[] {
                    "com.aspectran.console",
                    "com.aspectran.core",
                    "com.aspectran.embedded",
                    "com.aspectran.scheduler",
                    "com.aspectran.web"
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
    public void initialize(AspectranContextConfig aspectranContextConfig) throws InvalidResourceException {
        this.aspectranContextConfig = aspectranContextConfig;

        String basePath = aspectranContextConfig.getString(AspectranContextConfig.base);
        if (basePath != null) {
            setBasePath(basePath);
        }

        this.rootContext = aspectranContextConfig.getString(AspectranContextConfig.root);

        AspectranParameters aspectranParameters = aspectranContextConfig.getParameters(AspectranContextConfig.parameters);
        if (aspectranParameters != null) {
            this.aspectranParameters = aspectranParameters;
        }

        this.encoding = aspectranContextConfig.getString(AspectranContextConfig.encoding);

        String[] resourceLocations = aspectranContextConfig.getStringArray(AspectranContextConfig.resources);
        this.resourceLocations = AspectranClassLoader.checkResourceLocations(resourceLocations, applicationAdapter.getBasePath());

        Parameters aspectranContextProfilesConfig = aspectranContextConfig.getParameters(AspectranContextConfig.profiles);
        if (aspectranContextProfilesConfig != null) {
            this.activeProfiles = aspectranContextProfilesConfig.getStringArray(AspectranContextProfilesConfig.activeProfiles);
            this.defaultProfiles = aspectranContextProfilesConfig.getStringArray(AspectranContextProfilesConfig.defaultProfiles);
        }

        this.hybridLoad = aspectranContextConfig.getBoolean(AspectranContextConfig.hybridLoad, false);

        Parameters aspectranContextAutoReloadConfig = aspectranContextConfig.getParameters(AspectranContextConfig.autoReload);
        if (aspectranContextAutoReloadConfig != null) {
            String reloadMode = aspectranContextAutoReloadConfig.getString(AspectranContextAutoReloadConfig.reloadMode);
            int scanIntervalSeconds = aspectranContextAutoReloadConfig.getInt(AspectranContextAutoReloadConfig.scanIntervalSeconds, -1);
            boolean autoReloadStartup = aspectranContextAutoReloadConfig.getBoolean(AspectranContextAutoReloadConfig.startup, false);
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
                String contextAutoReloadingParamName = AspectranConfig.context.getName() + "." + AspectranContextConfig.autoReload.getName();
                log.info("'" + contextAutoReloadingParamName + "' is not specified, defaulting to 10 seconds.");
            }
        }
    }

    public void startReloadingTimer() {
        if (autoReloadStartup) {
            if (aspectranClassLoader != null) {
                reloadingTimer = new ActivityContextReloadingTimer(aspectranServiceController, aspectranClassLoader.extractResources());
                reloadingTimer.start(scanIntervalSeconds);
            }
        }
    }

    public void stopReloadingTimer() {
        if (reloadingTimer != null) {
            reloadingTimer.cancel();
            reloadingTimer = null;
        }
    }

}
