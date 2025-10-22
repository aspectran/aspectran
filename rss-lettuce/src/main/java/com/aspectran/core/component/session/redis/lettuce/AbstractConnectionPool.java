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

import com.aspectran.utils.Assert;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Abstract base class for Lettuce-based connection pools.
 * <p>
 * This class provides the core infrastructure for managing the lifecycle of
 * a {@link GenericObjectPool}, including initialization and destruction logic.
 * Subclasses must implement methods to create the specific Lettuce client
 * (e.g., {@code RedisClient}, {@code RedisClusterClient}), establish a
 * connection, and shut down the client.
 * </p>
 *
 * @param <T> the type of connection object
 * @param <C> the type of the Lettuce client
 * @param <P> the type of the pool configuration
 *
 * <p>Created: 2025/10/20</p>
 */
public abstract class AbstractConnectionPool<T extends StatefulConnection<?, ?>, C, P
        extends GenericObjectPoolConfig<T>> implements ConnectionPool<T> {

    protected final P poolConfig;

    protected C client;

    protected GenericObjectPool<T> pool;

    /**
     * Instantiates a new AbstractConnectionPool.
     * @param poolConfig the pool configuration
     */
    public AbstractConnectionPool(P poolConfig) {
        this.poolConfig = poolConfig;
    }

    protected P getPoolConfig() {
        return poolConfig;
    }

    @Override
    public T getConnection() throws Exception {
        Assert.state(pool != null, "No " + this.getClass().getSimpleName() + " configured");
        return pool.borrowObject();
    }

    @Override
    public void initialize(SessionDataCodec codec) {
        Assert.state(pool == null, this.getClass().getSimpleName() + " is already configured");
        this.client = createClient();
        this.pool = ConnectionPoolSupport.createGenericObjectPool(() -> connect(client, codec), poolConfig);
    }

    @Override
    public void destroy() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
        if (client != null) {
            shutdownClient(client);
            client = null;
        }
    }

    /**
     * Creates the underlying Lettuce client instance.
     * @return the new client instance
     */
    protected abstract C createClient();

    /**
     * Establishes a connection using the given client and codec.
     * @param client the Lettuce client
     * @param codec the session data codec
     * @return the new connection instance
     */
    protected abstract T connect(C client, SessionDataCodec codec);

    /**
     * Shuts down the underlying Lettuce client.
     * @param client the client to shut down
     */
    protected abstract void shutdownClient(C client);

}
