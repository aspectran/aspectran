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
package com.aspectran.core.component.session.redis.lettuce.primaryreplica;

import com.aspectran.core.component.session.SessionData;
import com.aspectran.core.component.session.redis.lettuce.AbstractConnectionPool;
import com.aspectran.core.component.session.redis.lettuce.SessionDataCodec;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;

import java.util.Arrays;

/**
 * Redis Primary-Replica connection pool based on Lettuce.
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisPrimaryReplicaConnectionPool
        extends AbstractConnectionPool<StatefulRedisConnection<String, SessionData>,
        RedisClient, RedisPrimaryReplicaConnectionPoolConfig> {

    /**
     * Instantiates a new RedisPrimaryReplicaConnectionPool.
     * @param poolConfig the pool configuration
     */
    public RedisPrimaryReplicaConnectionPool(RedisPrimaryReplicaConnectionPoolConfig poolConfig) {
        super(poolConfig);
    }

    @Override
    protected RedisClient createClient() {
        RedisURI[] redisURIs = poolConfig.getRedisURIs();
        if (redisURIs == null || redisURIs.length == 0) {
            throw new IllegalArgumentException("redisURIs must not be null or empty");
        }

        RedisClient client;
        if (poolConfig.getClientResources() != null) {
            client = RedisClient.create(poolConfig.getClientResources());
        } else {
            client = RedisClient.create();
        }

        if (poolConfig.getClientOptions() != null) {
            client.setOptions(poolConfig.getClientOptions());
        }

        return client;
    }

    @Override
    protected StatefulRedisConnection<String, SessionData> connect(RedisClient client, SessionDataCodec codec) {
        RedisURI[] redisURIs = poolConfig.getRedisURIs();
        StatefulRedisMasterReplicaConnection<String, SessionData> connection;
        if (redisURIs.length == 1) {
            connection = MasterReplica.connect(client, codec, redisURIs[0]);
        } else {
            connection = MasterReplica.connect(client, codec, Arrays.asList(redisURIs));
        }
        // Prefer to read from the upstream (primary) to ensure read-after-write consistency
        connection.setReadFrom(ReadFrom.UPSTREAM_PREFERRED);
        return connection;
    }

    @Override
    protected void shutdownClient(@NonNull RedisClient client) {
        client.shutdown();
    }

}
