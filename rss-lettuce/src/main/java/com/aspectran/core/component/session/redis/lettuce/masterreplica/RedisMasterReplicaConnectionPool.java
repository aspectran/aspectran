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
package com.aspectran.core.component.session.redis.lettuce.masterreplica;

import com.aspectran.core.component.session.SessionData;
import com.aspectran.core.component.session.redis.lettuce.ConnectionPool;
import com.aspectran.core.component.session.redis.lettuce.SessionDataCodec;
import com.aspectran.utils.Assert;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.Arrays;

/**
 * Redis Master-Replica connection pool based on Lettuce.
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisMasterReplicaConnectionPool implements ConnectionPool<StatefulRedisConnection<String, SessionData>> {

    private final RedisMasterReplicaConnectionPoolConfig poolConfig;

    private RedisClient client;

    private GenericObjectPool<StatefulRedisConnection<String, SessionData>> pool;

    public RedisMasterReplicaConnectionPool(RedisMasterReplicaConnectionPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    @Override
    public StatefulRedisConnection<String, SessionData> getConnection() throws Exception {
        Assert.state(pool != null, "No RedisMasterReplicaConnectionPool configured");
        return pool.borrowObject();
    }

    @Override
    public void initialize(SessionDataCodec codec) {
        Assert.state(pool == null, "RedisMasterReplicaConnectionPool is already configured");
        RedisURI[] redisURIs = poolConfig.getRedisURIs();
        if (redisURIs == null || redisURIs.length == 0) {
            throw new IllegalArgumentException("redisURIs must not be null or empty");
        }
        client = RedisClient.create();
        if (poolConfig.getClientOptions() != null) {
            client.setOptions(poolConfig.getClientOptions());
        }
        pool = ConnectionPoolSupport
                .createGenericObjectPool(() -> {
                    StatefulRedisMasterReplicaConnection<String, SessionData> connection;
                    if (redisURIs.length == 1) {
                        connection = MasterReplica.connect(client, codec, redisURIs[0]);
                    } else {
                        connection = MasterReplica.connect(client, codec, Arrays.asList(redisURIs));
                    }
                    connection.setReadFrom(ReadFrom.UPSTREAM_PREFERRED);
                    return connection;
                }, poolConfig);
    }

    @Override
    public void destroy() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
        if (client != null) {
            client.shutdown();
            client = null;
        }
    }

}
