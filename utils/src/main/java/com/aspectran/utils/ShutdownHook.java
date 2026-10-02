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
package com.aspectran.utils;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A utility class to manage the JVM shutdown hook and the tasks to be executed on shutdown.
 * <p>This class provides a centralized mechanism to add and remove shutdown tasks.
 * It ensures that the JVM shutdown hook is registered only once when the first task is added,
 * and removed when the last task is removed.</p>
 *
 * @since 4.0.0
 */
public class ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    private static final List<Task> tasks = new ArrayList<>();

    private static Thread hook;

    private static Win32ConsoleCtrlCloseHook win32ConsoleCtrlCloseHook;

    /**
     * This class cannot be instantiated.
     */
    private ShutdownHook() {
    }

    /**
     * Adds a task to be executed when the JVM shuts down.
     * <p>If this is the first task being added, a new shutdown hook thread is registered
     * with the JVM runtime.</p>
     * @param <T> the type of the task
     * @param task the task to add
     * @return the added task
     */
    public static synchronized <T extends Task> T addTask(@NonNull T task) {
        Assert.notNull(task, "task must not be null");

        if (hook == null) {
            hook = new Thread("shutdown") {
                @Override
                public void run() {
                    runTasks();

                    if (logger.isDebugEnabled()) {
                        logger.debug("Removed shutdown-hook: {}", hook);
                    }
                }
            };
            registerHook(hook);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Adding shutdown-hook task: {}", task);
        }

        tasks.add(task);
        return task;
    }

    /**
     * Removes a previously added shutdown task.
     * <p>If no tasks remain after removal, the JVM shutdown hook is deregistered.</p>
     * @param task the task to remove
     */
    public static synchronized void removeTask(Task task) {
        if (task == null || hook == null) {
            return;
        }

        // Drop the task
        if (tasks.remove(task)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Removed shutdown-hook task: {}", task);
            }
        }

        // If there are no more tasks, then remove the hook thread
        if (tasks.isEmpty()) {
            releaseHook(hook);
            hook = null;
        }
    }

    /**
     * Returns whether the given task is currently registered.
     * @param task the task to check
     * @return {@code true} if registered, {@code false} otherwise
     */
    public static synchronized boolean containsTask(Task task) {
        return (task != null && tasks.contains(task));
    }

    /**
     * Returns the number of registered shutdown tasks.
     * @return the number of tasks
     */
    public static synchronized int taskCount() {
        return tasks.size();
    }

    /**
     * Returns whether any shutdown tasks are registered.
     * @return {@code true} if tasks exist, {@code false} otherwise
     */
    public static synchronized boolean hasTasks() {
        return !tasks.isEmpty();
    }

    /**
     * Clears all registered shutdown tasks and removes the JVM shutdown hook.
     */
    public static synchronized void clearTasks() {
        tasks.clear();
        if (hook != null) {
            releaseHook(hook);
            hook = null;
        }
    }

    private static void registerHook(Thread thread) {
        if (logger.isDebugEnabled()) {
            logger.debug("Registering shutdown-hook: {}", thread);
        }

        Runtime.getRuntime().addShutdownHook(thread);

        try {
            win32ConsoleCtrlCloseHook = Win32ConsoleCtrlCloseHook.register(hook);
        } catch (NoClassDefFoundError e) {
            win32ConsoleCtrlCloseHook = null;
            logger.warn(e.getMessage(), e);
        }
    }

    private static void releaseHook(Thread thread) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing shutdown-hook: {}", thread);
        }
        try {
            Runtime.getRuntime().removeShutdownHook(thread);
        } catch (IllegalStateException e) {
            // The VM is shutting down, not a big deal; ignore
            logger.warn(e.getMessage(), e);
        }

        if (win32ConsoleCtrlCloseHook != null) {
            win32ConsoleCtrlCloseHook.close();
            win32ConsoleCtrlCloseHook = null;
        }
    }

    /**
     * Runs all registered shutdown tasks in reverse order of addition.
     */
    private static synchronized void runTasks() {
        if (logger.isDebugEnabled()) {
            logger.debug("Running all shutdown-hook tasks: {}", tasks.size());
        }

        List<Task> list = new ArrayList<>(tasks);
        int count = 0;
        for (ListIterator<Task> iter = list.listIterator(list.size()); iter.hasPrevious(); ) {
            Task task = iter.previous();
            if (tasks.contains(task)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Running task [{}/{}]: {}", ++count, list.size(), task);
                }
                try {
                    task.run();
                } catch (Throwable e) {
                    logger.warn("Failed to run task: {}", task, e);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Task already run or removed [{}/{}]: {}", ++count, list.size(), task);
                }
            }
        }
        tasks.clear();
    }

    /**
     * A task to be executed on shutdown. Essentially a {@link Runnable}
     * that allows its execution to throw an exception.
     */
    @FunctionalInterface
    public interface Task {

        /**
         * Executes the task.
         * @throws Exception if an error occurs during execution
         */
        void run() throws Exception;

    }

    /**
     * A simple manager to handle the registration and removal of a single shutdown task.
     * This simplifies the lifecycle management for components that need a shutdown hook.
     */
    public static class Manager implements AutoCloseable {

        private Task task;

        /**
         * Registers the given task with the central {@link ShutdownHook}.
         * If a task is already registered with this manager, this method does nothing.
         * @param task the task to register
         */
        public void register(@NonNull Task task) {
            if (this.task == null) {
                this.task = addTask(task);
            }
        }

        /**
         * Removes the task that was previously registered by this manager.
         * If no task is registered, this method does nothing.
         */
        public void remove() {
            if (this.task != null) {
                removeTask(this.task);
                this.task = null;
            }
        }

        /**
         * Returns whether a task is currently registered with this manager.
         * @return {@code true} if a task is registered, {@code false} otherwise
         */
        public boolean isRegistered() {
            return (this.task != null);
        }

        @Override
        public void close() {
            remove();
        }

        /**
         * Creates a new {@code Manager} and immediately registers the given task.
         * @param task the task to register
         * @return a new, configured {@code Manager} instance
         */
        @NonNull
        public static Manager create(@NonNull Task task) {
            Manager manager = new Manager();
            manager.register(task);
            return manager;
        }

    }

}
