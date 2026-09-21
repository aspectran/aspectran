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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A simplified abstract WebSocket endpoint that manages a thread-safe collection of
 * authorized sessions and provides convenient methods for broadcasting messages
 * both synchronously and asynchronously with guaranteed in-order delivery.
 * <p>This class is ideal for typical WebSocket use cases where messages need to be
 * sent to multiple clients.
 * </p>
 *
 * <p>Created: 2025-03-24</p>
 */
public abstract class SimplifiedEndpoint extends AbstractEndpoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** A thread-safe collection of authorized sessions */
    private final Set<Session> sessions = new CopyOnWriteArraySet<>();

    /** Per-session message queue for serialized asynchronous sending */
    private final ConcurrentMap<String, ConcurrentLinkedQueue<String>> messageQueues = new ConcurrentHashMap<>();

    /** Per-session flag indicating if an asynchronous send operation is currently in progress */
    private final ConcurrentMap<String, AtomicBoolean> sendingFlags = new ConcurrentHashMap<>();

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
                messageQueues.remove(session.getId());
                sendingFlags.remove(session.getId());
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
     * Finds a session with the given session id.
     * @param sessionId the session id to search for
     * @return the session if found, {@code null} if no session was found
     */
    protected Session findSession(String sessionId) {
        Assert.notNull(sessionId, "sessionId must not be null");
        synchronized (sessions) {
            for (Session session : sessions) {
                if (sessionId.equals(session.getId())) {
                    return session;
                }
            }
        }
        return null;
    }

    /**
     * Performs the given action for each authorized session.
     * @param action the action to be performed for each session
     */
    protected void forEachSession(Consumer<Session> action) {
        Assert.notNull(action, "action must not be null");
        for (Session session : sessions) {
            action.accept(session);
        }
    }

    /**
     * Returns the number of currently authorized sessions.
     * @return the session count
     */
    public int countSessions() {
        return sessions.size();
    }

    /**
     * Sends a message to all authorized sessions synchronously.
     * @param message the text message to send
     */
    public void broadcast(String message) {
        for (Session session : sessions) {
            sendText(session, message);
        }
    }

    /**
     * Sends a message to all authorized sessions except for the one to be skipped synchronously.
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
     * Sends a message to authorized sessions that match the given predicate synchronously.
     * @param message the text message to send
     * @param predicate the predicate to apply to each session
     */
    public void broadcast(String message, Predicate<Session> predicate) {
        Assert.notNull(predicate, "predicate must not be null");
        for (Session session : sessions) {
            if (session.isOpen() && predicate.test(session)) {
                sendText(session, message);
            }
        }
    }

    /**
     * Sends multiple messages to all authorized sessions synchronously.
     * @param messages the text messages to send
     */
    public void broadcast(Iterable<String> messages) {
        for (Session session : sessions) {
            sendText(session, messages);
        }
    }

    /**
     * Sends multiple messages to all authorized sessions except for the one to be skipped synchronously.
     * @param messages the text messages to send
     * @param sessionToSkip the session to exclude from the broadcast
     */
    public void broadcast(Iterable<String> messages, Session sessionToSkip) {
        for (Session session : sessions) {
            if (session != sessionToSkip) {
                sendText(session, messages);
            }
        }
    }

    /**
     * Sends multiple messages to authorized sessions that match the given predicate synchronously.
     * @param messages the text messages to send
     * @param predicate the predicate to apply to each session
     */
    public void broadcast(Iterable<String> messages, Predicate<Session> predicate) {
        Assert.notNull(predicate, "predicate must not be null");
        for (Session session : sessions) {
            if (session.isOpen() && predicate.test(session)) {
                sendText(session, messages);
            }
        }
    }

    /**
     * Sends a message to all authorized sessions asynchronously with guaranteed FIFO ordering.
     * @param message the text message to send
     */
    public void broadcastAsync(String message) {
        for (Session session : sessions) {
            sendTextAsync(session, message);
        }
    }

    /**
     * Sends a message to all authorized sessions except for the one to be skipped asynchronously.
     * @param message the text message to send
     * @param sessionToSkip the session to exclude from the broadcast
     */
    public void broadcastAsync(String message, Session sessionToSkip) {
        for (Session session : sessions) {
            if (session != sessionToSkip) {
                sendTextAsync(session, message);
            }
        }
    }

    /**
     * Sends a message to authorized sessions that match the given predicate asynchronously.
     * @param message the text message to send
     * @param predicate the predicate to apply to each session
     */
    public void broadcastAsync(String message, Predicate<Session> predicate) {
        Assert.notNull(predicate, "predicate must not be null");
        for (Session session : sessions) {
            if (session.isOpen() && predicate.test(session)) {
                sendTextAsync(session, message);
            }
        }
    }

    /**
     * Sends multiple messages to all authorized sessions asynchronously with guaranteed FIFO ordering.
     * @param messages the text messages to send
     */
    public void broadcastAsync(Collection<String> messages) {
        for (Session session : sessions) {
            sendTextAsync(session, messages);
        }
    }

    /**
     * Sends multiple messages to all authorized sessions except for the one to be skipped asynchronously.
     * @param messages the text messages to send
     * @param sessionToSkip the session to exclude from the broadcast
     */
    public void broadcastAsync(Collection<String> messages, Session sessionToSkip) {
        for (Session session : sessions) {
            if (session != sessionToSkip) {
                sendTextAsync(session, messages);
            }
        }
    }

    /**
     * Sends multiple messages to authorized sessions that match the given predicate asynchronously.
     * @param messages the text messages to send
     * @param predicate the predicate to apply to each session
     */
    public void broadcastAsync(Collection<String> messages, Predicate<Session> predicate) {
        Assert.notNull(predicate, "predicate must not be null");
        for (Session session : sessions) {
            if (session.isOpen() && predicate.test(session)) {
                sendTextAsync(session, messages);
            }
        }
    }

    /**
     * Sends a text message to the given session synchronously.
     * The sending is synchronized on the session to prevent concurrent writes.
     * @param session the session to send the message to
     * @param text the text message to send
     */
    public void sendText(Session session, String text) {
        Assert.notNull(session, "session must not be null");
        if (session.isOpen()) {
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(text);
                    }
                }
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to send text synchronously to session {}", session.getId(), e);
                }
            }
        }
    }

    /**
     * Sends multiple text messages to the given session synchronously.
     * The sending is synchronized on the session to ensure all messages are sent sequentially
     * in a single atomic lock, preventing interleaving from other threads.
     * @param session the session to send the messages to
     * @param texts the text messages to send
     */
    public void sendText(Session session, @NonNull Iterable<String> texts) {
        Assert.notNull(session, "session must not be null");
        Assert.notNull(texts, "texts must not be null");
        if (session.isOpen()) {
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        for (String text : texts) {
                            if (text != null && session.isOpen()) {
                                session.getBasicRemote().sendText(text);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to send texts synchronously to session {}", session.getId(), e);
                }
            }
        }
    }

    /**
     * Sends a text message to the given session asynchronously with guaranteed FIFO ordering.
     * Messages are queued per session and drained sequentially via {@link jakarta.websocket.SendHandler}.
     * @param session the session to send the message to
     * @param text the text message to send
     */
    public void sendTextAsync(Session session, String text) {
        Assert.notNull(session, "session must not be null");
        if (!session.isOpen()) {
            return;
        }
        String sessionId = session.getId();
        ConcurrentLinkedQueue<String> queue = messageQueues.computeIfAbsent(sessionId, k -> new ConcurrentLinkedQueue<>());
        queue.offer(text);
        drainQueue(session, queue);
    }

    /**
     * Sends multiple text messages to the given session asynchronously with guaranteed FIFO ordering.
     * Messages are queued per session and drained sequentially via {@link jakarta.websocket.SendHandler}.
     * @param session the session to send the messages to
     * @param texts the collection of text messages to send
     */
    public void sendTextAsync(Session session, @NonNull Collection<String> texts) {
        Assert.notNull(session, "session must not be null");
        Assert.notNull(texts, "texts must not be null");
        if (!session.isOpen() || texts.isEmpty()) {
            return;
        }
        String sessionId = session.getId();
        ConcurrentLinkedQueue<String> queue = messageQueues.computeIfAbsent(sessionId, k -> new ConcurrentLinkedQueue<>());
        queue.addAll(texts);
        drainQueue(session, queue);
    }

    private void drainQueue(@NonNull Session session, ConcurrentLinkedQueue<String> queue) {
        String sessionId = session.getId();
        AtomicBoolean isSending = sendingFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false));
        if (isSending.compareAndSet(false, true)) {
            String message = queue.poll();
            if (message != null && session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(message, result -> {
                        isSending.set(false);
                        if (result.isOK()) {
                            if (!queue.isEmpty() && session.isOpen()) {
                                drainQueue(session, queue);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Failed to send text asynchronously to session {}", sessionId, result.getException());
                            }
                            queue.clear();
                        }
                    });
                } catch (Exception e) {
                    isSending.set(false);
                    queue.clear();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to initiate asynchronous text send to session {}", sessionId, e);
                    }
                }
            } else {
                isSending.set(false);
            }
        }
    }

}
