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
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

/**
 * Redis connection pool based on Lettuce and Apache Commons Pool.
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisConnectionPool extends AbstractConnectionPool<StatefulRedisConnection<String, SessionData>,
        RedisClient, RedisConnectionPoolConfig> {

    /**
     * Instantiates a new RedisConnectionPool.
     * @param poolConfig the pool configuration
     */
    public RedisConnectionPool(RedisConnectionPoolConfig poolConfig) {
        super(poolConfig);
    }

    @Override
    protected RedisClient createClient() {
        RedisURI redisURI = poolConfig.getRedisURI();
        if (redisURI == null) {
            throw new IllegalArgumentException("redisURI must not be null");
        }
        RedisClient client;
        if (poolConfig.getClientResources() != null) {
            client = RedisClient.create(poolConfig.getClientResources(), redisURI);
        } else {
            client = RedisClient.create(redisURI);
        }
        if (poolConfig.getClientOptions() != null) {
            client.setOptions(poolConfig.getClientOptions());
        }
        return client;
    }

    @Override
    protected StatefulRedisConnection<String, SessionData> connect(@NonNull RedisClient client, SessionDataCodec codec) {
        return client.connect(codec);
    }

    @Override
    protected void shutdownClient(@NonNull RedisClient client) {
        client.shutdown();
    }

}
