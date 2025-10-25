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
 * A wrapper class for a {@link Session}, useful for extending or for use in collections.
 * <p>This implementation provides {@code equals()} and {@code hashCode()} that delegate
 * to the wrapped session instance, allowing collections of {@code WrappedSession} to
 * correctly identify and manage unique sessions.
 * </p>
 *
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

    /**
     * Compares this wrapped session to another object for equality.
     * <p>This method intentionally violates the symmetry principle of the
     * {@code equals} contract. It returns {@code true} if the other object
     * is the underlying session instance itself, which allows this wrapper
     * to be found in collections of raw {@link Session} objects.
     * </p>
     * <p>The comparison prioritizes identity checks for efficiency:
     * <ul>
     *     <li>If {@code other} is the same instance as this {@code WrappedSession}, returns {@code true}.</li>
     *     <li>If {@code other} is the same instance as the wrapped {@link Session}, returns {@code true}.</li>
     *     <li>If {@code other} is another {@code WrappedSession}, it compares their wrapped {@link Session}s.</li>
     *     <li>Otherwise, it delegates the comparison to the wrapped {@link Session}'s {@code equals} method
     *         against {@code other}, allowing for asymmetric comparison with raw {@link Session} instances.</li>
     * </ul>
     * </p>
     * @param other the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
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
