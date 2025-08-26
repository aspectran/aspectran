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

import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Initializer for WebSocket Support in Undertow.
 */
public class TowWebSocketServerContainerInitializer {

    private static final String WEBSOCKET_CURRENT_CONNECTIONS_ATTR = "io.undertow.websocket.current-connections";

    private boolean directBuffers = false;

    private int bufferSize = 1024;

    private int maximumPoolSize = -1;

    private int threadLocalCacheSize = 12;

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

    /**
     * Initializes the web socket server container.
     * @param towServletContext the servlet context
     */
    public void initialize(@NonNull TowServletContext towServletContext) {
        if (!towServletContext.getServletContextAttributes().containsKey(WebSocketDeploymentInfo.ATTRIBUTE_NAME)) {
            ByteBufferPool byteBufferPool = new DefaultByteBufferPool(directBuffers, bufferSize, maximumPoolSize, threadLocalCacheSize);
            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo().setBuffers(byteBufferPool);
            towServletContext.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSocketDeploymentInfo);
            towServletContext.addSessionListener(new WebSocketGracefulCloseListener());
        }
    }

    /**
     * Destroys the web socket server container.
     * @param deployment the deployment
     */
    @SuppressWarnings("unchecked")
    public static void destroy(@NonNull Deployment deployment) {
        SessionManager sessionManager = deployment.getSessionManager();
        if (sessionManager != null) {
            Set<String> activeSessions = sessionManager.getActiveSessions();
            if (!activeSessions.isEmpty()) {
                activeSessions.forEach(sessionId -> {
                    Session session = sessionManager.getSession(sessionId);
                    if (session != null) {
                        Object value = session.getAttribute(WEBSOCKET_CURRENT_CONNECTIONS_ATTR);
                        if (value != null) {
                            closeWebSockets((List<WebSocketChannel>) value);
                            session.removeAttribute(WEBSOCKET_CURRENT_CONNECTIONS_ATTR);
                        }
                    }
                });
            }
        }
    }

    private static void closeWebSockets(List<WebSocketChannel> connections) {
        if (connections != null && !connections.isEmpty()) {
            CloseMessage closeMessage = new CloseMessage(CloseMessage.MSG_VIOLATES_POLICY, null);
            for (WebSocketChannel webSocketChannel : new ArrayList<>(connections)) {
                if (webSocketChannel != null && webSocketChannel.isOpen()) {
                    WebSockets.sendClose(closeMessage, webSocketChannel, null);
                }
            }
        }
    }

    /**
     * A {@link SessionListener} that closes WebSocket connections when a session is destroyed.
     */
    public static class WebSocketGracefulCloseListener implements SessionListener {

        @Override
        public void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
            if (oldValue != null && oldValue != newValue) {
                closeWebSockets(name, oldValue);
            }
        }

        @Override
        public void attributeRemoved(Session session, String name, Object oldValue) {
            if (oldValue != null) {
                closeWebSockets(name, oldValue);
            }
        }

        @SuppressWarnings("unchecked")
        private void closeWebSockets(@NonNull String name, @NonNull Object value) {
            if (WEBSOCKET_CURRENT_CONNECTIONS_ATTR.equals(name)) {
                List<WebSocketChannel> connections = (List<WebSocketChannel>)value;
                TowWebSocketServerContainerInitializer.closeWebSockets(connections);
            }
        }

    }

}
