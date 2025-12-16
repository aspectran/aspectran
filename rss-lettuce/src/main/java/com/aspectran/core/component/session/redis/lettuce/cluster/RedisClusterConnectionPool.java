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
import com.aspectran.core.component.session.redis.lettuce.AbstractConnectionPool;
import com.aspectran.core.component.session.redis.lettuce.SessionDataCodec;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/**
 * Redis Cluster connection pool based on Lettuce and Apache Commons Pool.
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisClusterConnectionPool
        extends AbstractConnectionPool<StatefulRedisClusterConnection<String, SessionData>,
        RedisClusterClient, RedisClusterConnectionPoolConfig> {

    /**
     * Instantiates a new RedisClusterConnectionPool.
     * @param poolConfig the pool configuration
     */
    public RedisClusterConnectionPool(RedisClusterConnectionPoolConfig poolConfig) {
        super(poolConfig);
    }

    @Override
    protected RedisClusterClient createClient() {
        RedisURI[] redisURIs = poolConfig.getRedisURIs();
        if (redisURIs == null || redisURIs.length == 0) {
            throw new IllegalArgumentException("redisURIs must not be null or empty");
        }

        RedisClusterClient client;
        if (poolConfig.getClientResources() != null) {
            client = RedisClusterClient.create(poolConfig.getClientResources(), Arrays.asList(redisURIs));
        } else {
            client = RedisClusterClient.create(Arrays.asList(redisURIs));
        }
        if (poolConfig.getClusterClientOptions() != null) {
            client.setOptions(poolConfig.getClusterClientOptions());
        }
        return client;
    }

    @Override
    protected StatefulRedisClusterConnection<String, SessionData> connect(@NonNull RedisClusterClient client, SessionDataCodec codec) {
        return client.connect(codec);
    }

    @Override
    protected void shutdownClient(@NonNull RedisClusterClient client) {
        client.shutdown();
    }

}
