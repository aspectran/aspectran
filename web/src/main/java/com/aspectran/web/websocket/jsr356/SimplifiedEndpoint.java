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
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * <p>Created: 2025-03-24</p>
 */
@AvoidAdvice
public abstract class SimplifiedEndpoint extends AbstractEndpoint{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(@NonNull Session session) throws IOException {
        setLoggingGroup();
        checkAuthorized(session);
    }

    protected void checkAuthorized(Session session) throws IOException {
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        setLoggingGroup();
        processMessage(session, message);
    }

    @OnMessage
    public void onMessage(Session session, ByteBuffer message) {
        setLoggingGroup();
        processMessage(session, message);
    }

    protected void processMessage(Session session, String message) {
    }

    protected void processMessage(Session session, ByteBuffer message) {
    }

    @OnClose
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

    protected boolean addSession(@NonNull Session session) {
        synchronized (sessions) {
            return (session.isOpen() && sessions.add(session));
        }
    }

    protected void removeSession(Session session) {
        synchronized (sessions) {
            if (sessions.remove(session)) {
                onSessionRemoved(session);
            }
        }
    }

    protected abstract void onSessionRemoved(Session session);

    protected boolean existsSession(Predicate<Session> predicate) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (predicate.test(session)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void broadcast(String message) {
        for (Session session : sessions) {
            sendText(session, message);
        }
    }

    protected void sendText(@NonNull Session session, String text) {
        if (session.isOpen()) {
            session.getAsyncRemote().sendText(text);
        }
    }

}
