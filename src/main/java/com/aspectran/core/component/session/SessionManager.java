/*
 * Copyright 2008-2017 Juho Jeong
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

import java.util.EventListener;

/**
 * <p>Created: 2017. 6. 12.</p>
 */
public interface SessionManager {

    BasicSession getSession(String sessionId);

    BasicSession getSession(String sessionId, boolean create);

    void invalidate(String id);

    String newSessionId(long seedTerm);

    SessionData loadSessionData(String id, boolean create);

    void storeSessionData(String id, SessionData sessionData);

    void destroy();

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
    void sessionAttributeChanged(BasicSession session, String name, Object oldValue, Object newValue);

}
