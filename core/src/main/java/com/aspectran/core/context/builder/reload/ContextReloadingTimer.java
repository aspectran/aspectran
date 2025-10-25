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
package com.aspectran.core.context.builder.reload;

import com.aspectran.core.service.ServiceLifeCycle;
import com.aspectran.utils.thread.CustomizableThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Manages the scheduled task for automatic reloading of the {@link com.aspectran.core.context.ActivityContext}.
 * <p>This class wraps a {@link ScheduledExecutorService} to schedule a {@link ContextReloadingTask}
 * at a fixed interval, enabling the hot-reloading feature.</p>
 *
 * @since 6.3.0
 */
public class ContextReloadingTimer {

    private static final Logger logger = LoggerFactory.getLogger(ContextReloadingTimer.class);

    private static final ThreadFactory THREAD_FACTORY;

    static {
        CustomizableThreadFactory tf = new CustomizableThreadFactory("ContextReloading-");
        tf.setDaemon(true);
        THREAD_FACTORY = tf;
    }

    private final int scanIntervalSeconds;

    private volatile ScheduledExecutorService executor;

    private final ContextReloadingTask task;

    /**
     * Instantiates a new ContextReloadingTimer.
     * @param serviceLifeCycle the service life cycle to restart on changes
     * @param scanIntervalSeconds the scan interval in seconds
     */
    public ContextReloadingTimer(ServiceLifeCycle serviceLifeCycle, int scanIntervalSeconds) {
        this.task = new ContextReloadingTask(serviceLifeCycle);
        this.scanIntervalSeconds = scanIntervalSeconds;
    }

    /**
     * Sets the classpath resources to be monitored for changes.
     * @param resources an enumeration of resource URLs, typically from a classloader
     */
    public void setResources(Enumeration<URL> resources) {
        task.setResources(resources);
    }

    /**
     * Adds a file-system based resource to be monitored for changes.
     * @param file the resource file to monitor
     */
    public void addResource(File file) {
        task.addResource(file);
    }

    /**
     * Returns whether there are any resources to monitor for current changes.
     * @return {@code true} if one or more monitored resources exist, otherwise {@code false}.
     */
    public boolean hasResources() {
        return task.hasResources();
    }

    /**
     * Starts the timer to monitor for resource changes.
     */
    public void start() {
        if (scanIntervalSeconds <= 0) {
            throw new IllegalArgumentException("scanIntervalSeconds must be greater than 0");
        }

        stop();

        if (logger.isDebugEnabled()) {
            logger.debug("Starting ContextReloadingTimer...");
        }

        executor = Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
        executor.scheduleAtFixedRate(task, 0, scanIntervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * Stops the timer and shuts down the underlying scheduler.
     */
    public void stop() {
        if (executor != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Stopping ContextReloadingTimer...");
            }
            executor.shutdownNow();
            executor = null;
        }
    }

}
