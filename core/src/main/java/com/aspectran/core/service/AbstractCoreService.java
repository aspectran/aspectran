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
package com.aspectran.core.service;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.SchedulerConfig;
import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.util.FileLocker;
import com.aspectran.core.util.SystemUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.thread.ShutdownHooks;
import com.aspectran.core.util.wildcard.PluralWildcardPattern;
import com.aspectran.scheduler.service.QuartzSchedulerService;
import com.aspectran.scheduler.service.SchedulerService;

import java.io.File;

/**
 * The Class AbstractCoreService.
 */
public abstract class AbstractCoreService extends AbstractServiceController implements CoreService {

    protected final Log log = LogFactory.getLog(getClass());

    private final ApplicationAdapter applicationAdapter;

    private String basePath;

    private AspectranConfig aspectranConfig;

    private SchedulerConfig schedulerConfig;

    private ActivityContextBuilder activityContextBuilder;

    private PluralWildcardPattern exposableTransletNamesPattern;

    private ActivityContext activityContext;

    private SchedulerService schedulerService;

    private FileLocker fileLocker;

    public AbstractCoreService(ApplicationAdapter applicationAdapter) {
        if (applicationAdapter == null) {
            throw new IllegalArgumentException("Argument 'applicationAdapter' must not be null");
        }

        this.applicationAdapter = applicationAdapter;
    }

    public AbstractCoreService(CoreService rootService) {
        if (rootService == null) {
            throw new IllegalArgumentException("Argument 'rootService' must not be null");
        }
        if (rootService.getActivityContext() == null) {
            throw new IllegalStateException("Oops! rootService's ActivityContext is not yet created");
        }

        this.applicationAdapter = rootService.getApplicationAdapter();
        this.activityContext = rootService.getActivityContext();
        this.aspectranConfig = rootService.getAspectranConfig();
    }

    public String getBasePath() {
        return basePath;
    }

    protected void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    @Override
    public ActivityContext getActivityContext() {
        return activityContext;
    }

    public void setActivityContext(ActivityContext activityContext) {
        this.activityContext = activityContext;
    }

    @Override
    public AspectranClassLoader getAspectranClassLoader() {
        if (activityContextBuilder == null) {
            throw new IllegalStateException("ActivityContextLoader is not initialized; Call prepare() method first");
        }
        return activityContextBuilder.getAspectranClassLoader();
    }

    @Override
    public AspectranConfig getAspectranConfig() {
        return aspectranConfig;
    }

    @Override
    public boolean isHardReload() {
        if (activityContextBuilder == null) {
            throw new IllegalStateException("ActivityContextLoader is not initialized; Call prepare() method first");
        }
        return activityContextBuilder.isHardReload();
    }

    protected void prepare(AspectranConfig aspectranConfig) throws AspectranServiceException {
        if (activityContext != null) {
            throw new IllegalStateException("ActivityContext has already been loaded");
        }

        try {
            this.aspectranConfig = aspectranConfig;
            this.schedulerConfig = aspectranConfig.getSchedulerConfig();

            ContextConfig contextConfig = aspectranConfig.getContextConfig();
            if (contextConfig != null) {
                Boolean singleton = contextConfig.getBoolean(ContextConfig.singleton);
                if (Boolean.TRUE.equals(singleton)) {
                    if (!checkSingletonLock()) {
                        throw new IllegalStateException("Allow only one instance of Aspectran to run");
                    }
                }
            }

            activityContextBuilder = new HybridActivityContextBuilder(this);
            activityContextBuilder.setBasePath(basePath);
            activityContextBuilder.setContextConfig(contextConfig);
            activityContextBuilder.setServiceController(this);
        } catch (Exception e) {
            throw new AspectranServiceException("Unable to prepare the service", e);
        }
    }

