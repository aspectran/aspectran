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
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A scheduler interface for executing tasks after a specified delay.
 * <p>This interface is a clone of {@code org.eclipse.jetty.util.thread.Scheduler}.</p>
 * <p>Implementations of this interface are responsible for managing the execution
 * of {@link Runnable} tasks at a future point in time.</p>
 */
public interface Scheduler extends AutoCloseable {

    /**
     * Schedules a task to be executed after the given delay.
     * @param task the task to schedule
     * @param delay the delay before the task is executed
     * @return a {@link Task} object that can be used to cancel the scheduled task
     */
    default Task schedule(@NonNull Runnable task, @NonNull Duration delay) {
        Assert.notNull(delay, "delay must not be null");
        return schedule(task, delay.toNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Schedules a task to be executed after the given delay, with an option to interrupt if running.
     * @param task the task to schedule
     * @param delay the delay before the task is executed
     * @param mayInterruptIfRunning {@code true} if the thread executing this task should be
     *                              interrupted when the task is cancelled, {@code false} otherwise
     * @return a {@link Task} object that can be used to cancel the scheduled task
     */
    default Task schedule(@NonNull Runnable task, @NonNull Duration delay, boolean mayInterruptIfRunning) {
        Assert.notNull(delay, "delay must not be null");
        return schedule(task, delay.toNanos(), TimeUnit.NANOSECONDS, mayInterruptIfRunning);
    }

    /**
     * Schedules a task to be executed after the given delay.
     * @param task the task to schedule
     * @param delay the delay before the task is executed
     * @param units the time unit of the delay
     * @return a {@link Task} object that can be used to cancel the scheduled task
     */
    Task schedule(Runnable task, long delay, TimeUnit units);

    /**
     * Schedules a task to be executed after the given delay, with an option to interrupt if running.
     * @param task the task to schedule
     * @param delay the delay before the task is executed
     * @param units the time unit of the delay
     * @param mayInterruptIfRunning {@code true} if the thread executing this task should be
     *                              interrupted when the task is cancelled, {@code false} otherwise
     * @return a {@link Task} object that can be used to cancel the scheduled task
     */
    Task schedule(Runnable task, long delay, TimeUnit units, boolean mayInterruptIfRunning);

    /**
     * Starts the scheduler.
     */
    void start();

    /**
     * Stops the scheduler.
     */
    void stop();

    /**
     * Checks if the scheduler is currently running.
     * @return {@code true} if the scheduler is running, {@code false} otherwise
     */
    boolean isRunning();

    @Override
    default void close() {
        stop();
    }

    /**
     * Represents a scheduled task that can be cancelled.
     */
    interface Task {

        /**
         * Attempts to cancel the execution of this task.
         * @return {@code true} if the task was successfully cancelled, {@code false} otherwise
         */
        boolean cancel();

        /**
         * Returns {@code true} if this task was cancelled before it completed normally.
         * @return {@code true} if this task was cancelled
         */
        default boolean isCancelled() {
            return false;
        }

        /**
         * Returns {@code true} if this task completed.
         * @return {@code true} if this task completed
         */
        default boolean isDone() {
            return false;
        }

    }

}
