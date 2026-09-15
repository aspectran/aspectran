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
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.Range;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisSortedSetCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
        CMD extends RedisKeyCommands<String, SessionData> &
                    RedisStringCommands<String, SessionData> &
                    RedisSortedSetCommands<String, SessionData>
        > extends AbstractSessionStore {

    private static final String DEFAULT_EXPIRY_INDEX_KEY = "aspectran:session:expiry";

    private String expiryIndexKey = DEFAULT_EXPIRY_INDEX_KEY;

    /**
     * Returns the key used for the session expiration sorted set index.
     * @return the expiration index key
     */
    public String getExpiryIndexKey() {
        return expiryIndexKey;
    }

    /**
     * Sets the key used for the session expiration sorted set index.
     * @param expiryIndexKey the expiration index key
     */
    public void setExpiryIndexKey(String expiryIndexKey) {
        this.expiryIndexKey = expiryIndexKey;
    }

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
                if (!expiryIndexKey.equals(key)) {
                    SessionData data = c.get(key);
                    func.accept(data);
                }
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
            c.zrem(expiryIndexKey, SessionData.of(id));
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
        sync(c -> {
            c.set(id, data);
            long expiry = data.getExpiry();
            if (expiry > 0L) {
                c.zadd(expiryIndexKey, (double)expiry, SessionData.of(id));
            } else {
                c.zrem(expiryIndexKey, SessionData.of(id));
            }
            return null;
        });
    }

    @Override
    public Set<String> doGetExpired(long time) {
        return sync(c -> {
            String lockKey = expiryIndexKey + ":scavenge-lock";
            long lockTtl = Math.max(30, getGracePeriodSecs());
            String lockResult = c.set(lockKey, SessionData.of("locked"), SetArgs.Builder.nx().ex(lockTtl));
            if (!"OK".equals(lockResult)) {
                // Another node is currently scavenging expired sessions from the store
                return Collections.emptySet();
            }
            List<SessionData> expiredSessions = c.zrangebyscore(
                    expiryIndexKey,
                    Range.create(0.0, (double)time)
            );
            if (expiredSessions == null || expiredSessions.isEmpty()) {
                return Collections.emptySet();
            }
            Set<String> expiredIds = new HashSet<>(expiredSessions.size());
            for (SessionData data : expiredSessions) {
                if (data != null && data.getId() != null) {
                    expiredIds.add(data.getId());
                }
            }
            return expiredIds;
        });
    }

    @Override
    public void doCleanOrphans(long time) {
        sync(c -> {
            String lockKey = expiryIndexKey + ":clean-lock";
            long lockTtl = Math.max(60, getGracePeriodSecs());
            String lockResult = c.set(lockKey, SessionData.of("locked"), SetArgs.Builder.nx().ex(lockTtl));
            if (!"OK".equals(lockResult)) {
                // Another node is currently cleaning orphans or cleaned recently
                return null;
            }
            List<SessionData> expiredSessions = c.zrangebyscore(
                    expiryIndexKey,
                    Range.create(0.0, (double)time)
            );
            if (expiredSessions != null && !expiredSessions.isEmpty()) {
                for (SessionData data : expiredSessions) {
                    if (data != null && data.getId() != null) {
                        c.del(data.getId());
                    }
                }
                c.zremrangebyscore(expiryIndexKey, Range.create(0.0, (double)time));
            }
            return null;
        });
    }

    @Override
    public Set<String> getAllSessions() {
        return sync(c -> {
            List<SessionData> allSessions = c.zrange(expiryIndexKey, 0, -1);
            if (allSessions == null || allSessions.isEmpty()) {
                return Collections.emptySet();
            }
            Set<String> all = new HashSet<>(allSessions.size());
            for (SessionData data : allSessions) {
                if (data != null && data.getId() != null) {
                    all.add(data.getId());
                }
            }
            return all;
        });
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("pool", getPool());
        tsb.append("expiryIndexKey", expiryIndexKey);
        tsb.append("gracePeriodSecs", getGracePeriodSecs());
        tsb.append("savePeriodSecs", getSavePeriodSecs());
        tsb.append("nonPersistentAttributes", getNonPersistentAttributes());
        return tsb.toString();
    }

}
