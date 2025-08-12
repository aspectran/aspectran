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
import com.aspectran.core.component.session.redis.lettuce.ConnectionPool;
import com.aspectran.core.component.session.redis.lettuce.SessionDataCodec;
import com.aspectran.utils.Assert;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.Arrays;

/**
 * Redis Cluster connection pool based on Lettuce and Apache Commons Pool.
 * <p>
 * Creates and borrows {@link io.lettuce.core.cluster.api.StatefulRedisClusterConnection} instances
 * that encode/decode values using a provided {@link SessionDataCodec}. Configure with
 * {@link RedisClusterConnectionPoolConfig}.
 * </p>
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisClusterConnectionPool implements ConnectionPool<StatefulRedisClusterConnection<String, SessionData>> {

    private final RedisClusterConnectionPoolConfig poolConfig;

    private RedisClusterClient client;

    private GenericObjectPool<StatefulRedisClusterConnection<String, SessionData>> pool;

    /**
     * Create a new pool with the given configuration.
     * @param poolConfig the pool configuration providing Redis URIs and client options
     */
    public RedisClusterConnectionPool(RedisClusterConnectionPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    /**
     * Borrows a clustered connection from the pool.
     * @return a ready-to-use clustered connection
     * @throws Exception if the pool is not initialized or cannot provide a connection
     */
    public StatefulRedisClusterConnection<String, SessionData> getConnection() throws Exception {
        Assert.state(pool != null, "No RedisClusterConnectionPool configured");
        return pool.borrowObject();
    }

    /**
     * Initializes this pool by creating a {@link RedisClusterClient} and configuring
     * an internal pool that creates clustered connections with the provided codec.
     * @param codec the codec used to serialize/deserialize {@link SessionData}
     * @throws IllegalArgumentException if the Redis URIs are missing
     * @throws IllegalStateException if initialization is attempted more than once
     */
    @Override
    public void initialize(SessionDataCodec codec) {
        Assert.state(pool == null, "RedisClusterConnectionPool is already configured");
        RedisURI[] redisURIs = poolConfig.getRedisURIs();
        if (redisURIs == null || redisURIs.length == 0) {
            throw new IllegalArgumentException("redisURIs must not be null or empty");
        }
        client = RedisClusterClient.create(Arrays.asList(redisURIs));
        if (poolConfig.getClusterClientOptions() != null) {
            client.setOptions(poolConfig.getClusterClientOptions());
        }
        pool = ConnectionPoolSupport
                .createGenericObjectPool(()
                        -> client.connect(codec), poolConfig);
    }

    /**
     * Closes the internal pool and shuts down the {@link RedisClusterClient}.
     * This method is idempotent and can be invoked multiple times safely.
     */
    @Override
    public void destroy() {
        if (pool != null) {
            pool.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }

}
