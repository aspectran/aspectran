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

    private ShutdownHook.Manager shutdownHookManager;

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
    }

    @Override
    public void start() throws Exception {
        super.start();
        if (!isDerived()) {
            registerShutdownTask();
        }
    }

    @Override
    public void stop() {
        super.stop();
        removeShutdownTask();
    }

    private void startAspectranService() throws Exception {
        loadActivityContext();
        afterContextLoaded();
    }

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
        shutdownHookManager = ShutdownHook.Manager.create(new ShutdownHook.Task() {
            @Override
            public void run() throws Exception {
                if (isActive()) {
                    AspectranCoreService.super.stop();
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
