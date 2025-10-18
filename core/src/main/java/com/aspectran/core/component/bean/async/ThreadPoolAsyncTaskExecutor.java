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
package com.aspectran.core.component.bean.async;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.thread.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An {@link AsyncTaskExecutor} implementation that uses a {@link ThreadPoolExecutor}.
 * <p>This is a highly configurable task executor that can be managed by the Aspectran
 * bean container, implementing {@link InitializableBean} and {@link DisposableBean}
 * for lifecycle management.</p>
 */
public class ThreadPoolAsyncTaskExecutor implements AsyncTaskExecutor, InitializableBean, DisposableBean {

    private String threadNamePrefix;

    private int corePoolSize = 1;

    private int maxPoolSize = Integer.MAX_VALUE;

    private int keepAliveSeconds = 60;

    private int queueCapacity = Integer.MAX_VALUE;

    private boolean waitForTasksToCompleteOnShutdown = false;

    private ExecutorService executor;

    /**
     * Sets the prefix for thread names created by this executor.
     * @param threadNamePrefix the thread name prefix
     */
    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    /**
     * Sets the core number of threads.
     * @param corePoolSize the core pool size
     */
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     * Sets the maximum allowed number of threads.
     * @param maxPoolSize the max pool size
     */
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * Sets the time limit (in seconds) for which threads may remain idle before
     * being terminated.
     * @param keepAliveSeconds the keep alive seconds
     */
    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    /**
     * Sets the capacity of the queue to use for holding tasks before they are
     * executed.
     * @param queueCapacity the queue capacity
     */
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    /**
     * Sets whether to wait for scheduled tasks to complete on shutdown.
     * @param waitForTasksToCompleteOnShutdown true to wait, false to not
     */
    public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
    }

    @Override
    public void execute(@NonNull Runnable task) {
        executor.execute(task);
    }

    @Override
    public void initialize() {
        if (threadNamePrefix == null) {
            threadNamePrefix = getClass().getSimpleName() + "-";
        }
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory(threadNamePrefix);
        threadFactory.setContextClassLoader(ClassUtils.getDefaultClassLoader());
        executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                threadFactory);
    }

    @Override
    public void destroy() {
        if (executor != null) {
            if (waitForTasksToCompleteOnShutdown) {
                executor.shutdown();
            } else {
                executor.shutdownNow();
            }
        }
    }

}
