/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.Set;

/**
 * Redis connection pool using Lettuce.
 *
 * <p>Created: 2019/12/08</p>
 */
public class LettucePool {

    private final LettucePoolConfig poolConfig;

    private Set<String> nonPersistentAttributes;

    private RedisClient client;

    private GenericObjectPool<StatefulRedisConnection<String, SessionData>> pool;

    public LettucePool(LettucePoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    public void setNonPersistentAttributes(Set<String> nonPersistentAttributes) {
        this.nonPersistentAttributes = nonPersistentAttributes;
    }

    public void initialize() {
        if (client != null) {
            throw new IllegalStateException("LettucePool is already initialized");
        }
        client = RedisClient.create(RedisURI.create(poolConfig.getUri()));
        pool = ConnectionPoolSupport
                .createGenericObjectPool(()
                        -> client.connect(new SessionDataCodec(nonPersistentAttributes)), poolConfig);
    }

    public StatefulRedisConnection<String, SessionData> getConnection() throws Exception {
        if (pool == null) {
            throw new IllegalStateException("LettucePool is not initialized");
        }
        return pool.borrowObject();
    }

    public void destroy() {
        if (pool != null) {
            pool.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }

}
