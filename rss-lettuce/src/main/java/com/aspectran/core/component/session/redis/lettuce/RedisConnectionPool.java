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
import com.aspectran.utils.Assert;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * Redis connection pool based on Lettuce.
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisConnectionPool implements ConnectionPool<StatefulRedisConnection<String, SessionData>> {

    private final RedisConnectionPoolConfig poolConfig;

    private RedisClient client;

    private GenericObjectPool<StatefulRedisConnection<String, SessionData>> pool;

    public RedisConnectionPool(RedisConnectionPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    @Override
    public StatefulRedisConnection<String, SessionData> getConnection() throws Exception {
        Assert.state(pool != null, "No RedisConnectionPool configured");
        return pool.borrowObject();
    }

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
