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
import com.aspectran.core.util.Aspectran;
import com.aspectran.core.util.thread.ShutdownHooks;

/**
 * The Class AspectranCoreService.
 */
public class AspectranCoreService extends AbstractCoreService {

    private final CoreService rootService;

    /** Reference to the shutdown task, if registered */
    private ShutdownHooks.Task shutdownTask;

    /**
     * Instantiates a new AspectranCoreService.
     *
     * @param applicationAdapter the application adapter
     */
    public AspectranCoreService(ApplicationAdapter applicationAdapter) {
        super(applicationAdapter);
        this.rootService = null;
    }

    public AspectranCoreService(CoreService rootService) {
        super(rootService);
        this.rootService = rootService;
    }

    @Override
    public boolean isDerived() {
        return (rootService != null);
    }

    /**
     * This method is executed immediately after the ActivityContext is loaded.
     *
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
    public void doStart() throws Exception {
        log.info("Welcome to Aspectran " + Aspectran.VERSION);

        startAspectranService();
        if (!isDerived()) {
            registerShutdownTask();
        }
    }

    @Override
    public void doRestart() throws Exception {
        stopAspectranService();
        startAspectranService();
    }

    @Override
    public void doPause() throws Exception {
        pauseSchedulerService();
    }

    @Override
    public void doPause(long timeout) throws Exception {
        pauseSchedulerService(timeout);
    }

    @Override
    public void doResume() throws Exception {
        resumeSchedulerService();
    }

    @Override
    public void doStop() {
        stopAspectranService();
        removeShutdownTask();
    }

    private void startAspectranService() throws Exception {
        loadActivityContext();
        afterContextLoaded();
        startSchedulerService();

        if (isDerived()) {
            log.info(getServiceName() + " derived from " + rootService);
        }
    }

    /**
     * Actually performs destroys the singletons in the bean registry.
     * Called by both {@code shutdown()} and a JVM shutdown hook, if any.
     */
    private void stopAspectranService() {
        log.info("Destroying all cached resources...");

        stopSchedulerService();
        beforeContextDestroy();
        destroyActivityContext();
    }

    /**
     * Registers a shutdown hook with the JVM runtime, closing this context
     * on JVM shutdown unless it has already been closed at that time.
     */
    private void registerShutdownTask() {
        if (this.shutdownTask == null) {
            // Register a task to destroy the activity context on shutdown
            this.shutdownTask = ShutdownHooks.add(() -> {
                if (isActive()) {
                    stop();
                }
            });
        }
    }

    /**
     * De-registers a shutdown hook with the JVM runtime.
     */
    private void removeShutdownTask() {
        // If we registered a JVM shutdown hook, we don't need it anymore now:
        // We've already explicitly closed the context.
        if (this.shutdownTask != null) {
            ShutdownHooks.remove(this.shutdownTask);
            this.shutdownTask = null;
        }
    }

}