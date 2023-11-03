/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.context.resource.SiblingsClassLoader;
import com.aspectran.core.scheduler.service.QuartzSchedulerService;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.FileLocker;
import com.aspectran.core.util.ShutdownHook;
import com.aspectran.core.util.SystemUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.core.util.wildcard.PluralWildcardPattern;

import java.io.File;
import java.io.IOException;

import static com.aspectran.core.context.config.AspectranConfig.BASE_PATH_PROPERTY_NAME;
import static com.aspectran.core.context.config.AspectranConfig.TEMP_PATH_PROPERTY_NAME;
import static com.aspectran.core.context.config.AspectranConfig.WORK_PATH_PROPERTY_NAME;

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
            Assert.state(rootService.getActivityContext() != null,
                    "Oops! No ActivityContext configured");

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
        Assert.state(activityContextBuilder != null,
                "No ActivityContextLoader configured; First, call the prepare() method");
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
        Assert.state(coreService.isDerived(), "Not derived service: " + coreService);
        Assert.state(!coreService.getServiceController().isActive(), "Not stopped service: " + coreService);
        super.withdrawDerivedService(coreService.getServiceController());
    }

    @Override
    public void leaveFromRootService() {
        Assert.state(isDerived(), "Not derived service: " + this);
        Assert.state(!isActive(), "Not stopped service: " + this);
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
        Assert.state(activityContext == null, "ActivityContext is already configured");

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
        Assert.state(activityContextBuilder != null,
                "No ActivityContextLoader configured; First, call the prepare() method");
        Assert.state(activityContext == null,
                "ActivityContext is already configured; " +
                        "Must destroy the current ActivityContext before reloading");

        activityContextBuilder.build();
    }

    protected void destroyActivityContext() {
        Assert.state(activityContextBuilder != null,
                "No ActivityContextLoader configured; First, call the prepare() method");

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
        Assert.state(getActivityContext() != null,
                "No ActivityContext configured yet");
        return getActivityContext().getDefaultActivity();
    }

    @Override
    public SiblingsClassLoader getSiblingsClassLoader() {
        Assert.state(activityContextBuilder != null,
                "No ActivityContextLoader configured; First, call the prepare() method");
        return activityContextBuilder.getSiblingsClassLoader();
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

        if (!schedulerConfig.isEnabled()) {
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
        Assert.state(fileLocker == null, "Singleton lock is already configured");
        try {
            String basePath = getBasePath();
            if (basePath == null) {
                basePath = SystemUtils.getJavaIoTmpDir();
            }
            Assert.state(basePath != null,
                    "Unable to determine the directory where the lock file will be located");
            fileLocker = new FileLocker(new File(basePath, ".lock"));
            if (fileLocker.lock()) {
                ShutdownHook.addTask(() -> {
                    if (fileLocker != null) {
                        try {
                            fileLocker.release();
                            fileLocker = null;
                        } catch (Exception e) {
                            logger.warn("Unable to release singleton lock: " + e);
                        }
                    }
                });
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Unable to acquire singleton lock", e);
        }
    }

    protected void checkDirectoryStructure() {
        // Determines the path of the base directory
        try {
            String basePath = SystemUtils.getProperty(BASE_PATH_PROPERTY_NAME);
            if (basePath != null) {
                File baseDir = new File(basePath);
                if (!baseDir.isDirectory()) {
                    throw new AspectranServiceException("Make sure it is a valid base directory; " +
                            BASE_PATH_PROPERTY_NAME + "=" + basePath);
                }
            } else {
                basePath = new File("").getCanonicalPath();
            }
            setBasePath(basePath);
        } catch (IOException e) {
            throw new AspectranServiceException("Could not verify the base directory", e);
        }

        // Determines the path of the working directory
        String workPath = SystemUtils.getProperty(WORK_PATH_PROPERTY_NAME);
        if (workPath != null) {
            File workDir = new File(workPath);
            if (!workDir.isDirectory()) {
                throw new AspectranServiceException("Make sure it is a valid working directory; " +
                        WORK_PATH_PROPERTY_NAME + "=" + workPath);
            }
        } else {
            /*
             * Sets the path to the working directory as a system property.
             * The property {@code aspectran.workPath} is set if the working
             * directory {@code work} exists under the base directory.
             */
            File workDir = new File(getBasePath(), "work");
            if (workDir.isDirectory()) {
                try {
                    System.setProperty(WORK_PATH_PROPERTY_NAME, workDir.getCanonicalPath());
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not verify the working directory: " + workDir);
                    }
                }
            }
        }

        // Determines the path of the temporary directory
        String tempPath = SystemUtils.getProperty(TEMP_PATH_PROPERTY_NAME);
        if (tempPath != null) {
            File tempDir = new File(tempPath);
            if (!tempDir.isDirectory()) {
                throw new AspectranServiceException("Make sure it is a valid temporary directory; " +
                        TEMP_PATH_PROPERTY_NAME + "=" + tempPath);
            }
        } else {
            /*
             * Sets the path to the temporary directory as a system property.
             * The property {@code aspectran.tempPath} is set if the temporary
             * directory {@code temp} exists under the base directory.
             */
            File tempDir = new File(getBasePath(), "temp");
            if (tempDir.isDirectory()) {
                try {
                    System.setProperty(TEMP_PATH_PROPERTY_NAME, tempDir.getCanonicalPath());
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not verify the temporary directory: " + tempDir);
                    }
                }
            }
        }
    }

}