    protected void setExposals(String[] includePatterns, String[] excludePatterns) {
        if ((includePatterns != null && includePatterns.length > 0) ||
                excludePatterns != null && excludePatterns.length > 0) {
            exposableTransletNamesPattern = new PluralWildcardPattern(includePatterns, excludePatterns,
                    ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR);
        }
    }

    protected boolean isExposable(String transletName) {
        return (exposableTransletNamesPattern == null || exposableTransletNamesPattern.matches(transletName));
    }

    protected void loadActivityContext() throws ActivityContextBuilderException {
        if (activityContextBuilder == null) {
            throw new IllegalStateException("ActivityContextLoader is not in an instantiated state; First, call the prepare() method");
        }
        if (activityContext != null) {
            throw new IllegalStateException("ActivityContext has already been loaded; Must destroy the current ActivityContext before reloading");
        }

        activityContextBuilder.build();
    }

    protected void destroyActivityContext() {
        if (activityContextBuilder == null) {
            throw new IllegalStateException("ActivityContextBuilder is not in an instantiated state;" +
                    " First, call the prepare() method");
        }

        activityContextBuilder.destroy();
    }

    protected void startSchedulerService() throws Exception {
        if (this.schedulerConfig == null) {
            return;
        }

        boolean schedulerStartup = this.schedulerConfig.getBoolean(SchedulerConfig.startup);
        int startDelaySeconds = this.schedulerConfig.getInt(SchedulerConfig.startDelaySeconds.getName(), -1);
        boolean waitOnShutdown = this.schedulerConfig.getBoolean(SchedulerConfig.waitOnShutdown);
        ExposalsConfig exposalsConfig = this.schedulerConfig.getExposalsConfig();

        if (schedulerStartup) {
            if (startDelaySeconds == -1) {
                log.info("Scheduler option 'startDelaySeconds' not specified; So defaulting to 5 seconds");
                startDelaySeconds = 5;
            }

            SchedulerService newSchedulerService = new QuartzSchedulerService(activityContext);
            if (waitOnShutdown) {
                newSchedulerService.setWaitOnShutdown(true);
            }
            newSchedulerService.setStartDelaySeconds(startDelaySeconds);
            if (exposalsConfig != null) {
                String[] includePatterns = exposalsConfig.getStringArray(ExposalsConfig.plus);
                String[] excludePatterns = exposalsConfig.getStringArray(ExposalsConfig.minus);
                newSchedulerService.setExposals(includePatterns, excludePatterns);
            }
            newSchedulerService.start();

            this.schedulerService = newSchedulerService;
        }
    }

    protected void stopSchedulerService() {
        if (schedulerService != null) {
            schedulerService.stop();
            schedulerService = null;
        }
    }

    protected void pauseSchedulerService() throws Exception {
        if (schedulerService != null) {
            schedulerService.pause();
        }
    }

    protected void pauseSchedulerService(long timeout) {
        log.warn(schedulerService.getServiceName() + " does not support pausing for a certain period of time");
    }

    protected void resumeSchedulerService() throws Exception {
        if (schedulerService != null) {
            schedulerService.resume();
        }
    }

    private boolean checkSingletonLock() throws Exception {
        if (fileLocker != null) {
            throw new IllegalStateException("Already instantiated a file locker for Singleton lock");
        }
        try {
            String basePath = getBasePath();
            if (basePath == null) {
                basePath = SystemUtils.getProperty("java.io.tmpdir");
            }
            if (basePath != null) {
                fileLocker = new FileLocker(new File(basePath, ".lock"));
                if (fileLocker.lock()) {
                    ShutdownHooks.add(() -> {
                        if (fileLocker != null) {
                            try {
                                fileLocker.release();
                            } catch (Exception e) {
                                log.warn("Unable to release Singleton lock", e);
                            }
                        }
                    });
                    return true;
                } else {
                    return false;
                }
            } else {
                throw new IllegalArgumentException("Unable to determine the directory where the lock file will be located");
            }
        } catch (Exception e) {
            throw new Exception("Unable to acquire Singleton lock", e);
        }
    }

}
