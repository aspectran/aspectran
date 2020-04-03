/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.InsufficientEnvironmentException;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.SchedulerConfig;
import com.aspectran.core.context.config.SystemConfig;
import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.scheduler.service.QuartzSchedulerService;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.core.util.FileLocker;
import com.aspectran.core.util.SystemUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.core.util.thread.ShutdownHooks;
import com.aspectran.core.util.wildcard.PluralWildcardPattern;

import java.io.File;
import java.io.IOException;

import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY_NAME;

/**
 * The Class AbstractCoreService.
 */
public abstract class AbstractCoreService extends AbstractServiceController implements CoreService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CoreService rootService;

    private final boolean lateStart;

    private String basePath;

    private AspectranConfig aspectranConfig;

    private PluralWildcardPattern exposableTransletNamesPattern;

    private ActivityContextBuilder activityContextBuilder;

    private ActivityContext activityContext;

    private SchedulerService schedulerService;

    private FileLocker fileLocker;

    public AbstractCoreService() {
        this(null);
    }

    public AbstractCoreService(CoreService rootService) {
        super(true);

        if (rootService != null) {
            if (rootService.getActivityContext() == null) {
                throw new IllegalStateException("Oops! No ActivityContext configured");
            }

            this.rootService = rootService;
            this.activityContext = rootService.getActivityContext();
            this.aspectranConfig = rootService.getAspectranConfig();
            this.lateStart = rootService.getServiceController().isActive();

            setBasePath(rootService.getBasePath());
            rootService.joinDerivedService(this);
        } else {
            this.rootService = null;
            this.lateStart = false;
        }
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    protected void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public boolean isLateStart() {
        return lateStart;
    }

    @Override
    public boolean isHardReload() {
        if (activityContextBuilder == null) {
            throw new IllegalStateException("ActivityContextLoader is not initialized; Call prepare() method first");
        }
        return activityContextBuilder.isHardReload();
    }

    @Override
    public ServiceController getServiceController() {
        return this;
    }

    @Override
    public void joinDerivedService(CoreService coreService) {
        super.joinDerivedService(coreService.getServiceController());
    }

    @Override
    public void withdrawDerivedService(CoreService coreService) {
        if (!coreService.isDerived()) {
            throw new IllegalArgumentException("Not derived service: " + coreService);
        }
        if (coreService.getServiceController().isActive()) {
            throw new IllegalArgumentException("Not stopped service: " + coreService);
        }
        super.withdrawDerivedService(coreService.getServiceController());
    }

    @Override
    public void leaveFromRootService() {
        if (!isDerived()) {
            throw new IllegalArgumentException("Not derived service: " + this);
        }
        if (isActive()) {
            throw new IllegalArgumentException("Not stopped service: " + this);
        }
        rootService.withdrawDerivedService(this);
    }

    @Override
    public boolean isDerived() {
        return (rootService != null);
    }

    protected boolean isExposable(String transletName) {
        return (exposableTransletNamesPattern == null || exposableTransletNamesPattern.matches(transletName));
    }

    protected void setExposals(String[] includePatterns, String[] excludePatterns) {
        if ((includePatterns != null && includePatterns.length > 0) ||
                excludePatterns != null && excludePatterns.length > 0) {
            exposableTransletNamesPattern = new PluralWildcardPattern(includePatterns, excludePatterns,
                    ActivityContext.NAME_SEPARATOR_CHAR);
        }
    }

    protected void prepare(AspectranConfig aspectranConfig) throws AspectranServiceException {
        if (activityContext != null) {
            throw new IllegalStateException("ActivityContext has already been loaded");
        }

        try {
            this.aspectranConfig = aspectranConfig;

            SystemConfig systemConfig = aspectranConfig.getSystemConfig();
            if (systemConfig != null) {
                for (String key : systemConfig.getPropertyKeys()) {
                    String value = systemConfig.getProperty(key);
                    if (value != null) {
                        System.setProperty(key, value);
                    }
                }
            }

            ContextConfig contextConfig = aspectranConfig.getContextConfig();
            if (contextConfig != null) {
                String basePath = contextConfig.getBasePath();
                if (basePath != null) {
                    setBasePath(basePath);
                }

                boolean singleton = contextConfig.isSingleton();
                if (singleton && !checkSingletonLock()) {
                    throw new InsufficientEnvironmentException("Another instance of Aspectran is already running; " +
                            "Only one instance is allowed (context.singleton is set to true)");
                }
            }

            activityContextBuilder = new HybridActivityContextBuilder(this);
            activityContextBuilder.setBasePath(getBasePath());
            activityContextBuilder.setContextConfig(contextConfig);
            activityContextBuilder.setServiceController(this);

            schedulerService = createSchedulerService(aspectranConfig.getSchedulerConfig());
        } catch (Exception e) {
            throw new AspectranServiceException("Unable to prepare the service", e);
        }
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

    @Override
    public ActivityContext getActivityContext() {
        return activityContext;
    }

    public void setActivityContext(ActivityContext activityContext) {
        this.activityContext = activityContext;
    }

    @Override
    public Activity getDefaultActivity() {
        if (getActivityContext() == null) {
            throw new IllegalStateException("ActivityContext is not yet created");
        }
        return getActivityContext().getDefaultActivity();
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
    public SchedulerService getSchedulerService() {
        return schedulerService;
    }

    private SchedulerService createSchedulerService(SchedulerConfig schedulerConfig) {
        if (schedulerConfig == null) {
            return null;
        }

        if (!schedulerConfig.isStartup()) {
            return null;
        }

        int startDelaySeconds = schedulerConfig.getStartDelaySeconds();
        boolean waitOnShutdown = schedulerConfig.isWaitOnShutdown();
        ExposalsConfig exposalsConfig = schedulerConfig.getExposalsConfig();

        if (startDelaySeconds == -1) {
            startDelaySeconds = 5;
            if (logger.isDebugEnabled()) {
                logger.debug("Scheduler option 'startDelaySeconds' is not specified, defaulting to 5 seconds");
            }
        }

        QuartzSchedulerService schedulerService = new QuartzSchedulerService(this);
        if (waitOnShutdown) {
            schedulerService.setWaitOnShutdown(true);
        }
        schedulerService.setStartDelaySeconds(startDelaySeconds);
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            schedulerService.setExposals(includePatterns, excludePatterns);
        }
        return schedulerService;
    }

    private boolean checkSingletonLock() throws Exception {
        if (fileLocker != null) {
            throw new IllegalStateException("Already instantiated a file locker for Singleton lock");
        }
        try {
            String basePath = getBasePath();
            if (basePath == null) {
                basePath = SystemUtils.getJavaIoTmpDir();
            }
            if (basePath != null) {
                fileLocker = new FileLocker(new File(basePath, ".lock"));
                if (fileLocker.lock()) {
                    ShutdownHooks.add(() -> {
                        if (fileLocker != null) {
                            try {
                                fileLocker.release();
                                fileLocker = null;
                            } catch (Exception e) {
                                logger.warn("Unable to release Singleton lock: " + e);
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

    protected void determineBasePath() {
        try {
            String baseDir = SystemUtils.getProperty(BASE_PATH_PROPERTY_NAME);
            if (baseDir != null) {
                File dir = new File(baseDir);
                if (!dir.isDirectory()) {
                    throw new IOException("Make sure it is a valid base directory; " +
                            BASE_PATH_PROPERTY_NAME + "=" + baseDir);
                }
            } else {
                baseDir = new File("").getCanonicalPath();
            }
            setBasePath(baseDir);
        } catch (IOException e) {
            throw new AspectranServiceException("Can not verify base directory");
        }
    }

}
