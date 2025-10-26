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
package com.aspectran.core.context.builder;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.Component;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.appender.ShallowRuleAppendHandler;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.parser.ActivityContextRuleParser;
import com.aspectran.core.context.rule.parser.HybridActivityContextRuleParser;
import com.aspectran.core.context.rule.parsing.RuleParsingContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ShutdownHook;
import com.aspectran.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The primary, concrete implementation of {@link ActivityContextBuilder}.
 *
 * <p>This builder orchestrates the entire process of parsing configuration,
 * creating component registries, and initializing a fully-functional
 * {@link ActivityContext}. It is designed to be thread-safe with respect to its
 * build and destroy operations.
 *
 * <p>For standalone applications not managed by a {@link CoreService}, it
 * automatically registers a shutdown hook to ensure graceful destruction of the
 * context.
 */
public class HybridActivityContextBuilder extends AbstractActivityContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(HybridActivityContextBuilder.class);

    /** Synchronization monitor for the "build" and "destroy" */
    private final Object buildDestroyMonitor = new Object();

    /** Flag that indicates whether an ActivityContext is activated */
    private final AtomicBoolean active = new AtomicBoolean();

    private ShutdownHook.Manager shutdownHookManager;

    private ActivityContext activityContext;

    public HybridActivityContextBuilder() {
        this(null);
    }

    public HybridActivityContextBuilder(CoreService masterService) {
        super(masterService);
    }

    @Override
    public ActivityContext build(AspectranParameters aspectranParameters) throws ActivityContextBuilderException {
        synchronized (this.buildDestroyMonitor) {
            setAspectranParameters(aspectranParameters);
            return doBuild();
        }
    }

    @Override
    public ActivityContext build(String... contextRules) throws ActivityContextBuilderException {
        synchronized (this.buildDestroyMonitor) {
            setContextRules(contextRules);
            return doBuild();
        }
    }

    @Override
    public ActivityContext build() throws ActivityContextBuilderException {
        synchronized (this.buildDestroyMonitor) {
            return doBuild();
        }
    }

    /**
     * Performs the actual build process, which includes the following steps:
     * <ol>
     *   <li>Creates a classloader.</li>
     *   <li>Parses the configuration rules (XML or APON). During this phase, if auto-reloading
     *       is enabled, it collects all appended configuration files.</li>
     *   <li>Creates the {@link ActivityContext} and its internal components (registries, etc.).</li>
     *   <li>Initializes the context if not managed by a master service.</li>
     *   <li>Starts the context reloading timer if auto-reloading is enabled and a master service exists.</li>
     *   <li>Registers a shutdown hook for standalone applications.</li>
     * </ol>
     * @return a fully initialized {@link ActivityContext}
     * @throws ActivityContextBuilderException if the build process fails
     */
    private ActivityContext doBuild() throws ActivityContextBuilderException {
        try {
            Assert.state(!this.active.get(), "ActivityContext is already configured");

            String[] contextRules = getContextRules();
            AspectranParameters aspectranParameters = getAspectranParameters();

            if (contextRules != null) {
                logger.info("Building ActivityContext with context rules [{}]",
                        StringUtils.joinWithCommas(contextRules));
            } else if (aspectranParameters != null) {
                logger.info("Building ActivityContext with specified parameters");
            } else if (logger.isDebugEnabled()) {
                logger.debug("No context rules configured");
            }

            long startTime = System.currentTimeMillis();

            String contextName = null;
            if (getMasterService() != null) {
                contextName = getMasterService().getContextName();
            } else if (getContextConfig() != null) {
                contextName = getContextConfig().getName();
            }

            ClassLoader parentClassLoader = null;
            if (getMasterService() != null && getMasterService().getParentService() != null) {
                CoreService parentService = getMasterService().getParentService();
                if (parentService != null) {
                    parentClassLoader = parentService.getActivityContext().getClassLoader();
                }
            }

            ClassLoader classLoader = createSiblingClassLoader(contextName, parentClassLoader);

            ApplicationAdapter applicationAdapter = null;
            if (getMasterService() != null) {
                applicationAdapter = getMasterService().getApplicationAdapter();
            }
            if (applicationAdapter == null) {
                applicationAdapter = createApplicationAdapter();
            }

            EnvironmentProfiles environmentProfiles = createEnvironmentProfiles(contextName);

            RuleParsingContext ruleParsingContext = new RuleParsingContext(classLoader, applicationAdapter, environmentProfiles);
            ruleParsingContext.prepare();

            if (getBasePackages() != null) {
                BeanRuleRegistry beanRuleRegistry = ruleParsingContext.getBeanRuleRegistry();
                beanRuleRegistry.scanConfigurableBeans(getBasePackages());
            }

            List<File> appendedFiles = new ArrayList<>();

            if (contextRules != null || aspectranParameters != null) {
                try (ActivityContextRuleParser parser = new HybridActivityContextRuleParser(ruleParsingContext)) {
                    parser.setEncoding(getEncoding());
                    parser.setUseXmlToApon(isUseAponToLoadXml());
                    parser.setDebugMode(isDebugMode());
                    if (isAutoReloadEnabled() && getMasterService() != null) {
                        parser.setFileAppendedListener(file -> {
                            if (file != null && !appendedFiles.contains(file)) {
                                appendedFiles.add(file);
                            }
                        });
                    }
                    if (contextRules != null) {
                        parser.parse(contextRules);
                    } else {
                        parser.parse(aspectranParameters);
                    }
                    ruleParsingContext = parser.getRuleParsingContext();
                    ruleParsingContext.clearCurrentRuleAppender();
                }
            } else {
                RuleAppendHandler ruleAppendHandler = new ShallowRuleAppendHandler(ruleParsingContext);
                ruleParsingContext.setRuleAppendHandler(ruleAppendHandler);
            }

            activityContext = createActivityContext(ruleParsingContext);
            ruleParsingContext.release();

            if (getMasterService() == null && activityContext instanceof Component component) {
                // If a MasterService is specified, the ActivityContext will be initialized
                // by that service, otherwise it must be explicitly initialized here
                component.initialize();
            }

            long elapsedTime = System.currentTimeMillis() - startTime;

            logger.info("ActivityContext build completed in {} ms", elapsedTime);

            if (getMasterService() != null) {
                // Timer starts only if it is driven by a service
                startContextReloadingTimer(appendedFiles);
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

    /**
     * Destroys the managed {@link ActivityContext} and cleans up all associated resources.
     * This includes stopping the reloading timer and removing any registered shutdown hooks.
     */
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
                if (activityContext instanceof Component component && component.isInitialized()) {
                    component.destroy();
                }
                activityContext = null;
            }
            this.active.set(false);
        }
    }

    private void registerDestroyTask() {
        shutdownHookManager = ShutdownHook.Manager.create(new ShutdownHook.Task() {
            @Override
            public void run() {
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
