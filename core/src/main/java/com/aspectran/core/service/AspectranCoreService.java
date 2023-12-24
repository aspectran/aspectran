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

import com.aspectran.utils.ShutdownHook;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

/**
 * The Class AspectranCoreService.
 */
public class AspectranCoreService extends AbstractCoreService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Reference to the shutdown task, if registered */
    private ShutdownHook.Task shutdownTask;

    /**
     * Instantiates a new AspectranCoreService.
     */
    public AspectranCoreService() {
        super();
    }

    public AspectranCoreService(CoreService rootService) {
        super(rootService);
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
        startAspectranService();
        if (getSchedulerService() != null) {
            joinDerivedService(getSchedulerService());
        }
        if (!isDerived()) {
            registerShutdownTask();
        }
    }

    @Override
    protected void doPause() throws Exception {
    }

    @Override
    protected void doPause(long timeout) throws Exception {
    }

    @Override
    protected void doResume() throws Exception {
    }

    @Override
    protected void doStop() {
        clearDerivedService();
        stopAspectranService();
        removeShutdownTask();
    }

    private void startAspectranService() throws Exception {
        loadActivityContext();
        afterContextLoaded();
    }

    /**
     * Actually performs destroys the singletons in the bean registry.
     * Called by both {@code shutdown()} and a JVM shutdown hook, if any.
     */
    private void stopAspectranService() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying all cached resources...");
        }

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
            this.shutdownTask = ShutdownHook.addTask(() -> {
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
            ShutdownHook.removeTask(this.shutdownTask);
            this.shutdownTask = null;
        }
    }

}
