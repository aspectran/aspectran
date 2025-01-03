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
package com.aspectran.undertow.server.servlet;

import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionListener;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializer for WebSocket Support in Undertow.
 */
public class TowWebSocketInitializer  {

    private static final String WEBSOCKET_CURRENT_CONNECTIONS_ATTR = "io.undertow.websocket.current-connections";

    private boolean directBuffers = false;

    private int bufferSize = 1024;

    private int maximumPoolSize = -1;

    private int threadLocalCacheSize = 12;

    public void setDirectBuffers(boolean directBuffers) {
        this.directBuffers = directBuffers;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public void setThreadLocalCacheSize(int threadLocalCacheSize) {
        this.threadLocalCacheSize = threadLocalCacheSize;
    }

    public void initialize(@NonNull DeploymentInfo deploymentInfo) {
        if (!deploymentInfo.getServletContextAttributes().containsKey(WebSocketDeploymentInfo.ATTRIBUTE_NAME)) {
            ByteBufferPool byteBufferPool = new DefaultByteBufferPool(directBuffers, bufferSize, maximumPoolSize, threadLocalCacheSize);
            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo().setBuffers(byteBufferPool);
            deploymentInfo.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSocketDeploymentInfo);
            deploymentInfo.addSessionListener(new WebSocketGracefulCloseListener());
        }
    }

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

        private void closeWebSockets(@NonNull String name, @NonNull Object value) {
            if (WEBSOCKET_CURRENT_CONNECTIONS_ATTR.equals(name)) {
                @SuppressWarnings("unchecked")
                List<WebSocketChannel> connections = (List<WebSocketChannel>)value;
                if (!connections.isEmpty()) {
                    CloseMessage closeMessage = new CloseMessage(CloseMessage.MSG_VIOLATES_POLICY, null);
                    for (WebSocketChannel webSocketChannel : new ArrayList<>(connections)) {
                        if (webSocketChannel != null) {
                            WebSockets.sendClose(closeMessage, webSocketChannel, null);
                        }
                    }
                }
            }
        }

    }

}
