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

import com.aspectran.utils.Assert;
import com.aspectran.utils.thread.CustomizableThreadFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link Scheduler} based on JDK's {@link ScheduledThreadPoolExecutor}.
 * <p>This class is a clone of {@code org.eclipse.jetty.util.thread.ScheduledExecutorScheduler}.</p>
 * <p>It provides a robust way to schedule tasks for future execution, leveraging the capabilities
 * of {@code ScheduledThreadPoolExecutor}. It is particularly optimized for scenarios where tasks
 * might be cancelled, as it enables the {@code removeOnCancelPolicy} for efficient garbage collection.</p>
 */
public class ScheduledExecutorScheduler implements Scheduler {

    private final String name;

    private final boolean daemon;

    private final ClassLoader classloader;

    private final ThreadGroup threadGroup;

    private final int threads;

    private volatile ScheduledThreadPoolExecutor executor;

    /**
     * Creates a new ScheduledExecutorScheduler with a default name, non-daemon threads, and 1 thread.
     */
    public ScheduledExecutorScheduler() {
        this(null, false);
    }

    /**
     * Creates a new ScheduledExecutorScheduler with 1 thread.
     * @param name the name prefix for the threads created by this scheduler
     * @param daemon {@code true} if the threads should be daemon threads
     */
    public ScheduledExecutorScheduler(@Nullable String name, boolean daemon) {
        this(name, daemon, null);
    }

    /**
     * Creates a new ScheduledExecutorScheduler with 1 thread.
     * @param name the name prefix for the threads created by this scheduler
     * @param daemon {@code true} if the threads should be daemon threads
     * @param classLoader the ClassLoader to set as the context ClassLoader for new threads
     */
    public ScheduledExecutorScheduler(@Nullable String name, boolean daemon, @Nullable ClassLoader classLoader) {
        this(name, daemon, classLoader, null);
    }

    /**
     * Creates a new ScheduledExecutorScheduler with 1 thread.
     * @param name the name prefix for the threads created by this scheduler
     * @param daemon {@code true} if the threads should be daemon threads
     * @param classLoader the ClassLoader to set as the context ClassLoader for new threads
     * @param threadGroup the ThreadGroup to which new threads will belong
     */
    public ScheduledExecutorScheduler(
            @Nullable String name,
            boolean daemon,
            @Nullable ClassLoader classLoader,
            @Nullable ThreadGroup threadGroup) {
        this(name, daemon, classLoader, threadGroup, 1);
    }

    /**
     * Creates a new ScheduledExecutorScheduler with the specified thread count.
     * @param name the name prefix for the threads created by this scheduler
     * @param daemon {@code true} if the threads should be daemon threads
     * @param threads the number of threads to execute tasks
     */
    public ScheduledExecutorScheduler(@Nullable String name, boolean daemon, int threads) {
        this(name, daemon, null, null, threads);
    }

    /**
     * Creates a new ScheduledExecutorScheduler with the specified thread count.
     * @param name the name prefix for the threads created by this scheduler
     * @param daemon {@code true} if the threads should be daemon threads
     * @param classLoader the ClassLoader to set as the context ClassLoader for new threads
     * @param threadGroup the ThreadGroup to which new threads will belong
     * @param threads the number of threads to execute tasks
     */
    public ScheduledExecutorScheduler(
            @Nullable String name,
            boolean daemon,
            @Nullable ClassLoader classLoader,
            @Nullable ThreadGroup threadGroup,
            int threads) {
        this.name = (name == null ? "Scheduler-" + hashCode() : name);
        this.daemon = daemon;
        this.classloader = classLoader;
        this.threadGroup = threadGroup;
        this.threads = (threads > 0 ? threads : 1);
    }

    /**
     * Returns the name prefix for threads created by this scheduler.
     * @return the name prefix
     */
    public @NonNull String getName() {
        return name;
    }

    /**
     * Returns whether threads created by this scheduler are daemon threads.
     * @return {@code true} if daemon threads are created, {@code false} otherwise
     */
    public boolean isDaemon() {
        return daemon;
    }

    /**
     * Returns the number of threads in the scheduler pool.
     * @return the thread count
     */
    public int getThreads() {
        return threads;
    }

    @Override
    public @NonNull Task schedule(@NonNull Runnable task, long delay, @NonNull TimeUnit unit) {
        return schedule(task, delay, unit, false);
    }

    @Override
    public @NonNull Task schedule(
            @NonNull Runnable task,
            long delay,
            @NonNull TimeUnit unit,
            boolean mayInterruptIfRunning) {
        Assert.notNull(task, "task must not be null");
        Assert.notNull(unit, "unit must not be null");
        ScheduledThreadPoolExecutor executor = this.executor;
        if (executor == null) {
            // If the executor is null (e.g., scheduler not started or stopped), return a no-op task.
            return new NoOpTask();
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

        executor = new ScheduledThreadPoolExecutor(threads, threadFactory);
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

    private static class NoOpTask implements Task {

        @Override
        public boolean cancel() {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return true;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public String toString() {
            return "NoOpTask";
        }

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

        @Override
        public boolean isCancelled() {
            return scheduledFuture.isCancelled();
        }

        @Override
        public boolean isDone() {
            return scheduledFuture.isDone();
        }

        @Override
        public String toString() {
            return String.format("%s@%x[done=%b, cancelled=%b]",
                    getClass().getSimpleName(), hashCode(), isDone(), isCancelled());
        }

    }

}
