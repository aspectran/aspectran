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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.Component;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.SystemConfig;
import com.aspectran.utils.Assert;
import com.aspectran.utils.FileLocker;
import com.aspectran.utils.InsufficientEnvironmentException;
import com.aspectran.utils.ShutdownHook;
import com.aspectran.utils.SystemUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

/**
 * The Class AspectranCoreService.
 */
public class AspectranCoreService extends AbstractCoreService {

    private final Logger logger = LoggerFactory.getLogger(AspectranCoreService.class);

    private FileLocker fileLocker;

    private ShutdownHook.Manager shutdownHookManager;

    /**
     * Instantiates a new AspectranCoreService.
     */
    public AspectranCoreService() {
        super();
    }

    public AspectranCoreService(CoreService parentService, boolean derived) {
        super(parentService, derived);
    }

    @Nullable
    protected ApplicationAdapter getApplicationAdapter() {
        if (getRootService().getActivityContext() != null) {
            return getRootService().getActivityContext().getApplicationAdapter();
        } else {
            return null;
        }
    }

    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        Assert.state(!isDerived(),
            "Must not be called for derived services");
        Assert.state(!hasActivityContextBuilder(),
            "prepare() method can be called only once");

        try {
            setAspectranConfig(aspectranConfig);

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
            if (getApplicationAdapter() == null && contextConfig != null) {
                String basePath = contextConfig.getBasePath();
                if (basePath != null) {
                    setBasePath(basePath);
                }
                if (contextConfig.isSingleton() && !acquireSingletonLock()) {
                    throw new InsufficientEnvironmentException("Another instance of Aspectran is already " +
                        "running; Only one instance is allowed (context.singleton is set to true)");
                }
            }

            ActivityContextBuilder activityContextBuilder = new HybridActivityContextBuilder();
            if (getApplicationAdapter() != null) {
                activityContextBuilder.setApplicationAdapter(getApplicationAdapter());
                activityContextBuilder.setBasePath(getApplicationAdapter().getBasePath());
            } else {
                activityContextBuilder.setBasePath(getBasePath());
            }
            activityContextBuilder.configure(contextConfig);
            activityContextBuilder.setMasterService(this);
            setActivityContextBuilder(activityContextBuilder);
        } catch (Exception e) {
            throw new AspectranServiceException("Unable to prepare the service", e);
        }
    }

    protected void buildActivityContext() throws ActivityContextBuilderException {
        Assert.state(getActivityContext() == null,
            "ActivityContext is already built; " +
                "Must destroy the current ActivityContext before reloading");
        ActivityContext activityContext = getActivityContextBuilder().build();
        setActivityContext(activityContext);
        try {
            ((Component)activityContext).initialize();
        } catch (Exception e) {
            throw new ActivityContextBuilderException("Failed to initialize ActivityContext", e);
        }
    }

    protected void destroyActivityContext() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying all cached resources...");
        }
        getActivityContextBuilder().destroy();
        setActivityContext(null);
    }

    /**
     * This method is executed immediately after the ActivityContext is loaded.
     * @throws Exception if an error occurs
     */
    protected void afterContextLoaded() throws Exception {
    }

    /**
     * This method executed just before the ActivityContext is destroyed.
     */
    protected void beforeContextDestroy() {
    }

    @Override
    protected void doStart() throws Exception {
        if (!isDerived()) {
            buildActivityContext();
            buildSchedulerService();
            afterContextLoaded();
        }
    }

    @Override
    protected void doStop() {
        if (!isDerived()) {
            clearDerivedServices();
            beforeContextDestroy();
            destroyActivityContext();
        }
    }

    @Override
    public void start() throws Exception {
        if (isRootService()) {
            registerShutdownTask();
        }
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        if (isRootService()) {
            releaseSingletonLock();
            removeShutdownTask();
        }
    }

    private boolean acquireSingletonLock() throws Exception {
        Assert.state(fileLocker == null, "Singleton lock is already configured");
        try {
            String basePath = getBasePath();
            if (basePath == null) {
                basePath = SystemUtils.getJavaIoTmpDir();
            }
            Assert.state(basePath != null,
                "Unable to determine the directory where the lock file will be located");
            fileLocker = new FileLocker(basePath);
            return fileLocker.lock();
        } catch (Exception e) {
            throw new Exception("Unable to acquire singleton lock", e);
        }
    }

    private void releaseSingletonLock() {
        if (fileLocker != null) {
            try {
                fileLocker.release();
                fileLocker = null;
            } catch (Exception e) {
                logger.warn("Unable to release singleton lock: " + e);
            }
        }
    }

    /**
     * Registers a shutdown hook with the JVM runtime, closing this context
     * on JVM shutdown unless it has already been closed at that time.
     */
    private void registerShutdownTask() {
        shutdownHookManager = ShutdownHook.Manager.create(new ShutdownHook.Task() {
            @Override
            public void run() throws Exception {
                if (isActive()) {
                    AspectranCoreService.super.stop();
                    releaseSingletonLock();
                }
            }

            @Override
            public String toString() {
                return "Stop " + getServiceName();
            }
        });
    }

    /**
     * De-registers a shutdown hook with the JVM runtime.
     */
    private void removeShutdownTask() {
        if (shutdownHookManager != null) {
            shutdownHookManager.remove();
            shutdownHookManager = null;
        }
    }

}
