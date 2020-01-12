/*
 * Copyright (c) 2008-2020 The Aspectran Project
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
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.function.Function;

/**
 * A Redis-based session store using Lettuce as the client.
 *
 * <p>Created: 2019/12/06</p>
 *
 * @since 6.6.0
 */
public class DefaultLettuceSessionStore extends AbstractLettuceSessionStore<StatefulRedisConnection<String, SessionData>> {

    public DefaultLettuceSessionStore(ConnectionPool<StatefulRedisConnection<String, SessionData>> pool) {
        super(pool);
    }

    <R> R sync(Function<RedisCommands<String, SessionData>, R> func) throws Exception {
        try (StatefulRedisConnection<String, SessionData> conn = getConnectionPool().getConnection()) {
            return func.apply(conn.sync());
        }
    }

    @Override
    public SessionData load(String id) throws Exception {
        return sync(c -> c.get(id));
    }

    @Override
    public boolean delete(String id) throws Exception {
        return sync(c -> {
            Long deleted = c.del(id);
            return (deleted != null && deleted > 0L);
        });
    }

    @Override
    public boolean exists(String id) throws Exception {
        return sync(c -> {
            SessionData data = c.get(id);
            return checkExpiry(data);
        });
    }

    @Override
    public void doSave(String id, SessionData data, long lastSaveTime) throws Exception {
        sync(c -> {
            long timeout = calculateTimeout(data);
            if (timeout > 0L) {
                c.psetex(id, timeout, data);
            } else {
                // Never timeout
                c.set(id, data);
            }
            return null;
        });
    }

}
