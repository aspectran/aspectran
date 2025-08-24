/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import java.util.concurrent.ThreadFactory;

/**
 * Implementation of the {@link java.util.concurrent.ThreadFactory} interface,
 * allowing for customizing the created threads (name, priority, etc).
 *
 * <p>See the base class {@link CustomizableThreadCreator}
 * for details on the available configuration options.
 *
 * @see #setThreadNamePrefix
 * @see #setThreadPriority
 */
public class CustomizableThreadFactory extends CustomizableThreadCreator implements ThreadFactory {

    /**
     * Create a new CustomizableThreadFactory with default thread name prefix.
     */
    public CustomizableThreadFactory() {
        super();
    }

    /**
     * Create a new CustomizableThreadFactory with the given thread name prefix.
     * @param threadNamePrefix the prefix to use for the names of newly created threads
     */
    public CustomizableThreadFactory(String threadNamePrefix) {
        super(threadNamePrefix);
    }


    @Override
    public Thread newThread(Runnable runnable) {
        return createThread(runnable);
    }

}
