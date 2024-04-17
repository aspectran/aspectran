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
package com.aspectran.core.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.SchedulerConfig;
import com.aspectran.core.scheduler.service.QuartzSchedulerService;
import com.aspectran.core.scheduler.service.SchedulerService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.SystemUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

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

    private final CoreService parentService;

    private final boolean derived;

    private String basePath;

    private AspectranConfig aspectranConfig;

    private ActivityContextBuilder activityContextBuilder;

    private ActivityContext activityContext;

    private ClassLoader serviceClassLoader;

    private ClassLoader altClassLoader;

    private SchedulerService schedulerService;

    public AbstractCoreService() {
        this(null, false);
    }

    public AbstractCoreService(CoreService parentService, boolean derived) {
        super(true);

        if (parentService == null && derived) {
            throw new IllegalArgumentException("parentService must not be null");
        }

        this.parentService = parentService;
        this.derived = derived;

        if (parentService != null) {
            if (derived) {
                Assert.state(parentService.getActivityContext() != null,
                        "Oops! No ActivityContext configured");
                this.activityContext = parentService.getActivityContext();
                this.aspectranConfig = parentService.getAspectranConfig();
            }
            setBasePath(parentService.getBasePath());
            setRootService(parentService.getRootService());
            parentService.getRootService().joinDerivedService(this);
        } else {
            setRootService(this);
        }
    }

    @Override
    public CoreService getParentService() {
        return parentService;
    }

    @Override
    public boolean isDerived() {
        return derived;
    }

    @Override
    public boolean isLateStart() {
        return (parentService == null || parentService.getServiceController().isActive());
    }

    @Override
    public String getServiceName() {
        if (getActivityContext() != null && getActivityContext().getName() != null) {
            return ObjectUtils.simpleIdentityToString(this, getActivityContext().getName());
        } else {
            return super.getServiceName();
        }
    }

    @Override
    public ServiceController getServiceController() {
        return this;
    }

    @Override
    public void joinDerivedService(ServiceController serviceController) {
        super.joinDerivedService(serviceController);
    }

    @Override
    public void withdrawDerivedService(@NonNull CoreService coreService) {
        Assert.state(coreService.isDerived(), "Not derived service: " + coreService);
        Assert.state(!coreService.getServiceController().isActive(), "Not stopped service: " + coreService);
        super.withdrawDerivedService(coreService.getServiceController());
    }

    @Override
    public void leaveFromRootService() {
        if (isDerived()) {
            getRootService().withdrawDerivedService(this);
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
    public AspectranConfig getAspectranConfig() {
        return aspectranConfig;
    }

    protected void setAspectranConfig(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

    protected boolean hasActivityContextBuilder() {
        return (activityContextBuilder != null);
    }

    protected ActivityContextBuilder getActivityContextBuilder() {
        Assert.state(hasActivityContextBuilder(),
            "No ActivityContextLoader configured");
        return activityContextBuilder;
    }

    protected void setActivityContextBuilder(ActivityContextBuilder activityContextBuilder) {
        Assert.state(!hasActivityContextBuilder(),
            "ActivityContextBuilder is already configured");
        this.activityContextBuilder = activityContextBuilder;
    }

    @Override
    public ActivityContext getActivityContext() {
        return activityContext;
    }

    protected void setActivityContext(ActivityContext activityContext) {
        this.activityContext = activityContext;
    }

    @Override
    public Activity getDefaultActivity() {
        Assert.state(getActivityContext() != null,
            "No ActivityContext configured yet");
        return getActivityContext().getDefaultActivity();
    }

    @Override
    public boolean hasServiceClassLoader() {
        return (serviceClassLoader != null);
    }

    @Override
    @Nullable
    public ClassLoader getServiceClassLoader() {
        if (serviceClassLoader != null) {
            return serviceClassLoader;
        } else if (activityContext != null) {
            return activityContext.getClassLoader();
        } else if (getActivityContextBuilder() != null) {
            return getActivityContextBuilder().getClassLoader();
        } else {
            return null;
        }
    }

    protected void setServiceClassLoader(ClassLoader serviceClassLoader) {
        this.serviceClassLoader = serviceClassLoader;
    }

    @Override
    public ClassLoader getAltClassLoader() {
        return altClassLoader;
    }

    public void setAltClassLoader(ClassLoader altClassLoader) {
        this.altClassLoader = altClassLoader;
    }

    @Override
    public SchedulerService getSchedulerService() {
        return schedulerService;
    }

    protected void createSchedulerService(@NonNull SchedulerConfig schedulerConfig) {
        if (!schedulerConfig.isEnabled()) {
            return;
        }

        int startDelaySeconds = schedulerConfig.getStartDelaySeconds();
        boolean waitOnShutdown = schedulerConfig.isWaitOnShutdown();
        ExposalsConfig exposalsConfig = schedulerConfig.getExposalsConfig();

        if (startDelaySeconds == -1) {
            startDelaySeconds = 3;
            if (logger.isDebugEnabled()) {
                logger.debug("Scheduler option 'startDelaySeconds' is not specified, defaulting to 3 seconds");
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
        this.schedulerService = schedulerService;
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
