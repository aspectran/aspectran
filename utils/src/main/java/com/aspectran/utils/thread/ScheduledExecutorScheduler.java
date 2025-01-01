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
package com.aspectran.utils.thread;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>This class is a clone of org.eclipse.jetty.util.thread.ScheduledExecutorScheduler</p>
 *
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

    private volatile ScheduledThreadPoolExecutor executor;

    public ScheduledExecutorScheduler() {
        this(null, false);
    }

    public ScheduledExecutorScheduler(String name, boolean daemon) {
        this(name, daemon, null);
    }

    public ScheduledExecutorScheduler(String name, boolean daemon, ClassLoader classLoader) {
        this(name, daemon, classLoader, null);
    }

    public ScheduledExecutorScheduler(String name, boolean daemon, ClassLoader classLoader, ThreadGroup threadGroup) {
        this.name = (name == null ? "Scheduler-" + hashCode() : name);
        this.daemon = daemon;
        this.classloader = classLoader;
        this.threadGroup = threadGroup;
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
        ScheduledFuture<?> result = executor.schedule(task, delay, unit);
        return new ScheduledFutureTask(result, mayInterruptIfRunning);
    }

    @Override
    public synchronized void start() {
        if (executor != null) {
            throw new IllegalStateException("Scheduler " + name + " is already running");
        }
        executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(threadGroup, r, name);
            thread.setDaemon(daemon);
            if (classloader != null) {
                thread.setContextClassLoader(classloader);
            }
            return thread;
        });
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
