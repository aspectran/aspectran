/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
import com.aspectran.core.component.Component;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.parser.ActivityContextParser;
import com.aspectran.core.context.rule.parser.HybridActivityContextParser;
import com.aspectran.core.service.AbstractCoreService;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.ShutdownHook;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class HybridActivityContextBuilder extends AbstractActivityContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(HybridActivityContextBuilder.class);

    private final AbstractCoreService coreService;

    private volatile ActivityContext activityContext;

    /** Flag that indicates whether an ActivityContext is activated */
    private final AtomicBoolean active = new AtomicBoolean();

    /** Synchronization monitor for the "build" and "destroy" */
    private final Object buildDestroyMonitor = new Object();

    /** Reference to the shutdown task, if registered */
    private ShutdownHook.Task shutdownTask;

    public HybridActivityContextBuilder() {
        super();
        this.coreService = null;
    }

    public HybridActivityContextBuilder(AbstractCoreService coreService) {
        super();
        this.coreService = coreService;
        setServiceController(coreService);
    }

    @Override
    public ActivityContext build(AspectranParameters aspectranParameters) throws ActivityContextBuilderException {
        synchronized (this.buildDestroyMonitor) {
            setAspectranParameters(aspectranParameters);
            return doBuild();
        }
    }

    @Override
    public ActivityContext build(String[] contextRules) throws ActivityContextBuilderException {
        synchronized (this.buildDestroyMonitor) {
            setContextRules(contextRules);
            return doBuild();
        }
    }

    @Override
    public ActivityContext build(String contextRuleFile) throws ActivityContextBuilderException {
        synchronized (this.buildDestroyMonitor) {
            setContextRules(new String[] {contextRuleFile});
            return doBuild();
        }
    }

    @Override
    public ActivityContext build() throws ActivityContextBuilderException {
        synchronized (this.buildDestroyMonitor) {
            return doBuild();
        }
    }

    private ActivityContext doBuild() throws ActivityContextBuilderException {
        try {
            Assert.state(!this.active.get(), "ActivityContext is already configured");

            String[] contextRules = getContextRules();
            AspectranParameters aspectranParameters = getAspectranParameters();

            if (contextRules != null) {
                logger.info("Building ActivityContext with context rules [" +
                        StringUtils.joinCommaDelimitedList(contextRules) + "]");
            } else if (aspectranParameters != null) {
                logger.info("Building ActivityContext with specified parameters");
            } else {
                logger.warn("No rootFile or aspectranParameters specified");
            }

            if (getActiveProfiles() != null) {
                logger.info("Activating profiles [" +
                        StringUtils.joinCommaDelimitedList(getActiveProfiles()) + "]");
            }

            if (getDefaultProfiles() != null) {
                logger.info("Default profiles [" +
                        StringUtils.joinCommaDelimitedList(getDefaultProfiles()) + "]");
            }

            long startTime = System.currentTimeMillis();

            ApplicationAdapter applicationAdapter = createApplicationAdapter();
            EnvironmentProfiles environmentProfiles = createEnvironmentProfiles();
            ActivityRuleAssistant assistant = new ActivityRuleAssistant(applicationAdapter, environmentProfiles);
            assistant.ready();

            if (getBasePackages() != null) {
                BeanRuleRegistry beanRuleRegistry = assistant.getBeanRuleRegistry();
                beanRuleRegistry.scanConfigurableBeans(getBasePackages());
            }

            if (contextRules != null || aspectranParameters != null) {
                ActivityContextParser parser = new HybridActivityContextParser(assistant);
                parser.setEncoding(getEncoding());
                parser.setUseXmlToApon(isUseAponToLoadXml());
                parser.setDebugMode(isDebugMode());
                if (contextRules != null) {
                    parser.parse(contextRules);
                } else {
                    parser.parse(aspectranParameters);
                }
                assistant = parser.getContextRuleAssistant();
                assistant.clearCurrentRuleAppender();
            }

            activityContext = createActivityContext(assistant);
            assistant.release();

            if (coreService != null) {
                coreService.setActivityContext(activityContext);
                activityContext.setRootService(coreService);
            }

            ((Component)activityContext).initialize();

            long elapsedTime = System.currentTimeMillis() - startTime;

            logger.info("ActivityContext build completed in " + elapsedTime + " ms");

            if (coreService == null) {
                // If it is driven by a builder without a service
                registerDestroyTask();
            } else {
                // Timer starts only if it is driven by a service
                startContextReloader();
            }

            this.active.set(true);

            return activityContext;
        } catch (Exception e) {
            throw new ActivityContextBuilderException("Failed to build ActivityContext", e);
        }
    }

    @Override
    public void destroy() {
        synchronized (this.buildDestroyMonitor) {
            doDestroy();
            removeDestroyTask();
        }
    }

    private void doDestroy() {
        if (this.active.get()) {
            stopContextReloader();
            if (activityContext != null) {
                ((Component)activityContext).destroy();
                activityContext = null;
            }
            if (coreService != null) {
                coreService.setActivityContext(null);
            }
            this.active.set(false);
        }
    }

    /**
     * Registers a shutdown hook with the JVM runtime, closing this context
     * on JVM shutdown unless it has already been closed at that time.
     */
    private void registerDestroyTask() {
        if (this.shutdownTask == null) {
            // Register a task to destroy the activity context on shutdown
            this.shutdownTask = ShutdownHook.addTask(() -> {
                synchronized (this.buildDestroyMonitor) {
                    doDestroy();
                    removeDestroyTask();
                }
            });
        }
    }

    /**
     * De-registers a shutdown hook with the JVM runtime.
     */
    private void removeDestroyTask() {
        // If we registered a JVM shutdown hook, we don't need it anymore now:
        // We've already explicitly closed the context.
        if (this.shutdownTask != null) {
            ShutdownHook.removeTask(this.shutdownTask);
            this.shutdownTask = null;
        }
    }

    @Override
    public boolean isActive() {
        return this.active.get();
    }

}
