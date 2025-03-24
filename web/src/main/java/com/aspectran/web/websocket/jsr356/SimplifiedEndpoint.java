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
package com.aspectran.web.websocket.jsr356;

import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.HandshakeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.ClosedChannelException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeoutException;

import static com.aspectran.web.websocket.jsr356.AspectranConfigurator.HANDSHAKE_REQUEST;

/**
 * <p>Created: 2025-03-24</p>
 */
@AvoidAdvice
public abstract class SimplifiedEndpoint extends AbstractEndpoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<Session> sessions = new CopyOnWriteArraySet<>();

    private SessionListener sessionListener;

    protected void setSessionListener(SessionListener sessionListener) {
        this.sessionListener = sessionListener;
    }

    @OnOpen
    public void onOpen(@NonNull Session session, @NonNull EndpointConfig config) throws IOException {
        setLoggingGroup();
        HandshakeRequest request = (HandshakeRequest)config.getUserProperties().get(HANDSHAKE_REQUEST);
        setRequest(request);
        authenticate(session, config);
    }

    public abstract void authenticate(Session session, EndpointConfig config) throws IOException;

    @OnMessage
    public void onMessage(Session session, String message) {
        setLoggingGroup();
        processMessage(session, message);
    }

    public void onClose(Session session, CloseReason reason) {
        setLoggingGroup();
        if (logger.isDebugEnabled()) {
            logger.debug("Websocket session {} has been closed. Reason: {}", session.getId(), reason);
        }
        removeSession(session);
    }

    @OnError
    public void onError(@NonNull Session session, Throwable error) {
        setLoggingGroup();
        if (!ExceptionUtils.hasCause(error, ClosedChannelException.class, TimeoutException.class)) {
            logger.warn("Error in websocket session: {}", session.getId(), error);
        }
        try {
            removeSession(session);
            session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, null));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public abstract void processMessage(Session session, String message);

    public void addSession(Session session) {
        synchronized (sessions) {
            if (sessions.add(session) && sessionListener != null) {
                sessionListener.onAdded(session);
            }
        }
    }

    public void removeSession(Session session) {
        synchronized (sessions) {
            if (sessions.remove(session) && sessionListener != null) {
                sessionListener.onRemoved(session);
            }
        }
    }

    protected void broadcast(String message) {
        for (Session session : sessions) {
            sendMessage(session, message);
        }
    }

    protected void sendMessage(@NonNull Session session, String message) {
        if (session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }

    protected void setSendTimeout(@NonNull Session session, long timeoutInMillis) {
        session.getAsyncRemote().setSendTimeout(timeoutInMillis);
    }

    protected interface SessionListener {

        void onAdded(Session session);

        void onRemoved(Session session);

    }

}
