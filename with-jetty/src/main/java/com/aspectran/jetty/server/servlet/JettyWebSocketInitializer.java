/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.jetty.server.servlet;

import jakarta.websocket.server.ServerContainer;

/**
 * The Class JettyWebSocketInitializer.
 *
 * <p>Created: 2021/02/26</p>
 *
 * @since 7.0.0
 */
public class JettyWebSocketInitializer {

    private Long idleTimeout;

    private Long asyncSendTimeout;

    private Integer maxBinaryMessageSize;

    private Integer maxTextMessageSize;

    public Long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Long getAsyncSendTimeout() {
        return asyncSendTimeout;
    }

    public void setAsyncSendTimeout(long asyncSendTimeout) {
        this.asyncSendTimeout = asyncSendTimeout;
    }

    public Integer getMaxBinaryMessageSize() {
        return maxBinaryMessageSize;
    }

    public void setMaxBinaryMessageSize(int maxBinaryMessageSize) {
        this.maxBinaryMessageSize = maxBinaryMessageSize;
    }

    public Integer getMaxTextMessageSize() {
        return maxTextMessageSize;
    }

    public void setMaxTextMessageSize(int maxTextMessageSize) {
        this.maxTextMessageSize = maxTextMessageSize;
    }

    public void customize(ServerContainer serverContainer) {
        if (idleTimeout != null) {
            serverContainer.setDefaultMaxSessionIdleTimeout(idleTimeout);
        }
        if (asyncSendTimeout != null) {
            serverContainer.setAsyncSendTimeout(asyncSendTimeout);
        }
        if (maxBinaryMessageSize != null) {
            serverContainer.setDefaultMaxBinaryMessageBufferSize(maxBinaryMessageSize);
        }
        if (maxTextMessageSize != null) {
            serverContainer.setDefaultMaxTextMessageBufferSize(maxTextMessageSize);
        }
    }

}
