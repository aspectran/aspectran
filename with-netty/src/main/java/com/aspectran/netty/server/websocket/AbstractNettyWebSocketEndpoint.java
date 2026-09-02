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

import com.aspectran.core.activity.InstantActivitySupport;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Convenient abstract base class for Netty-based WebSocket endpoints.
 * <p>Automatically tracks active sessions, provides broadcasting utilities,
 * and integrates with Aspectran via {@link InstantActivitySupport}.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public abstract class AbstractNettyWebSocketEndpoint
        extends InstantActivitySupport implements NettyWebSocketListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<NettyWebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    /**
     * Returns an unmodifiable set of all currently open WebSocket sessions.
     * @return the active sessions
     */
    @NonNull
    public Set<NettyWebSocketSession> getOpenSessions() {
        return Collections.unmodifiableSet(sessions);
    }

    /**
     * Returns the count of currently connected sessions.
     * @return the session count
     */
    public int getSessionCount() {
        return sessions.size();
    }

    @Override
    public void onOpen(@NonNull NettyWebSocketSession session) throws Exception {
        sessions.add(session);
        if (logger.isDebugEnabled()) {
            logger.debug("WebSocket session opened: {} (total: {})", session.getId(), sessions.size());
        }
    }

    @Override
    public void onClose(@NonNull NettyWebSocketSession session, int statusCode, @Nullable String reason) throws Exception {
        sessions.remove(session);
        if (logger.isDebugEnabled()) {
            logger.debug("WebSocket session closed: {} (code: {}, reason: {})", session.getId(), statusCode, reason);
        }
    }

    @Override
    public void onError(@NonNull NettyWebSocketSession session, @NonNull Throwable cause) {
        logger.warn("WebSocket error in session: {}", session.getId(), cause);
    }

    /**
     * Broadcasts a text message to all connected sessions.
     * @param text the message text
     */
    public void broadcast(String text) {
        broadcast(text, null);
    }

    /**
     * Broadcasts a text message to connected sessions matching the given filter.
     * @param text the message text
     * @param filter optional predicate to select recipient sessions
     */
    public void broadcast(String text, @Nullable Predicate<NettyWebSocketSession> filter) {
        for (NettyWebSocketSession session : sessions) {
            if (session.isOpen() && (filter == null || filter.test(session))) {
                session.sendText(text);
            }
        }
    }

    /**
     * Broadcasts binary data to all connected sessions.
     * @param data the binary payload
     */
    public void broadcast(byte[] data) {
        broadcast(data, null);
    }

    /**
     * Broadcasts binary data to connected sessions matching the given filter.
     * @param data the binary payload
     * @param filter optional predicate to select recipient sessions
     */
    public void broadcast(byte[] data, @Nullable Predicate<NettyWebSocketSession> filter) {
        for (NettyWebSocketSession session : sessions) {
            if (session.isOpen() && (filter == null || filter.test(session))) {
                session.sendBinary(data);
            }
        }
    }

}
