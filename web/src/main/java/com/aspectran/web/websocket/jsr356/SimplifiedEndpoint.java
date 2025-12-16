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
package com.aspectran.web.websocket.jsr356;

import com.aspectran.utils.Assert;
import jakarta.websocket.Session;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

/**
 * A simplified abstract WebSocket endpoint that manages a thread-safe collection of
 * authorized sessions and provides convenient methods for broadcasting messages.
 * <p>This class is ideal for typical WebSocket use cases where messages need to be
 * sent to multiple clients.
 * </p>
 *
 * <p>Created: 2025-03-24</p>
 */
public abstract class SimplifiedEndpoint extends AbstractEndpoint {

    /** A thread-safe collection of authorized sessions */
    private final Set<Session> sessions = new CopyOnWriteArraySet<>();

    /**
     * Adds a session to the collection of authorized sessions.
     * @param session the session to add
     * @return {@code true} if the session was added, {@code false} otherwise
     */
    protected boolean addSession(@NonNull Session session) {
        synchronized (sessions) {
            return (session.isOpen() && sessions.add(session));
        }
    }

    @Override
    protected void removeSession(Session session) {
        synchronized (sessions) {
            if (sessions.remove(session)) {
                onSessionRemoved(session);
            }
        }
    }

    /**
     * A hook method called when a session is removed.
     * @param session the session that was removed
     */
    protected abstract void onSessionRemoved(Session session);

    /**
     * Returns whether a session matching the given predicate exists in the authorized sessions.
     * @param predicate the predicate to apply to each session
     * @return {@code true} if a matching session is found, {@code false} otherwise
     */
    public boolean containsSession(Predicate<Session> predicate) {
        Assert.notNull(predicate, "predicate must not be null");
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen() && predicate.test(session)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the number of currently authorized sessions.
     * @return the session count
     */
    public int countSessions() {
        return sessions.size();
    }

    /**
     * Sends a message to all authorized sessions.
     * @param message the text message to send
     */
    public void broadcast(String message) {
        for (Session session : sessions) {
            sendText(session, message);
        }
    }

    /**
     * Sends a message to all authorized sessions except for the one to be skipped.
     * @param message the text message to send
     * @param sessionToSkip the session to exclude from the broadcast
     */
    public void broadcast(String message, Session sessionToSkip) {
        for (Session session : sessions) {
            if (session != sessionToSkip) {
                sendText(session, message);
            }
        }
    }

    /**
     * Sends a text message to the given session asynchronously.
     * @param session the session to send the message to
     * @param text the text message to send
     */
    public void sendText(Session session, String text) {
        Assert.notNull(session, "session must not be null");
        if (session.isOpen()) {
            session.getAsyncRemote().sendText(text);
        }
    }

}
