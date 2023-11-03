/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

/**
 * This exception occurs when the maximum number of sessions in the session cache is exceeded.
 *
 * <p>Created: 2023. 11. 2.</p>
 */
public class MaxSessionsExceededException extends RuntimeException {

    private static final long serialVersionUID = 7481714758313822060L;

    public static final String MAX_SESSIONS_EXCEEDED = "Max Sessions Exceeded";

    private final String id;

    private final int maxSessions;

    public MaxSessionsExceededException(String id, int maxSessions) {
        //super("Maximum of sessions " + maxSessions + " exceeded");
        super("Session id=" + id + " was rejected as the maximum number of sessions " +
                maxSessions + " has been hit");
        this.id = id;
        this.maxSessions = maxSessions;
    }

    public String getId() {
        return id;
    }

    public int getMaxSessions() {
        return maxSessions;
    }

}
