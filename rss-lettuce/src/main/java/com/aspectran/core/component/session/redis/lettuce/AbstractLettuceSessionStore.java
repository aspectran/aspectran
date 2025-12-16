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
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An abstract base class for Lettuce-based session stores, providing common logic for session operations.
 * <p>
 * This class encapsulates the pattern of borrowing a connection, executing commands, and returning
 * the connection to the pool. Subclasses must provide the connection pool and a way to obtain
 * the synchronous command API from a connection.
 * </p>
 *
 * @param <C> the type of the stateful connection
 * @param <CMD> the type of the Redis commands interface
 *
 * <p>Created: 2019/12/06</p>
 * @since 6.6.0
 */
public abstract class AbstractLettuceSessionStore<
        C extends StatefulConnection<String, SessionData>,
        CMD extends RedisKeyCommands<String, SessionData> & RedisStringCommands<String, SessionData>
        > extends AbstractSessionStore {

    /**
     * Returns the connection pool.
     * @return the connection pool
     */
    protected abstract ConnectionPool<C> getPool();

    /**
     * Returns the synchronous command API from the given connection.
     * @param connection the stateful connection
     * @return the synchronous command API
     */
    protected abstract CMD getCommands(C connection);

    /**
     * Executes a synchronous Redis callback with a borrowed connection, ensuring
     * the connection is returned to the pool when finished.
     * @param func function receiving the synchronous command API
     * @param <R> result type
     * @return the callback result
     * @throws RedisConnectionException if a connection cannot be obtained
     */
    protected <R> R sync(@NonNull Function<CMD, R> func) {
        try (C conn = getPool().getConnection()) {
            return func.apply(getCommands(conn));
        } catch (Exception e) {
            throw new RedisConnectionException("Could not get a resource from the pool", e);
        }
    }

    /**
     * Iterates all keys and feeds decoded SessionData to the given consumer.
     * Uses Redis SCAN to avoid blocking the server.
     * @param func consumer that will receive each SessionData (may receive nulls if keys are missing)
     */
    protected void scan(Consumer<SessionData> func) {
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

    @Override
    public SessionData load(String id) {
        return sync(c -> c.get(id));
    }

    @Override
    public boolean delete(String id) {
        return sync(c -> {
            Long deleted = c.del(id);
            return (deleted != null && deleted > 0L);
        });
    }

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

    @Override
    public void doSave(String id, SessionData data) {
        sync(c -> c.set(id, data));
    }

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

    @Override
    public void doCleanOrphans(long time) {
        // Unnecessary
    }

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
