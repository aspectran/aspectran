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

import java.util.concurrent.atomic.AtomicBoolean;

import com.aspectran.core.adapter.BasicApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.parser.ActivityContextParser;
import com.aspectran.core.context.rule.parser.HybridActivityContextParser;
import com.aspectran.core.service.AbstractAspectranService;
import com.aspectran.core.util.ShutdownHooks;

public class HybridActivityContextBuilder extends AbstractActivityContextBuilder {

    private final AbstractAspectranService aspectranService;

    private ActivityContext activityContext;

    /** Flag that indicates whether an ActivityContext is activated */
    private final AtomicBoolean active = new AtomicBoolean();

    /** Synchronization monitor for the "build" and "destroy" */
    private final Object buildDestroyMonitor = new Object();

    /** Reference to the shutdown task, if registered */
    private ShutdownHooks.Task shutdownTask;

    public HybridActivityContextBuilder() {
        super(new BasicApplicationAdapter());
        this.aspectranService = null;
    }

    public HybridActivityContextBuilder(AbstractAspectranService aspectranService) {
        super(aspectranService.getApplicationAdapter());
        this.aspectranService = aspectranService;
        setAspectranServiceController(aspectranService);
    }

    @Override
    public ActivityContext build(AspectranParameters aspectranParameters) throws ActivityContextBuilderException {
        synchronized (this.buildDestroyMonitor) {
            setAspectranParameters(aspectranParameters);
            return doBuild();
        }
    }

    @Override
    public ActivityContext build(String rootContext) throws ActivityContextBuilderException {
        synchronized (this.buildDestroyMonitor) {
            setRootContext(rootContext);
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
            if (this.active.get()) {
                throw new IllegalStateException("An ActivityContext already activated");
            }

            String rootContext = getRootContext();
            AspectranParameters aspectranParameters = getAspectranParameters();

            if (rootContext == null && aspectranParameters == null) {
                throw new IllegalArgumentException("Either context.root or context.parameters must be specified in AspectranContextConfig: " + getAspectranContextConfig());
            }

            newAspectranClassLoader();

            log.info("Building ActivityContext with root configuration: " + rootContext);

            long startTime = System.currentTimeMillis();

            ActivityContextParser parser = new HybridActivityContextParser(getApplicationAdapter());
            parser.setActiveProfiles(getActiveProfiles());
            parser.setDefaultProfiles(getDefaultProfiles());
            parser.setEncoding(getEncoding());
            parser.setHybridLoad(isHybridLoad());

            ActivityContext activityContext;
            if (rootContext != null) {
                activityContext = parser.parse(rootContext);
            } else {
                activityContext = parser.parse(aspectranParameters);
            }

            this.activityContext = activityContext;

            if (aspectranService != null) {
                aspectranService.setActivityContext(activityContext);
            }

            activityContext.initialize(aspectranService);

            long elapsedTime = System.currentTimeMillis() - startTime;

            log.info("ActivityContext build completed in " + elapsedTime + " ms");

            registerDestroyTask();

            startReloadingTimer();

            this.active.set(true);

            return activityContext;
        } catch (Exception e) {
            throw new ActivityContextBuilderException("Failed to build an ActivityContext with " + getAspectranContextConfig(), e);
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
            stopReloadingTimer();

            getApplicationAdapter().getApplicationScope().destroy();

            if (activityContext != null) {
                activityContext.destroy();
                activityContext = null;

                log.info("ActivityContext has been destroyed");
            }

            if (aspectranService != null) {
                aspectranService.setActivityContext(null);
            }

            this.active.set(false);
        }
    }

    /**
     * Registers a shutdown hook with the JVM runtime, closing this context
     * on JVM shutdown unless it has already been closed at that time.
     */
    private void registerDestroyTask() {
        if (this.aspectranService == null && this.shutdownTask == null) {
            // Register a task to destroy the activity context on shutdown
            this.shutdownTask = ShutdownHooks.add(() -> {
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
            ShutdownHooks.remove(this.shutdownTask);
            this.shutdownTask = null;
        }
    }

    @Override
    public boolean isActive() {
        return this.active.get();
    }

}
