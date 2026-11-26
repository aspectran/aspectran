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
package com.aspectran.netty.server.session;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A helper class for managing the session ID within the scope of a Netty {@link Channel}.
 * <p>Stores the active session ID in a channel attribute for access by downstream handlers
 * (such as access logging, metrics, or security checks).</p>
 *
 * <p>Created: 2026-11-26</p>
 */
public final class ChannelSessionHelper {

    /**
     * Netty channel attribute key for storing the session ID.
     */
    public static final AttributeKey<String> SESSION_ID_KEY =
            AttributeKey.valueOf("netty.session.id");

    private ChannelSessionHelper() {
    }

    /**
     * Sets the session ID on the specified channel.
     * @param channel the Netty channel
     * @param sessionId the session ID to set, or {@code null} to clear it
     */
    public static void setSessionId(@NonNull Channel channel, @Nullable String sessionId) {
        channel.attr(SESSION_ID_KEY).set(sessionId);
    }

    /**
     * Retrieves the session ID from the specified channel.
     * @param channel the Netty channel
     * @return the session ID, or {@code null} if not set
     */
    @Nullable
    public static String getSessionId(@NonNull Channel channel) {
        return channel.attr(SESSION_ID_KEY).get();
    }

    /**
     * Retrieves and atomically removes the session ID from the specified channel.
     * @param channel the Netty channel
     * @return the session ID, or {@code null} if not set
     */
    @Nullable
    public static String getAndRemoveSessionId(@NonNull Channel channel) {
        return channel.attr(SESSION_ID_KEY).getAndSet(null);
    }

    /**
     * Clears the session ID from the specified channel.
     * @param channel the Netty channel
     */
    public static void clearSessionId(@NonNull Channel channel) {
        channel.attr(SESSION_ID_KEY).set(null);
    }

}
