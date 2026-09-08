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

import com.aspectran.core.component.session.NonPersistentValue;
import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionListener;
import com.aspectran.netty.server.NettyContext;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Initializer for WebSocket support in a {@link NettyContext}.
 *
 * <p>Created: 2026-09-04</p>
 */
public class NettyWebSocketServerContainerInitializer extends NettyWebSocketConfig {

    public static final String WEBSOCKET_SESSIONS_ATTRIBUTE =
            NettyWebSocketServerContainerInitializer.class.getName() + ".WebSocketSessions";

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
        nettyContext.ensureWebSocketGracefulCloseListener();
    }

    /**
     * Binds a WebSocket session to an HTTP session, ensuring that the WebSocket connection
     * is tracked and gracefully closed when the HTTP session is invalidated or destroyed.
     * @param httpSession the HTTP session
     * @param webSocketSession the WebSocket session
     */
    public static void bindSession(@NonNull Session httpSession, @NonNull NettyWebSocketSession webSocketSession) {
        WebSocketSessions connections;
        synchronized (httpSession) {
            connections = httpSession.getAttribute(WEBSOCKET_SESSIONS_ATTRIBUTE);
            if (connections == null) {
                connections = new WebSocketSessions();
                httpSession.setAttribute(WEBSOCKET_SESSIONS_ATTRIBUTE, NonPersistentValue.wrap(connections));
            }
        }
        WebSocketSessions sessions = connections;
        sessions.add(webSocketSession);
        webSocketSession.getChannel().closeFuture().addListener(future -> sessions.remove(webSocketSession));
    }

    /**
     * Set of {@link NettyWebSocketSession}s associated with an HTTP session.
     */
    public static class WebSocketSessions extends CopyOnWriteArraySet<NettyWebSocketSession> {

        @Serial
        private static final long serialVersionUID = 2121556475462496509L;

    }

    /**
     * A {@link SessionListener} that closes WebSocket connections associated with an HTTP session
     * when the session is destroyed, or when the WebSocket connections attribute in the session
     * is updated or removed, preventing socket connection leaks.
     */
    public static class WebSocketGracefulCloseListener implements SessionListener {

        @Override
        public void sessionDestroyed(@NonNull Session session) {
            Object value = session.getAttribute(WEBSOCKET_SESSIONS_ATTRIBUTE);
            if (value != null) {
                closeWebSockets(value);
            }
        }

        @Override
        public void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
            if (WEBSOCKET_SESSIONS_ATTRIBUTE.equals(name) && oldValue != null && oldValue != newValue) {
                closeWebSockets(oldValue);
            }
        }

        @Override
        public void attributeRemoved(Session session, String name, Object oldValue) {
            if (WEBSOCKET_SESSIONS_ATTRIBUTE.equals(name) && oldValue != null) {
                closeWebSockets(oldValue);
            }
        }

        private void closeWebSockets(@NonNull Object value) {
            if (value instanceof WebSocketSessions connections) {
                if (!connections.isEmpty()) {
                    for (NettyWebSocketSession session : new ArrayList<>(connections)) {
                        if (session != null && session.isOpen()) {
                            session.close(NettyWebSocketSession.POLICY_VIOLATION, null);
                        }
                    }
                    connections.clear();
                }
            }
        }

    }

}
