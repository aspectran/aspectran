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
package com.aspectran.netty.server.websocket;

import com.aspectran.netty.server.NettyContext;
import org.jspecify.annotations.NonNull;

/**
 * Initializer for WebSocket support in a {@link NettyContext}.
 *
 * <p>Created: 2026-09-04</p>
 */
public class NettyWebSocketServerContainerInitializer extends NettyWebSocketConfig {

    /**
     * Returns the maximum idle timeout in milliseconds.
     * @return the idle timeout in milliseconds
     */
    public Long getIdleTimeout() {
        return getMaxIdleTimeout();
    }

    /**
     * Sets the maximum idle timeout in milliseconds.
     * Alias for {@link #setMaxIdleTimeout(long)}.
     * @param idleTimeout the idle timeout in milliseconds
     */
    public void setIdleTimeout(long idleTimeout) {
        setMaxIdleTimeout(idleTimeout);
    }

    /**
     * Sets both text and binary message buffer sizes.
     * @param bufferSize the buffer size in bytes
     */
    public void setBufferSize(int bufferSize) {
        setMaxTextMessageBufferSize(bufferSize);
        setMaxBinaryMessageBufferSize(bufferSize);
    }

    /**
     * Initializes the WebSocket configuration for the given {@link NettyContext}.
     * @param nettyContext the Netty context to initialize
     */
    public void initialize(@NonNull NettyContext nettyContext) {
        if (nettyContext.getWebSocketConfig() == null) {
            nettyContext.setWebSocketConfig(this);
        }
    }

}
