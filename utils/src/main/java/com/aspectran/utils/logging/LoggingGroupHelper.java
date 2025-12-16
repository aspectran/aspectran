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
package com.aspectran.utils.logging;

import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;

/**
 * Utility class for managing a logical "logging group" identifier in the
 * SLF4J Mapped Diagnostic Context (MDC).
 * <p>
 * A logging group is a string that ties a sequence of log events together
 * (for example, per HTTP request, background job, or user session). Because
 * many logging backends write asynchronously and application servers often
 * reuse threads from pools, removing the key too early may cause late log
 * events to be written without the expected context, or may leave the key
 * associated with a thread that is reused for the next task. To avoid leaks
 * and missing context, set the logging group at the beginning of the unit of
 * work and clear it in a finally block.
 * </p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * try {
 *     LoggingGroupHelper.set("request-12345");
 *     // perform work and write logs
 * } finally {
 *     LoggingGroupHelper.clear();
 * }
 * }</pre>
 *
 * Thread-safety note: MDC stores values per-thread. When using executors or
 * reactive frameworks, you may need to propagate the MDC to child threads
 * explicitly depending on the MDC implementation and runtime environment.
 *
 * @see org.slf4j.MDC
 */
public abstract class LoggingGroupHelper {

    /**
     * MDC key used to store the logging group name.
     */
    public static final String LOGGING_GROUP = "LOGGING_GROUP";

    /**
     * Associates the given group name with the current thread's MDC under the
     * {@link #LOGGING_GROUP} key. If a value was already present for this key,
     * it will be overwritten.
     *
     * @param groupName the non-null logical group identifier to associate with
     *                  subsequent log events on the current thread
     */
    public static void set(@NonNull String groupName) {
        MDC.put(LOGGING_GROUP, groupName);
    }

    /**
     * Removes the logging group from the current thread's MDC. It is safe to
     * call this method even if no logging group was previously set.
     */
    public static void clear() {
        MDC.remove(LOGGING_GROUP);
    }

}
