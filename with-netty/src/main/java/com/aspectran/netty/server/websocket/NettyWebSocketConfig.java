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

/**
 * Configuration for WebSocket handling in Netty server.
 *
 * <p>Created: 2026-09-03</p>
 */
public class NettyWebSocketConfig {

    private int maxFramePayloadLength = 65536;

    private int maxMessageSize = 65536;

    private boolean allowExtensions = true;

    private String subprotocols;

    private long maxIdleTimeout;

    private int maxTextMessageBufferSize = 65536;

    private int maxBinaryMessageBufferSize = 65536;

    /**
     * Returns the maximum payload length of a WebSocket frame.
     * @return the maximum frame payload length in bytes
     */
    public int getMaxFramePayloadLength() {
        return maxFramePayloadLength;
    }

    /**
     * Sets the maximum payload length of a WebSocket frame.
     * @param maxFramePayloadLength the maximum frame payload length in bytes
     */
    public void setMaxFramePayloadLength(int maxFramePayloadLength) {
        if (maxFramePayloadLength <= 0) {
            throw new IllegalArgumentException("maxFramePayloadLength must be positive");
        }
        this.maxFramePayloadLength = maxFramePayloadLength;
    }

    /**
     * Returns the maximum aggregated WebSocket message size.
     * @return the maximum message size in bytes
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * Sets the maximum aggregated WebSocket message size.
     * @param maxMessageSize the maximum message size in bytes
     */
    public void setMaxMessageSize(int maxMessageSize) {
        if (maxMessageSize <= 0) {
            throw new IllegalArgumentException("maxMessageSize must be positive");
        }
        this.maxMessageSize = maxMessageSize;
    }

    /**
     * Returns whether WebSocket extensions (such as per-message-deflate) are allowed.
     * @return {@code true} if extensions are allowed; {@code false} otherwise
     */
    public boolean isAllowExtensions() {
        return allowExtensions;
    }

    /**
     * Sets whether WebSocket extensions are allowed.
     * @param allowExtensions {@code true} to allow extensions; {@code false} otherwise
     */
    public void setAllowExtensions(boolean allowExtensions) {
        this.allowExtensions = allowExtensions;
    }

    /**
     * Returns the comma-separated list of supported WebSocket subprotocols.
     * @return the subprotocols string, or {@code null}
     */
    public String getSubprotocols() {
        return subprotocols;
    }

    /**
     * Sets the comma-separated list of supported WebSocket subprotocols.
     * @param subprotocols the subprotocols string
     */
    public void setSubprotocols(String subprotocols) {
        this.subprotocols = subprotocols;
    }

    /**
     * Returns the maximum idle timeout in milliseconds before a WebSocket connection is closed.
     * @return the idle timeout in milliseconds, or 0 if disabled
     */
    public long getMaxIdleTimeout() {
        return maxIdleTimeout;
    }

    /**
     * Sets the maximum idle timeout in milliseconds for WebSocket connections.
     * @param maxIdleTimeout the idle timeout in milliseconds (0 or negative to disable)
     */
    public void setMaxIdleTimeout(long maxIdleTimeout) {
        this.maxIdleTimeout = Math.max(0, maxIdleTimeout);
    }

    /**
     * Returns the maximum buffer size for incoming text messages.
     * @return the maximum text message buffer size in bytes
     */
    public int getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    /**
     * Sets the maximum buffer size for incoming text messages.
     * @param maxTextMessageBufferSize the buffer size in bytes
     */
    public void setMaxTextMessageBufferSize(int maxTextMessageBufferSize) {
        if (maxTextMessageBufferSize <= 0) {
            throw new IllegalArgumentException("maxTextMessageBufferSize must be positive");
        }
        this.maxTextMessageBufferSize = maxTextMessageBufferSize;
    }

    /**
     * Returns the maximum buffer size for incoming binary messages.
     * @return the maximum binary message buffer size in bytes
     */
    public int getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    /**
     * Sets the maximum buffer size for incoming binary messages.
     * @param maxBinaryMessageBufferSize the buffer size in bytes
     */
    public void setMaxBinaryMessageBufferSize(int maxBinaryMessageBufferSize) {
        if (maxBinaryMessageBufferSize <= 0) {
            throw new IllegalArgumentException("maxBinaryMessageBufferSize must be positive");
        }
        this.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    }

}
