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

import org.jspecify.annotations.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * An implementation of the {@link ThreadFactory} interface that allows for
 * customizing the created threads (name, priority, daemon status, etc.).
 * <p>This class extends {@link CustomizableThreadCreator} to provide the
 * configuration options and implements {@code ThreadFactory} to be used
 * with {@link java.util.concurrent.ExecutorService}s.</p>
 *
 * @see CustomizableThreadCreator
 */
public class CustomizableThreadFactory extends CustomizableThreadCreator implements ThreadFactory {

    /**
     * Creates a new CustomizableThreadFactory with a default thread name prefix.
     */
    public CustomizableThreadFactory() {
        super();
    }

    /**
     * Creates a new CustomizableThreadFactory with the given thread name prefix.
     * @param threadNamePrefix the prefix to use for the names of newly created threads
     */
    public CustomizableThreadFactory(String threadNamePrefix) {
        super(threadNamePrefix);
    }

    /**
     * Creates a new {@link Thread} for the given {@link Runnable}.
     * <p>This method delegates to {@link CustomizableThreadCreator#createThread(Runnable)}
     * to apply the configured thread properties.</p>
     * @param runnable the {@code Runnable} to execute
     * @return a newly created {@code Thread}
     */
    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        return createThread(runnable);
    }

}
