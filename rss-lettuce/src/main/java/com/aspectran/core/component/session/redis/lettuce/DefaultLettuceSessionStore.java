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
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * Redis-backed SessionStore using a single-node (standalone) Lettuce connection.
 * <p>
 * Builds upon {@link AbstractLettuceSessionStore} for common session operations and
 * provides concrete Redis access using a pooled {@link io.lettuce.core.api.StatefulRedisConnection}.
 * </p>
 *
 * <p>Created: 2019/12/06</p>
 * @since 6.6.0
 */
public class DefaultLettuceSessionStore extends AbstractLettuceSessionStore<
        StatefulRedisConnection<String, SessionData>,
        RedisCommands<String, SessionData>> {

    private final ConnectionPool<StatefulRedisConnection<String, SessionData>> pool;

    /**
     * Instantiates a new DefaultLettuceSessionStore.
     * @param pool the connection pool
     */
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

    @Override
    protected ConnectionPool<StatefulRedisConnection<String, SessionData>> getPool() {
        return pool;
    }

    @Override
    protected RedisCommands<String, SessionData> getCommands(
            @NonNull StatefulRedisConnection<String, SessionData> connection) {
        return connection.sync();
    }

}
