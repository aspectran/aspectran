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
package com.aspectran.undertow.server.servlet;

import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionListener;
import io.undertow.servlet.api.Deployment;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.jsr.JsrWebSocketFilter;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import jakarta.websocket.server.ServerContainer;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;

/**
 * Initializer for WebSocket Support in Undertow.
 */
public class TowWebSocketServerContainerInitializer {

    private boolean directBuffers = true;

    private int bufferSize = 16384;

    private int maximumPoolSize = -1;

    private int threadLocalCacheSize = 12;

    private Long idleTimeout;

    private Long asyncSendTimeout;

    private Integer maxBinaryMessageSize;

    private Integer maxTextMessageSize;

    /**
     * Sets whether to use direct buffers.
     * @param directBuffers whether to use direct buffers
     */
    public void setDirectBuffers(boolean directBuffers) {
        this.directBuffers = directBuffers;
    }

    /**
     * Sets the buffer size.
     * @param bufferSize the buffer size
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Sets the maximum pool size.
     * @param maximumPoolSize the maximum pool size
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     * Sets the thread local cache size.
     * @param threadLocalCacheSize the thread local cache size
     */
    public void setThreadLocalCacheSize(int threadLocalCacheSize) {
        this.threadLocalCacheSize = threadLocalCacheSize;
    }

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

    /**
     * Initializes the web socket server container.
     * @param towServletContext the servlet context
     */
    public void initialize(@NonNull TowServletContext towServletContext) {
        if (!towServletContext.getServletContextAttributes().containsKey(WebSocketDeploymentInfo.ATTRIBUTE_NAME)) {
            ByteBufferPool byteBufferPool = new DefaultByteBufferPool(directBuffers, bufferSize, maximumPoolSize, threadLocalCacheSize);
            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo().setBuffers(byteBufferPool);
            if (idleTimeout != null || asyncSendTimeout != null || maxBinaryMessageSize != null || maxTextMessageSize != null) {
                webSocketDeploymentInfo.addListener(container -> {
                    if (idleTimeout != null) {
                        container.setDefaultMaxSessionIdleTimeout(idleTimeout);
                    }
                    if (asyncSendTimeout != null) {
                        container.setAsyncSendTimeout(asyncSendTimeout);
                    }
                    if (maxBinaryMessageSize != null) {
                        container.setDefaultMaxBinaryMessageBufferSize(maxBinaryMessageSize);
                    }
                    if (maxTextMessageSize != null) {
                        container.setDefaultMaxTextMessageBufferSize(maxTextMessageSize);
                    }
                });
            }
            towServletContext.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSocketDeploymentInfo);
            towServletContext.addSessionListener(new WebSocketGracefulCloseListener());
        }
    }

    /**
     * Destroys the web socket server container.
     * @param deployment the deployment
     */
    public static void destroy(@NonNull Deployment deployment) {
        Object container = deployment.getServletContext().getAttribute(ServerContainer.class.getName());
        if (container instanceof AutoCloseable) {
            try {
                ((AutoCloseable)container).close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * A {@link SessionListener} that closes WebSocket connections associated with a session
     * when the session is destroyed, or when the WebSocket connections attribute in the session
     * is updated or removed, preventing socket connection leaks.
     */
    public static class WebSocketGracefulCloseListener implements SessionListener {

        @Override
        public void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
            if (oldValue != null && oldValue != newValue) {
                closeWebSockets(oldValue);
            }
        }

        @Override
        public void attributeRemoved(Session session, String name, Object oldValue) {
            if (oldValue != null) {
                closeWebSockets(oldValue);
            }
        }

        private void closeWebSockets(@NonNull Object value) {
            if (value instanceof JsrWebSocketFilter.WebSocketChannels connections) {
                if (!connections.isEmpty()) {
                    CloseMessage closeMessage = new CloseMessage(CloseMessage.MSG_VIOLATES_POLICY, null);
                    for (WebSocketChannel channel : new ArrayList<>(connections)) {
                        if (channel != null && channel.isOpen()) {
                            WebSockets.sendClose(closeMessage, channel, null);
                        }
                    }
                }
            }
        }

    }

}
