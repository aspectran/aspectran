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
package com.aspectran.core.component.session;

import java.io.Serial;

/**
 * Exception thrown when an attempt is made to create a new session, but the
 * configured maximum number of active sessions in the {@link SessionCache} has
 * already been reached.
 *
 * <p>This indicates a resource limitation or a potential misconfiguration if
 * the limit is unexpectedly low for the expected load.</p>
 *
 * <p>Created: 2023. 11. 2.</p>
 */
public class MaxSessionsExceededException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7481714758313822060L;

    /** A standard message for this exception. */
    public static final String MAX_SESSIONS_EXCEEDED = "Max Sessions Exceeded";

    private final String id;

    private final int maxSessions;

    /**
     * Constructs a new MaxSessionsExceededException with the specified session ID
     * and the maximum allowed sessions.
     * @param id the ID of the session that was attempted to be created
     * @param maxSessions the maximum number of sessions allowed
     */
    public MaxSessionsExceededException(String id, int maxSessions) {
        super("Session id=" + id + " was rejected as the maximum number of sessions " +
                maxSessions + " has been hit");
        this.id = id;
        this.maxSessions = maxSessions;
    }

    /**
     * Returns the ID of the session that was attempted to be created.
     * @return the session ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the maximum number of sessions allowed in the cache.
     * @return the maximum sessions limit
     */
    public int getMaxSessions() {
        return maxSessions;
    }

}
