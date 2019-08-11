/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
     * Creates a new {@link Session}.
     *
     * @param id the session id
     * @return the new session object
     */
    BasicSession createSession(String id);

    /**
     * Get a known existing session.
     *
     * @param id the session id
     * @return a Session or null if none exists
     */
    BasicSession getSession(String id);

    void saveSession(BasicSession session);

    /**
     * Called when a session has expired.
     *
     * @param id the id to invalidate
     */
    void invalidate(String id);

    /**
     * Create a new Session ID.
     *
     * @param seedTerm the seed for RNG
     * @return the new session id
     */
    String createSessionId(long seedTerm);

    /**
     * Change the id of a Session.
     *
     * @param oldId the current session id
     * @param newId the new session id
     * @return the Session after changing its id
     */
    String renewSessionId(String oldId, String newId);

    /**
     * Adds an event listener for session-related events.
     *
     * @param listener the session listener
     * @see #removeSessionListener(SessionListener)
     */
    void addSessionListener(SessionListener listener);

    /**
     * Removes an event listener for for session-related events.
     *
     * @param listener the session listener to remove
     * @see #addSessionListener(SessionListener)
     */
    void removeSessionListener(SessionListener listener);

    /**
     * Removes all event listeners for session-related events.
     *
     * @see #removeSessionListener(SessionListener)
     */
    void clearSessionListeners();

    /**
     * Call binding and attribute listeners based on the new and old
     * values of the attribute.
     *
     * @param session the basic session
     * @param name name of the attribute
     * @param oldValue previous value of the attribute
     * @param newValue  new value of the attribute
     */
    void attributeChanged(BasicSession session, String name, Object oldValue, Object newValue);

    /**
     * Call the activation listeners.
     * This must be called holding the lock.
     *
     * @param session the basic session
     */
    void didActivate(BasicSession session);

    /**
     * Call the passivation listeners.
     * This must be called holding the lock.
     *
     * @param session the basic session
     */
    void willPassivate(BasicSession session);

    /**
     * @return the maximum amount of time session remained valid
     */
    long getSessionTimeMax();

    /**
     * @return the total amount of time all sessions remained valid
     */
    long getSessionTimeTotal();

    /**
     * @return the mean amount of time session remained valid
     */
    long getSessionTimeMean();

    /**
     * @return the standard deviation of amount of time session remained valid
     */
    double getSessionTimeStdDev();

    /**
     * Resets the session usage statistics.
     */
    void statsReset();

}
