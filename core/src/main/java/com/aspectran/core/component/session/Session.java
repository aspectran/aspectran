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
package com.aspectran.core.component.session;

import java.util.Set;

/**
 * The Interface Session.
 *
 * <p>Created: 2017. 6. 13.</p>
 */
public interface Session {

    String getId();

    <T> T getAttribute(String name);

    Set<String> getAttributeNames();

    Object setAttribute(String name, Object value);

    Object removeAttribute(String name);

    long getCreationTime();

    long getLastAccessedTime();

    int getMaxInactiveInterval();

    void setMaxInactiveInterval(int secs);

    /**
     * Returns true if a new session was created for the request.
     * @return true if the server has created a session,
     *      but the client has not yet joined
     */
    boolean isNew();

    boolean isTempResident();

    /**
     * Returns whether it is a valid session or not.
     * @return true if valid session, false otherwise
     */
    boolean isValid();

    /**
     * Called when a session is first accessed by a request.
     */
    boolean access();

    /**
     * Called when a session is last accessed by a request.
     */
    void complete();

    /**
     * Called by users to invalidate a session, or called by the
     * access method as a request enters the session if the session
     * has expired, or called by manager as a result of scavenger
     * expiring session.
     */
    void invalidate();

    /**
     * Returns the reason why the session was destroyed.
     * @return the destroyed reason
     */
    DestroyedReason getDestroyedReason();

    enum DestroyedReason {
        INVALIDATED,
        TIMEOUT,
        UNDEPLOY
    }

}
