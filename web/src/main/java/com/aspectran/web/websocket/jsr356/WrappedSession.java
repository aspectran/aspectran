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

import com.aspectran.utils.annotation.jsr305.Nullable;
import jakarta.websocket.Session;

/**
 * <p>Created: 2025-03-25</p>
 */
public class WrappedSession {

    private final Session session;

    public WrappedSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other || session == other) {
            return true;
        }
        if (other instanceof WrappedSession that) {
            return session.equals(that.getSession());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return session.hashCode();
    }

}
