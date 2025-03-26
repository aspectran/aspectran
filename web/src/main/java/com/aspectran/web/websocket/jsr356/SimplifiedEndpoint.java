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

import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.websocket.Session;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

/**
 * <p>Created: 2025-03-24</p>
 */
public abstract class SimplifiedEndpoint extends AbstractEndpoint {

    /** A collection of authorized sessions */
    private final Set<Session> sessions = new CopyOnWriteArraySet<>();

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

    protected abstract void onSessionRemoved(Session session);

    /**
     * Returns whether a session matching the given predicate exists in the authorized sessions.
     */
    public boolean containsSession(Predicate<Session> predicate) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (predicate.test(session)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the number of currently authorized sessions.
     */
    public int countSessions() {
        return sessions.size();
    }

    public void broadcast(String message) {
        for (Session session : sessions) {
            sendText(session, message);
        }
    }

    public void sendText(@NonNull Session session, String text) {
        if (session.isOpen()) {
            session.getAsyncRemote().sendText(text);
        }
    }

}
