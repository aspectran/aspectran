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
package com.aspectran.core.component.session.redis.lettuce.cluster;

import com.aspectran.core.component.session.SessionData;
import com.aspectran.core.component.session.redis.lettuce.AbstractLettuceSessionStore;
import com.aspectran.core.component.session.redis.lettuce.ConnectionPool;
import com.aspectran.core.component.session.redis.lettuce.SessionDataCodec;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.ScanIterator;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Redis-based session store using Lettuce as the client.
 *
 * <p>Created: 2019/12/06</p>
 *
 * @since 6.6.0
 */
public class ClusterLettuceSessionStore extends AbstractLettuceSessionStore {

    private final ConnectionPool<StatefulRedisClusterConnection<String, SessionData>> pool;

    public ClusterLettuceSessionStore(ConnectionPool<StatefulRedisClusterConnection<String, SessionData>> pool) {
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

    private StatefulRedisClusterConnection<String, SessionData> getConnection() {
        try {
            return pool.getConnection();
        } catch (Exception e) {
            throw RedisConnectionException.create(e);
        }
    }

    <R> R sync(@NonNull Function<RedisClusterCommands<String, SessionData>, R> func) {
        try (StatefulRedisClusterConnection<String, SessionData> conn = getConnection()) {
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
        return sync(c -> {
            SessionData data = c.get(id);
            return checkExpiry(data, now);
        });
    }

    @Override
    public void doSave(String id, SessionData data) {
        sync(c -> c.set(id, data));
    }

}
