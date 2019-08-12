/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.thread;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link Scheduler} based on JDK's {@link ScheduledThreadPoolExecutor}.
 * <p>
 * While use of {@link ScheduledThreadPoolExecutor} creates futures that will not be used,
 * it has the advantage of allowing to set a property to remove cancelled tasks from its
 * queue even if the task did not fire, which provides a huge benefit in the performance
 * of garbage collection in young generation.</p>
 */
public class ScheduledExecutorScheduler implements Scheduler {

    private final String name;

    private final boolean daemon;

    private final ClassLoader classloader;

    private final ThreadGroup threadGroup;

    private volatile ScheduledThreadPoolExecutor scheduler;

    public ScheduledExecutorScheduler() {
        this(null, false);
    }

    public ScheduledExecutorScheduler(String name, boolean daemon) {
        this(name, daemon, Thread.currentThread().getContextClassLoader());
    }

    public ScheduledExecutorScheduler(String name, boolean daemon, ClassLoader threadFactoryClassLoader) {
        this(name, daemon, threadFactoryClassLoader, null);
    }

    public ScheduledExecutorScheduler(String name, boolean daemon, ClassLoader threadFactoryClassLoader, ThreadGroup threadGroup) {
        this.name = (name == null ? "scheduler-" + hashCode() : name);
        this.daemon = daemon;
        this.classloader = threadFactoryClassLoader == null ? Thread.currentThread().getContextClassLoader() : threadFactoryClassLoader;
        this.threadGroup = threadGroup;
    }

    @Override
    public Task schedule(Runnable task, long delay, TimeUnit unit) {
        ScheduledThreadPoolExecutor s = scheduler;
        if (s == null)
            return () -> false;
        ScheduledFuture<?> result = s.schedule(task, delay, unit);
        return new ScheduledFutureTask(result);
    }

    public void start() {
        if (scheduler != null) {
            throw new IllegalStateException("Scheduler " + name + " is already running");
        }
        scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(threadGroup, r, name);
            thread.setDaemon(daemon);
            thread.setContextClassLoader(classloader);
            return thread;
        });
        scheduler.setRemoveOnCancelPolicy(true);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private static class ScheduledFutureTask implements Task {

        private final ScheduledFuture<?> scheduledFuture;

        ScheduledFutureTask(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        @Override
        public boolean cancel() {
            return scheduledFuture.cancel(false);
        }

    }

}
