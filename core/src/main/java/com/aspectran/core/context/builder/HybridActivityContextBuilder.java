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
import com.aspectran.utils.Assert;
import com.aspectran.utils.ShutdownHook;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class HybridActivityContextBuilder extends AbstractActivityContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(HybridActivityContextBuilder.class);

    private ApplicationAdapter applicationAdapter;

    private volatile ActivityContext activityContext;

    /** Flag that indicates whether an ActivityContext is activated */
    private final AtomicBoolean active = new AtomicBoolean();

    /** Synchronization monitor for the "build" and "destroy" */
    private final Object buildDestroyMonitor = new Object();

    private ShutdownHook.Manager shutdownHookManager;

    public HybridActivityContextBuilder() {
        super();
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
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
            setContextRules(new String[] { contextRuleFile });
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
                logger.warn("No context rules configured");
            }

            long startTime = System.currentTimeMillis();

            ClassLoader classLoader = createSiblingClassLoader();
            ApplicationAdapter applicationAdapter = this.applicationAdapter;
            if (applicationAdapter == null) {
                applicationAdapter = createApplicationAdapter();
            }
            EnvironmentProfiles environmentProfiles = createEnvironmentProfiles();
            ActivityRuleAssistant assistant = new ActivityRuleAssistant(
                classLoader, applicationAdapter, environmentProfiles);
            assistant.prepare();

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

            // When driven by a service
            if (getMasterService() != null) {
                // ActivityContext will be initialized in that service
                activityContext.setMasterService(getMasterService());
            } else {
                ((Component)activityContext).initialize();
            }

            long elapsedTime = System.currentTimeMillis() - startTime;

            logger.info("ActivityContext build completed in " + elapsedTime + " ms");

            if (getMasterService() != null) {
                // Timer starts only if it is driven by a service
                startContextReloadingTimer();
            } else {
                // If it is driven by a builder without a service
                registerDestroyTask();
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
            stopContextReloadingTimer();
            if (activityContext != null) {
                ((Component)activityContext).destroy();
                activityContext = null;
            }
            this.active.set(false);
        }
    }

    private void registerDestroyTask() {
        shutdownHookManager = ShutdownHook.Manager.create(new ShutdownHook.Task() {
            @Override
            public void run() throws Exception {
                synchronized (buildDestroyMonitor) {
                    doDestroy();
                }
            }

            @Override
            public String toString() {
                return "Destroy " + HybridActivityContextBuilder.class.getSimpleName();
            }
        });
    }

    private void removeDestroyTask() {
        if (shutdownHookManager != null) {
            shutdownHookManager.remove();
            shutdownHookManager = null;
        }
    }

    @Override
    public boolean isActive() {
        return this.active.get();
    }

}
