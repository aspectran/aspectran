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
package com.aspectran.core.service;

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
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DefaultCoreService.
 */
public class DefaultCoreService extends AbstractCoreService {

    private final Logger logger = LoggerFactory.getLogger(DefaultCoreService.class);

    private FileLocker fileLocker;

    private ShutdownHook.Manager shutdownHookManager;

    /**
     * Instantiates a new DefaultCoreService.
     */
    public DefaultCoreService() {
        super();
    }

    public DefaultCoreService(CoreService parentService, boolean derived) {
        super(parentService, derived);
    }

    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        Assert.state(!isDerived(), "Must not be called for derived services");
        Assert.state(!hasActivityContextBuilder(), "prepare() method can be called only once");

        try {
            setAspectranConfig(aspectranConfig);

            SystemConfig systemConfig = aspectranConfig.getSystemConfig();
            if (systemConfig != null) {
                for (String name : systemConfig.getPropertyNames()) {
                    String value = systemConfig.getProperty(name);
                    if (value != null) {
                        System.setProperty(name, value);
                    }
                }
            }

            ContextConfig contextConfig = aspectranConfig.getContextConfig();
            if (isRootService()) {
                if (getBasePath() == null && contextConfig != null && contextConfig.hasBasePath()) {
                    setBasePath(contextConfig.getBasePath());
                }
            } else {
                setBasePath(getParentService().getBasePath());
            }
            if (getContextName() == null && contextConfig != null && contextConfig.hasName()) {
                setContextName(contextConfig.getName());
            }

            ActivityContextBuilder activityContextBuilder = new HybridActivityContextBuilder(this);
            activityContextBuilder.configure(contextConfig);
            setActivityContextBuilder(activityContextBuilder);

            if (getBasePath() == null) {
                setBasePath(activityContextBuilder.getBasePath());
            }

            if (isRootService() && contextConfig != null && contextConfig.isSingleton()) {
                if (activityContextBuilder.hasOwnBasePath()) {
                    acquireSingletonLock();
                } else {
                    logger.warn("Since no base directory is explicitly specified, no singleton lock is applied");
                }
            }
        } catch (Exception e) {
            throw new CoreServiceException("Unable to prepare the service", e);
        }
    }

    protected void buildActivityContext() throws ActivityContextBuilderException {
        Assert.state(getActivityContext() == null,
                "ActivityContext is already built; Must destroy the current ActivityContext before reloading");
        ActivityContext activityContext = getActivityContextBuilder().build();
        setActivityContext(activityContext);
        try {
            ((Component)activityContext).initialize();
        } catch (Exception e) {
            throw new ActivityContextBuilderException("Failed to initialize " +
                    ((Component)activityContext).getComponentName(), e);
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
            getActivityContextBuilder().clear();
        }
    }

    @Override
    protected void doStart() throws Exception {
        if (!isDerived()) {
            buildActivityContext();
            buildSchedulerService();
            afterContextLoaded();
        }
        initFlashMapManager();
        initLocaleResolver();
    }

    @Override
    protected void doStop() {
        if (!isDerived()) {
            beforeContextDestroy();
            destroySchedulerService();
            destroyActivityContext();
        }
    }

    private void acquireSingletonLock() throws Exception {
        Assert.state(fileLocker == null, "Singleton lock is already configured");
        fileLocker = new FileLocker(getBasePath());
        if (!fileLocker.lock()) {
            throw new InsufficientEnvironmentException("Another instance of Aspectran is already " +
                "running; Only one instance is allowed (context.singleton is set to true)");
        }
    }

    private void releaseSingletonLock() {
        if (fileLocker != null) {
            try {
                fileLocker.release();
                fileLocker = null;
            } catch (Exception e) {
                logger.warn("Unable to release singleton lock; {}", e.toString());
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
            public void run() {
                if (isActive()) {
                    DefaultCoreService.super.stop();
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
