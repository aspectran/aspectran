/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Redis-based session store using Lettuce as the client.
 *
 * <p>Created: 2019/12/06</p>
 *
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

    <R> R sync(Function<RedisCommands<String, SessionData>, R> func) {
        try (StatefulRedisConnection<String, SessionData> conn = getConnection()) {
            return func.apply(conn.sync());
        }
    }

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
        return sync(c -> checkExpiry(c.get(id), now));
    }

    @Override
    public void doSave(String id, SessionData data) {
        sync(c -> c.set(id, data));
    }

}
