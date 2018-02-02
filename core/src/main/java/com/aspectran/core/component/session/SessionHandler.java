/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.component.session;

import com.aspectran.core.component.Component;
import com.aspectran.core.util.thread.Scheduler;

import java.util.EventListener;

/**
 * The session handler is responsible for session start, session exists, session write,
 * time to live and session destroy.
 *
 * <p>Created: 2017. 6. 12.</p>
 */
public interface SessionHandler extends Component {

    SessionIdGenerator getSessionIdGenerator();

    SessionCache getSessionCache();

    Scheduler getScheduler();

    int getDefaultMaxIdleSecs();

    void setDefaultMaxIdleSecs(int defaultMaxIdleSecs);

    /**
     * Called by the {@link SessionHandler} when a session is first accessed by a request.
     *
     * @param session the session object
     * @see #complete(Session)
     */
    void access(Session session);

    /**
     * Called by the {@link SessionHandler} when a session is last accessed by a request.
     *
     * @param session the session object
     * @see #access(Session)
     */
    void complete(Session session);

    /**
     * Creates a new {@link Session}.
     *
     * @param id the session id
     * @return the new session object
     */
    Session newSession(String id);

    /**
     * Get a known existing session.
     *
     * @param id the session id
     * @return a Session or null if none exists
     */
    Session getSession(String id);

    /**
     * Called when a session has expired.
     *
     * @param id the id to invalidate
     */
    void invalidate(String id);

    String newSessionId(long seedTerm);

    /**
     * Adds an event listener for session-related events.
     *
     * @param listener the session event listener
     * @see #removeEventListener(EventListener)
     */
    void addEventListener(EventListener listener);

    /**
     * Removes an event listener for for session-related events.
     *
     * @param listener the session event listener to remove
     * @see #addEventListener(EventListener)
     */
    void removeEventListener(EventListener listener);

    /**
     * Removes all event listeners for session-related events.
     *
     * @see #removeEventListener(EventListener)
     */
    void clearEventListeners();

    /**
     * Call binding and attribute listeners based on the new and old
     * values of the attribute.
     *
     * @param session the basic session
     * @param name name of the attribute
     * @param oldValue previous value of the attribute
     * @param newValue  new value of the attribute
     */
    void sessionAttributeChanged(Session session, String name, Object oldValue, Object newValue);

    /**
     * Call the activation listeners.
     * This must be called holding the lock.
     *
     * @param session the session
     */
    void didActivate(Session session);

    /**
     * Call the passivation listeners.
     * This must be called holding the lock.
     *
     * @param session the session
     */
    void willPassivate(Session session);

    /**
     * Returns the maximum time the session is valid.
     *
     * @return the maximum amount of time session remained valid
     */
    long getSessionTimeMax();

    /**
     * Returns the total amount of time all sessions remained valid.
     *
     * @return the total amount of time all sessions remained valid
     */
    long getSessionTimeTotal();

    /**
     * Returns the mean amount of time session remained valid.
     *
     * @return the mean amount of time session remained valid
     */
    double getSessionTimeMean();

    /**
     * Returns the standard deviation of amount of time session remained valid.
     *
     * @return the standard deviation of amount of time session remained valid
     */
    double getSessionTimeStdDev();

    /**
     * Returns the total number of sessions created by this manager.
     *
     * @return the total number of sessions created by this manager
     */
    int getSessionsCreated();

    /**
     * Resets the session usage statistics.
     */
    void statsReset();

}
