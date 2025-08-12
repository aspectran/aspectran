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
package com.aspectran.core.component.session.redis.lettuce;

import com.aspectran.core.component.session.AbstractSessionStore;
import com.aspectran.core.component.session.SessionData;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Base support class for Redis-backed {@link com.aspectran.core.component.session.SessionStore}
 * implementations using the Lettuce client.
 * <p>
 * This abstraction provides common logic to determine expired sessions and to
 * enumerate currently active session ids. Subclasses supply the Redis access
 * details by implementing {@link #scan(java.util.function.Consumer)}, which should
 * iterate over stored {@link SessionData} entries and feed them to the provided consumer.
 * </p>
 *
 * <p>
 * Responsibilities:
 * </p>
 * <ul>
 *   <li>Compute expired session IDs by comparing each entry's {@link SessionData#getExpiry()} to a given timestamp</li>
 *   <li>List all non-expired sessions for diagnostics or maintenance operations</li>
 *   <li>Provide no-op orphan cleanup by default (can be overridden if needed)</li>
 * </ul>
 *
 * <p>Created: 2019/12/06</p>
 * @since 6.6.0
 */
public abstract class AbstractLettuceSessionStore extends AbstractSessionStore {

    /**
     * Iterates over all stored session entries and applies the given consumer.
     * Subclasses must implement this to traverse keys/values (e.g., via SCAN)
     * and provide each corresponding {@link SessionData} instance to {@code func}.
     * @param func consumer to receive each decoded {@link SessionData}; never null
     */
    protected abstract void scan(Consumer<SessionData> func);

    /**
     * Scans all session entries and returns the set of session IDs whose
     * {@linkplain SessionData#getExpiry() expiry} is greater than 0 and less
     * than or equal to the provided timestamp.
     * @param time the cutoff time in milliseconds since epoch
     * @return a set of expired session IDs (never null)
     */
    @Override
    public Set<String> doGetExpired(long time) {
        Set<String> expired = new HashSet<>();
        // iterate over the saved sessions and work out which have expired
        scan(sessionData -> {
            if (sessionData != null) {
                long expiry = sessionData.getExpiry();
                if (expiry > 0 && expiry <= time) {
                    expired.add(sessionData.getId());
                }
            }
        });
        return expired;
    }

    /**
     * Hook to clean orphaned sessions from the backing store. The Lettuce-based
     * implementation does not track orphaned sessions and therefore does nothing by default.
     * @param time a reference time in milliseconds (ignored)
     */
    @Override
    public void doCleanOrphans(long time) {
        // Unnecessary
    }

    /**
     * Returns all currently valid (non-expired) session IDs by scanning the store
     * and filtering out entries whose {@linkplain SessionData#getExpiry() expiry}
     * is in the past. Sessions with no expiry (0 or negative) are treated as active.
     * @return a set of active session IDs (never null)
     */
    @Override
    public Set<String> getAllSessions() {
        long now = System.currentTimeMillis();
        Set<String> all = new HashSet<>();
        scan(sessionData -> {
            if (sessionData != null) {
                long expiry = sessionData.getExpiry();
                if (expiry <= 0 || expiry > now) {
                    all.add(sessionData.getId());
                }
            }
        });
        return all;
    }

}
