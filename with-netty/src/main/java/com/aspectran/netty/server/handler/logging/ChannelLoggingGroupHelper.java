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
package com.aspectran.netty.server.handler.logging;

import com.aspectran.utils.logging.LoggingGroupHelper;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A helper class for managing the logging group within the scope of a Netty {@link Channel}.
 * <p>Stores the active logging group in a channel attribute and synchronizes it with
 * the current thread's SLF4J MDC via {@link LoggingGroupHelper}.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public final class ChannelLoggingGroupHelper {

    /**
     * Netty channel attribute key for storing the logging group name.
     */
    public static final AttributeKey<String> LOGGING_GROUP_KEY =
            AttributeKey.valueOf(LoggingGroupHelper.LOGGING_GROUP);

    private ChannelLoggingGroupHelper() {
    }

    /**
     * Sets the logging group on the current thread and attaches the group name to the channel.
     * @param channel the Netty channel
     * @param groupName the logging group name to set, or {@code null} to clear it
     */
    public static void setTo(@NonNull Channel channel, @Nullable String groupName) {
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
            channel.attr(LOGGING_GROUP_KEY).set(groupName);
        } else {
            LoggingGroupHelper.clear();
            channel.attr(LOGGING_GROUP_KEY).set(null);
        }
    }

    /**
     * Retrieves the logging group name from the channel attribute and sets it on the current thread.
     * @param channel the Netty channel
     */
    public static void setFrom(@NonNull Channel channel) {
        String groupName = channel.attr(LOGGING_GROUP_KEY).get();
        if (groupName != null) {
            LoggingGroupHelper.set(groupName);
        } else {
            LoggingGroupHelper.clear();
        }
    }

    /**
     * Returns the logging group name attached to the channel.
     * @param channel the Netty channel
     * @return the logging group name, or {@code null} if not set
     */
    @Nullable
    public static String get(@NonNull Channel channel) {
        return channel.attr(LOGGING_GROUP_KEY).get();
    }

}
