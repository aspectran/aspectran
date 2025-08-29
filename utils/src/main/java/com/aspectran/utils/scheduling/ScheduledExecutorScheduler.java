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
package com.aspectran.utils.scheduling;

import com.aspectran.utils.thread.CustomizableThreadFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link Scheduler} based on JDK's {@link ScheduledThreadPoolExecutor}.
 * <p>This class is a clone of {@code org.eclipse.jetty.util.thread.ScheduledExecutorScheduler}.</p>
 * <p>While the use of {@link ScheduledThreadPoolExecutor} creates futures that may not be directly used,
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

    /**
     * Creates a new ScheduledExecutorScheduler with a default name and non-daemon threads.
     */
    public ScheduledExecutorScheduler() {
        this(null, false);
    }

    /**
     * Creates a new ScheduledExecutorScheduler.
     * @param name the name prefix for the threads created by this scheduler
     * @param daemon {@code true} if the threads should be daemon threads
     */
    public ScheduledExecutorScheduler(String name, boolean daemon) {
        this(name, daemon, null);
    }

    /**
     * Creates a new ScheduledExecutorScheduler.
     * @param name the name prefix for the threads created by this scheduler
     * @param daemon {@code true} if the threads should be daemon threads
     * @param classLoader the ClassLoader to set as the context ClassLoader for new threads
     */
    public ScheduledExecutorScheduler(String name, boolean daemon, ClassLoader classLoader) {
        this(name, daemon, classLoader, null);
    }

    /**
     * Creates a new ScheduledExecutorScheduler.
     * @param name the name prefix for the threads created by this scheduler
     * @param daemon {@code true} if the threads should be daemon threads
     * @param classLoader the ClassLoader to set as the context ClassLoader for new threads
     * @param threadGroup the ThreadGroup to which new threads will belong
     */
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
            // If the executor is null (e.g., scheduler not started or stopped), return a no-op task.
            return () -> false;
        }
        ScheduledFuture<?> scheduledFuture = executor.schedule(task, delay, unit);
        return new ScheduledFutureTask(scheduledFuture, mayInterruptIfRunning);
    }

    /**
     * Starts the scheduler by initializing the underlying {@link ScheduledThreadPoolExecutor}.
     * @throws IllegalStateException if the scheduler is already running
     */
    @Override
    public synchronized void start() {
        if (executor != null) {
            throw new IllegalStateException("Scheduler " + name + " is already running");
        }

        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(name);
        threadFactory.setDaemon(daemon);
        threadFactory.setContextClassLoader(classloader);
        threadFactory.setThreadGroup(threadGroup);

        executor = new ScheduledThreadPoolExecutor(1, threadFactory);
        // This policy helps in garbage collection by removing cancelled tasks from the queue.
        executor.setRemoveOnCancelPolicy(true);
    }

    /**
     * Stops the scheduler by shutting down the underlying {@link ScheduledThreadPoolExecutor}.
     * Any currently executing tasks are interrupted, and no new tasks will be accepted.
     */
    @Override
    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * Checks if the scheduler is currently running.
     * @return {@code true} if the scheduler is running, {@code false} otherwise
     */
    @Override
    public boolean isRunning() {
        return (executor != null);
    }

    /**
     * An internal implementation of {@link Scheduler.Task} that wraps a {@link ScheduledFuture}.
     */
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
