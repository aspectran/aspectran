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
 * Exception raised when session data cannot be read from the {@link SessionStore}.
 * This might occur if the persisted session data is corrupted or in an incompatible format.
 *
 * <p>Created: 2017. 9. 7.</p>
 */
public class UnreadableSessionDataException extends Exception {

    @Serial
    private static final long serialVersionUID = 799147544009142489L;

    private final String id;

    /**
     * Instantiates a new UnreadableSessionDataException.
     * @param id the ID of the session that could not be read
     * @param t the cause of the exception
     */
    public UnreadableSessionDataException(String id, Throwable t) {
        super("Unreadable session " + id, t);
        this.id = id;
    }

    /**
     * Returns the ID of the session that could not be read.
     * @return the session ID
     */
    public String getId() {
        return id;
    }

}
