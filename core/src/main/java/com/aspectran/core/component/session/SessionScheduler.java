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
package com.aspectran.core.component.session;

import com.aspectran.utils.scheduling.Scheduler;
import com.aspectran.utils.thread.CustomizableThreadFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A specialized {@link Scheduler} implementation for managing background tasks
 * related to session management, such as session scavenging.
 *
 * <p>This scheduler ensures that session-related background operations are executed
 * efficiently and reliably within the Aspectran framework.</p>
 *
 * @see HouseKeeper
 */
public class SessionScheduler implements Scheduler {

    private final String name;

    private final ClassLoader classloader;

    private volatile ScheduledThreadPoolExecutor executor;

    /**
     * Instantiates a new SessionScheduler.
     * @param name the name of the scheduler
     * @param classLoader the class loader to use for the scheduler thread
     */
    public SessionScheduler(String name, ClassLoader classLoader) {
        this.name = name;
        this.classloader = classLoader;
    }

    @Override
    public Task schedule(Runnable task, long delay, TimeUnit unit) {
        return schedule(task, delay, unit, false);
    }

    @Override
    public Task schedule(Runnable task, long delay, TimeUnit unit, boolean mayInterruptIfRunning) {
        ScheduledThreadPoolExecutor executor = this.executor;
        if (executor == null) {
            return () -> false;
        }
        ScheduledFuture<?> scheduledFuture = executor.schedule(task, delay, unit);
        return new ScheduledFutureTask(scheduledFuture, mayInterruptIfRunning);
    }

    @Override
    public synchronized void start() {
        if (executor != null) {
            throw new IllegalStateException(name + " is already running");
        }

        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(name);
        threadFactory.setContextClassLoader(classloader);

        executor = new ScheduledThreadPoolExecutor(1, threadFactory);
        executor.setRemoveOnCancelPolicy(true);
    }

    @Override
    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    @Override
    public boolean isRunning() {
        return (executor != null);
    }

    private static class ScheduledFutureTask implements Task {

        private final ScheduledFuture<?> scheduledFuture;

        private final boolean mayInterruptIfRunning;

        ScheduledFutureTask(ScheduledFuture<?> scheduledFuture, boolean mayInterruptIfRunning) {
            this.scheduledFuture = scheduledFuture;
            this.mayInterruptIfRunning = mayInterruptIfRunning;
        }

        @Override
        public boolean cancel() {
            return scheduledFuture.cancel(mayInterruptIfRunning);
        }

    }

}
