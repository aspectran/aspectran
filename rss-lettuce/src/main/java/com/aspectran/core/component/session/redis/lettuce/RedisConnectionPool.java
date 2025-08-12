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
import com.aspectran.utils.Assert;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * Redis connection pool based on Lettuce and Apache Commons Pool.
 * <p>
 * This pool is responsible for creating and borrowing Redis connections that
 * serialize values using a provided {@link SessionDataCodec}. It is configured via
 * {@link RedisConnectionPoolConfig}, which supplies the {@link RedisURI} as well
 * as optional {@link io.lettuce.core.ClientOptions} and pool tuning parameters.
 * </p>
 *
 * <p>Lifecycle:</p>
 * <ul>
 *   <li>{@link #initialize(SessionDataCodec)} — create the underlying {@link RedisClient}
 *       and internal {@link org.apache.commons.pool2.impl.GenericObjectPool}.</li>
 *   <li>{@link #getConnection()} — borrow a {@link io.lettuce.core.api.StatefulRedisConnection}
 *       from the pool. Callers should close the connection when finished so it is
 *       returned to the pool.</li>
 *   <li>{@link #destroy()} — close the pool and shut down the client. Safe to call multiple times.</li>
 * </ul>
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisConnectionPool implements ConnectionPool<StatefulRedisConnection<String, SessionData>> {

    private final RedisConnectionPoolConfig poolConfig;

    private RedisClient client;

    private GenericObjectPool<StatefulRedisConnection<String, SessionData>> pool;

    /**
     * Create a new pool with the given configuration.
     * @param poolConfig the pool configuration providing Redis URI, client options,
     *                   and commons-pool2 settings (must not be {@code null})
     */
    public RedisConnectionPool(RedisConnectionPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    /**
     * Borrows a Redis connection from the internal pool.
     * <p>
     * The returned connection must be closed by the caller when finished to
     * ensure it is returned to the pool.
     * </p>
     * @return a ready-to-use stateful Redis connection
     * @throws Exception if the pool is not initialized or cannot provide a connection
     */
    @Override
    public StatefulRedisConnection<String, SessionData> getConnection() throws Exception {
        Assert.state(pool != null, "No RedisConnectionPool configured");
        return pool.borrowObject();
    }

    /**
     * Initializes this pool by creating a {@link RedisClient} and configuring
     * an internal {@link GenericObjectPool} that creates connections with the provided codec.
     * <p>
     * Preconditions: {@link RedisConnectionPoolConfig#getRedisURI()} must be set.
     * If client options are present on the config, they are applied to the client.
     * </p>
     * @param codec the codec used to serialize/deserialize {@link SessionData}
     * @throws IllegalArgumentException if the Redis URI is missing
     * @throws IllegalStateException if initialization is attempted more than once
     */
    @Override
    public void initialize(SessionDataCodec codec) {
        Assert.state(pool == null, "RedisConnectionPool is already configured");
        RedisURI redisURI = poolConfig.getRedisURI();
        if (redisURI == null) {
            throw new IllegalArgumentException("redisURI must not be null");
        }
        client = RedisClient.create(redisURI);
        if (poolConfig.getClientOptions() != null) {
            client.setOptions(poolConfig.getClientOptions());
        }
        pool = ConnectionPoolSupport
                .createGenericObjectPool(()
                        -> client.connect(codec), poolConfig);
    }

    /**
     * Closes the internal pool (if initialized) and shuts down the {@link RedisClient}.
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
