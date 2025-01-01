/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.context.builder.reload;

import com.aspectran.core.service.ServiceLifeCycle;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.net.URL;
import java.util.Enumeration;
import java.util.Timer;

/**
 * Provides timer control to reload the ActivityContext.
 */
public class ContextReloadingTimer {

    private static final Logger logger = LoggerFactory.getLogger(ContextReloadingTimer.class);

    private final ServiceLifeCycle serviceLifeCycle;

    private Enumeration<URL> resources;

    private volatile Timer timer;

    private ContextReloadingTimerTask timerTask;

    public ContextReloadingTimer(ServiceLifeCycle serviceLifeCycle) {
        this.serviceLifeCycle = serviceLifeCycle;
    }

    public void setResources(Enumeration<URL> resources) {
        this.resources = resources;
    }

    public void start(int scanIntervalInSeconds) {
        stop();

        if (logger.isDebugEnabled()) {
            logger.debug("Starting ContextReloadingTimer...");
        }

        timerTask = new ContextReloadingTimerTask(serviceLifeCycle);
        timerTask.setResources(resources);

        timer = new Timer("ContextReloading");
        timer.schedule(timerTask, 0, scanIntervalInSeconds * 1000L);
    }

    public void stop() {
        if (timer != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Stopping ContextReloadingTimer...");
            }

            timer.cancel();
            timer = null;

            timerTask.cancel();
            timerTask = null;
        }
    }

}
