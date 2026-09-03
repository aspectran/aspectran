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

    public int getMaxFramePayloadLength() {
        return maxFramePayloadLength;
    }

    public void setMaxFramePayloadLength(int maxFramePayloadLength) {
        if (maxFramePayloadLength <= 0) {
            throw new IllegalArgumentException("maxFramePayloadLength must be positive");
        }
        this.maxFramePayloadLength = maxFramePayloadLength;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        if (maxMessageSize <= 0) {
            throw new IllegalArgumentException("maxMessageSize must be positive");
        }
        this.maxMessageSize = maxMessageSize;
    }

    public boolean isAllowExtensions() {
        return allowExtensions;
    }

    public void setAllowExtensions(boolean allowExtensions) {
        this.allowExtensions = allowExtensions;
    }

    public String getSubprotocols() {
        return subprotocols;
    }

    public void setSubprotocols(String subprotocols) {
        this.subprotocols = subprotocols;
    }

    public long getMaxIdleTimeout() {
        return maxIdleTimeout;
    }

    public void setMaxIdleTimeout(long maxIdleTimeout) {
        this.maxIdleTimeout = Math.max(0, maxIdleTimeout);
    }

    public int getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    public void setMaxTextMessageBufferSize(int maxTextMessageBufferSize) {
        if (maxTextMessageBufferSize <= 0) {
            throw new IllegalArgumentException("maxTextMessageBufferSize must be positive");
        }
        this.maxTextMessageBufferSize = maxTextMessageBufferSize;
    }

    public int getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    public void setMaxBinaryMessageBufferSize(int maxBinaryMessageBufferSize) {
        if (maxBinaryMessageBufferSize <= 0) {
            throw new IllegalArgumentException("maxBinaryMessageBufferSize must be positive");
        }
        this.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    }

}
