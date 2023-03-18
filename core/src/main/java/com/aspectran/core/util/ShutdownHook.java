/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.util;

import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Manages the shutdown-hook thread and tasks to execute on shutdown.
 *
 * @since 4.0.0
 */
public class ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    private static final List<Task> tasks = new ArrayList<>();

    private static Thread hook;

    private static Win32ConsoleCtrlCloseHook win32ConsoleCtrlCloseHook;

    public static synchronized <T extends Task> T addTask(final T task) {
        if (task == null) {
            throw new IllegalArgumentException("task must not be null");
        }

        if (hook == null) {
            hook = new Thread("goodbye") {
                @Override
                public void run() {
                    runTasks();
                }
            };
            registerHook(hook);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Adding shutdown-hook task: " + task);
        }

        tasks.add(task);
        return task;
    }

    public static synchronized void removeTask(final Task task) {
        if (task == null) {
            throw new IllegalArgumentException("task must not be null");
        }

        // ignore if hook never installed
        if (hook == null) {
            return;
        }

        // Drop the task
        tasks.remove(task);

        // If there are no more tasks, then remove the hook thread
        if (tasks.isEmpty()) {
            releaseHook(hook);
            hook = null;
        }
    }

    private static void registerHook(final Thread thread) {
        if (logger.isDebugEnabled()) {
            logger.debug("Registering shutdown-hook: " + thread);
        }

        Runtime.getRuntime().addShutdownHook(thread);

        try {
            win32ConsoleCtrlCloseHook = Win32ConsoleCtrlCloseHook.register(hook);
        } catch (NoClassDefFoundError e) {
            win32ConsoleCtrlCloseHook = null;
            logger.warn(e);
        }
    }

    private static void releaseHook(final Thread thread) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing shutdown-hook: " + thread);
        }
        try {
            Runtime.getRuntime().removeShutdownHook(thread);
        } catch (IllegalStateException e) {
            // The VM is shutting down, not a big deal; ignore
        }

        if (win32ConsoleCtrlCloseHook != null) {
            win32ConsoleCtrlCloseHook.release();
            win32ConsoleCtrlCloseHook = null;
        }
    }

    private static synchronized void runTasks() {
        if (logger.isDebugEnabled()) {
            logger.debug("Running all shutdown-hook tasks");
        }

        List<Task> list = new ArrayList<>(tasks);
        for (ListIterator<Task> iter = list.listIterator(list.size()); iter.hasPrevious(); ) {
            Task task = iter.previous();
            if (logger.isDebugEnabled()) {
                logger.debug("Running task: " + task);
            }
            try {
                task.run();
            } catch (Throwable e) {
                logger.warn("Task failed", e);
            }
        }

        tasks.clear();
    }

    /**
     * Essentially a {@link Runnable} which allows running to throw an exception.
     */
    public interface Task {

        void run() throws Exception;

    }

}
