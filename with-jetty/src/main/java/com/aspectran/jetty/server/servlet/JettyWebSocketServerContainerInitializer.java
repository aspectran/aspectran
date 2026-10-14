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
package com.aspectran.jetty.server.servlet;

import jakarta.websocket.server.ServerContainer;

/**
 * A {@link jakarta.servlet.ServletContainerInitializer} for initializing
 * a Jetty {@link ServerContainer}.
 *
 * <p>Created: 2021/02/26</p>
 *
 * @since 7.0.0
 */
public class JettyWebSocketServerContainerInitializer {

    private Long idleTimeout;

    private Long asyncSendTimeout;

    private Integer maxBinaryMessageSize;

    private Integer maxTextMessageSize;

    /**
     * Returns the idle timeout of this initializer.
     * @return the idle timeout of this initializer
     */
    public Long getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets the idle timeout of this initializer.
     * @param idleTimeout the idle timeout of this initializer
     */
    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * Returns the async send timeout of this initializer.
     * @return the async send timeout of this initializer
     */
    public Long getAsyncSendTimeout() {
        return asyncSendTimeout;
    }

    /**
     * Sets the async send timeout of this initializer.
     * @param asyncSendTimeout the async send timeout of this initializer
     */
    public void setAsyncSendTimeout(long asyncSendTimeout) {
        this.asyncSendTimeout = asyncSendTimeout;
    }

    /**
     * Returns the maximum binary message size of this initializer.
     * @return the maximum binary message size of this initializer
     */
    public Integer getMaxBinaryMessageSize() {
        return maxBinaryMessageSize;
    }

    /**
     * Sets the maximum binary message size of this initializer.
     * @param maxBinaryMessageSize the maximum binary message size of this initializer
     */
    public void setMaxBinaryMessageSize(int maxBinaryMessageSize) {
        this.maxBinaryMessageSize = maxBinaryMessageSize;
    }

    /**
     * Returns the maximum text message size of this initializer.
     * @return the maximum text message size of this initializer
     */
    public Integer getMaxTextMessageSize() {
        return maxTextMessageSize;
    }

    /**
     * Sets the maximum text message size of this initializer.
     * @param maxTextMessageSize the maximum text message size of this initializer
     */
    public void setMaxTextMessageSize(int maxTextMessageSize) {
        this.maxTextMessageSize = maxTextMessageSize;
    }

    /**
     * Customizes the provided {@link ServerContainer} by applying the configuration settings
     * such as idle timeout, async send timeout, maximum binary message size, and
     * maximum text message size if they are not null.
     * @param serverContainer the {@link ServerContainer} to customize
     */
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
