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
package com.aspectran.utils.thread;

import com.aspectran.utils.ClassUtils;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * A simple customizable helper class for creating new {@link Thread} instances.
 * <p>This class is a clone of {@code org.springframework.util.CustomizableThreadCreator}.</p>
 * <p>It provides various configurable properties such as thread name prefix, thread priority,
 * daemon status, and thread group. It serves as a base class for more specific thread factories
 * like {@link CustomizableThreadFactory}.</p>
 *
 * @see CustomizableThreadFactory
 */
public class CustomizableThreadCreator {

    private final AtomicInteger threadCount = new AtomicInteger();

    private String threadNamePrefix;

    private int threadPriority = Thread.NORM_PRIORITY;

    private boolean daemon = false;

    @Nullable
    private ThreadGroup threadGroup;

    private ClassLoader contextClassLoader;

    /**
     * Creates a new CustomizableThreadCreator with a default thread name prefix.
     */
    public CustomizableThreadCreator() {
        this.threadNamePrefix = getDefaultThreadNamePrefix();
    }

    /**
     * Creates a new CustomizableThreadCreator with the given thread name prefix.
     * @param threadNamePrefix the prefix to use for the names of newly created threads
     */
    public CustomizableThreadCreator(@Nullable String threadNamePrefix) {
        this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
    }

    /**
     * Specifies the prefix to use for the names of newly created threads.
     * Default is "SimpleAsyncTaskExecutor-".
     * @param threadNamePrefix the prefix to use
     */
    public void setThreadNamePrefix(@Nullable String threadNamePrefix) {
        this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
    }

    /**
     * Returns the thread name prefix used for the names of newly created threads.
     * @return the thread name prefix
     */
    public String getThreadNamePrefix() {
        return this.threadNamePrefix;
    }

    /**
     * Sets the priority of the threads that this factory creates.
     * Default is {@link Thread#NORM_PRIORITY} (5).
     * @param threadPriority the priority to set
     * @see Thread#setPriority
     */
    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    /**
     * Returns the priority of the threads that this factory creates.
     * @return the thread priority
     */
    public int getThreadPriority() {
        return this.threadPriority;
    }

    /**
     * Sets whether this factory is supposed to create daemon threads.
     * <p>Daemon threads are typically used for background tasks that should not prevent the JVM from exiting.
     * Default is {@code false}.</p>
     * @param daemon {@code true} to create daemon threads, {@code false} otherwise
     * @see Thread#setDaemon
     */
    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    /**
     * Returns whether this factory should create daemon threads.
     * @return {@code true} if daemon threads are created, {@code false} otherwise
     */
    public boolean isDaemon() {
        return this.daemon;
    }

    /**
     * Specifies the name of the thread group that threads should be created in.
     * A new {@link ThreadGroup} will be created with this name.
     * @param name the name of the thread group
     * @see #setThreadGroup(ThreadGroup)
     */
    public void setThreadGroupName(String name) {
        this.threadGroup = new ThreadGroup(name);
    }

    /**
     * Specifies the thread group that threads should be created in.
     * @param threadGroup the {@link ThreadGroup} to use (may be {@code null} for the default group)
     * @see #setThreadGroupName(String)
     */
    public void setThreadGroup(@Nullable ThreadGroup threadGroup) {
        this.threadGroup = threadGroup;
    }

    /**
     * Returns the thread group that threads should be created in
     * (or {@code null} for the default group).
     * @return the thread group
     */
    @Nullable
    public ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }

    /**
     * Returns the context ClassLoader to be set for new threads.
     * @return the context ClassLoader
     */
    public ClassLoader getContextClassLoader() {
        return this.contextClassLoader;
    }

    /**
     * Sets the context ClassLoader to be set for new threads.
     * @param contextClassLoader the context ClassLoader to set
     */
    public void setContextClassLoader(ClassLoader contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
    }

    /**
     * Template method for the creation of a new {@link Thread}.
     * <p>The default implementation creates a new Thread for the given
     * {@link Runnable}, applying an appropriate thread name, priority, daemon status,
     * and context ClassLoader.</p>
     * @param runnable the {@code Runnable} to execute
     * @return a newly created {@code Thread}
     * @see #nextThreadName()
     */
    public Thread createThread(Runnable runnable) {
        Thread thread = new Thread(getThreadGroup(), runnable, nextThreadName());
        thread.setPriority(getThreadPriority());
        thread.setDaemon(isDaemon());
        if (contextClassLoader != null) {
            thread.setContextClassLoader(contextClassLoader);
        }
        return thread;
    }

    /**
     * Returns the thread name to use for a newly created {@link Thread}.
     * <p>The default implementation returns the specified thread name prefix
     * with an increasing thread count appended: for example, "SimpleAsyncTaskExecutor-0".</p>
     * @return the generated thread name
     * @see #getThreadNamePrefix()
     */
    protected String nextThreadName() {
        return getThreadNamePrefix() + threadCount.incrementAndGet();
    }

    /**
     * Builds the default thread name prefix for this factory.
     * @return the default thread name prefix (never {@code null})
     */
    protected String getDefaultThreadNamePrefix() {
        return ClassUtils.getShortName(getClass()) + "-";
    }

}
