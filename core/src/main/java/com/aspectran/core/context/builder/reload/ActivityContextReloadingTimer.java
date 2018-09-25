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
package com.aspectran.core.context.builder.reload;

import com.aspectran.core.service.ServiceController;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.net.URL;
import java.util.Timer;

/**
 * Provides timer control to reload the ActivityContext.
 */
public class ActivityContextReloadingTimer {

    private final Log log = LogFactory.getLog(ActivityContextReloadingTimer.class);

    private final ServiceController serviceController;

    private URL[] resources;

    private Timer timer;

    private ActivityContextReloadingTimerTask timerTask;

    public ActivityContextReloadingTimer(ServiceController serviceController) {
        this.serviceController = serviceController;
    }

    public void setResources(URL[] resources) {
        this.resources = resources;

        if (log.isDebugEnabled()) {
            log.debug("ActivityContextReloadingTimer is initialized");
        }
    }

    public void start(int scanIntervalSeconds) {
        stop();

        if (log.isDebugEnabled()) {
            log.debug("Starting ActivityContextReloadingTimer...");
        }

        timerTask = new ActivityContextReloadingTimerTask(serviceController);
        timerTask.setResources(resources);

        timer = new Timer("ActivityContextReloadingTimer");
        timer.schedule(timerTask, 0, scanIntervalSeconds * 1000L);
    }

    public void cancel() {
        stop();
    }

    protected void stop() {
        if (timer != null) {
            log.debug("Stopping ActivityContextReloadingTimer...");

            timer.cancel();
            timer = null;

            timerTask.cancel();
            timerTask = null;
        }
    }

}