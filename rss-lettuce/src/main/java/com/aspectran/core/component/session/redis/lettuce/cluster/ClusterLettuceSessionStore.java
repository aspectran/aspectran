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
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;

/**
 * Redis-backed SessionStore for Redis Cluster using Lettuce.
 * <p>
 * Extends {@link AbstractLettuceSessionStore} and interacts with the cluster via a pooled
 * {@link io.lettuce.core.cluster.api.StatefulRedisClusterConnection}.
 * </p>
 *
 * <p>Created: 2019/12/06</p>
 * @since 6.6.0
 */
public class ClusterLettuceSessionStore extends AbstractLettuceSessionStore<
        StatefulRedisClusterConnection<String, SessionData>,
        RedisClusterCommands<String, SessionData>> {

    private final ConnectionPool<StatefulRedisClusterConnection<String, SessionData>> pool;

    /**
     * Instantiates a new ClusterLettuceSessionStore.
     * @param pool the connection pool
     */
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

    @Override
    protected ConnectionPool<StatefulRedisClusterConnection<String, SessionData>> getPool() {
        return pool;
    }

    @Override
    protected RedisClusterCommands<String, SessionData> getCommands(
            @NonNull StatefulRedisClusterConnection<String, SessionData> connection) {
        return connection.sync();
    }

}
