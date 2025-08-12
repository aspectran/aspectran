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

import com.aspectran.core.component.session.SessionData;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Redis-backed SessionStore using a single-node (standalone) Lettuce connection.
 * <p>
 * Builds upon {@link AbstractLettuceSessionStore} for common expiry handling and
 * provides concrete Redis access using a pooled {@link io.lettuce.core.api.StatefulRedisConnection}.
 * </p>
 *
 * <p>Created: 2019/12/06</p>
 * @since 6.6.0
 */
public class DefaultLettuceSessionStore extends AbstractLettuceSessionStore {

    private final ConnectionPool<StatefulRedisConnection<String, SessionData>> pool;

    public DefaultLettuceSessionStore(ConnectionPool<StatefulRedisConnection<String, SessionData>> pool) {
        this.pool = pool;
    }

    @Override
    protected void doInitialize() throws Exception {
        SessionDataCodec codec = new SessionDataCodec(getNonPersistentAttributes());
        pool.initialize(codec);
    }

    @Override
    protected void doDestroy() throws Exception {
        pool.destroy();
    }

    private StatefulRedisConnection<String, SessionData> getConnection() {
        try {
            return pool.getConnection();
        } catch (Exception e) {
            throw RedisConnectionException.create(e);
        }
    }

    /**
     * Executes a synchronous Redis callback with a borrowed connection, ensuring
     * the connection is returned to the pool when finished.
     * @param func function receiving the synchronous command API
     * @param <R> result type
     * @return the callback result
     * @throws io.lettuce.core.RedisConnectionException if a connection cannot be obtained
     */
    <R> R sync(@NonNull Function<RedisCommands<String, SessionData>, R> func) {
        try (StatefulRedisConnection<String, SessionData> conn = getConnection()) {
            return func.apply(conn.sync());
        }
    }

    /**
     * Iterates all keys and feeds decoded SessionData to the given consumer.
     * Uses Redis SCAN to avoid blocking the server.
     * @param func consumer that will receive each SessionData (may receive nulls if keys are missing)
     */
    @Override
    public void scan(Consumer<SessionData> func) {
        sync(c -> {
            ScanIterator<String> scanIterator = ScanIterator.scan(c);
            while (scanIterator.hasNext()) {
                String key = scanIterator.next();
                SessionData data = c.get(key);
                func.accept(data);
            }
            return null;
        });
    }

    /**
     * Loads a session by id from Redis.
     * @param id the session id
     * @return the decoded SessionData, or {@code null} if not found
     */
    @Override
    public SessionData load(String id) {
        return sync(c -> c.get(id));
    }

    /**
     * Deletes a session by id.
     * @param id the session id
     * @return {@code true} if at least one key was removed
     */
    @Override
    public boolean delete(String id) {
        return sync(c -> {
            Long deleted = c.del(id);
            return (deleted != null && deleted > 0L);
        });
    }

    /**
     * Checks if a session exists and is not expired.
     * @param id the session id
     * @return {@code true} if the session exists and has not expired
     */
    @Override
    public boolean exists(String id) {
        long now = System.currentTimeMillis();
        return sync(c -> {
            SessionData data = c.get(id);
            if (data != null) {
                return checkExpiry(data, now);
            } else {
                return false;
            }
        });
    }

    /**
     * Persists the SessionData under the given id.
     * @param id the session id
     * @param data the session data to store
     */
    @Override
    public void doSave(String id, SessionData data) {
        sync(c -> c.set(id, data));
    }

}
