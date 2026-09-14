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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.aspectran.core.component.session.redis.lettuce.AbstractConnectionPoolConfig.DEFAULT_POOL_SIZE;

/**
 * Abstract base class for Lettuce-based thread-safe, lock-free Redis connection pools.
 * <p>Instead of relying on heavy pool synchronization (e.g. Apache Commons Pool2)
 * which causes severe lock contention under high concurrency (e.g. Java 21 Virtual Threads),
 * this implementation maintains a striped set of shared {@link StatefulConnection}
 * instances. Each shared connection is wrapped in a proxy whose {@code close()} method
 * is a no-op, allowing callers to use standard {@code try-with-resources} blocks without
 * closing the underlying multiplexed socket connections.</p>
 *
 * @param <T> the type of connection object
 * @param <C> the type of the Lettuce client
 * @param <P> the type of the pool configuration
 *
 * <p>Created: 2025/10/20</p>
 */
public abstract class AbstractConnectionPool<T extends StatefulConnection<?, ?>, C, P
        extends AbstractConnectionPoolConfig> implements ConnectionPool<T> {

    protected final P poolConfig;

    protected C client;

    private T[] sharedConnections;

    private T[] proxyConnections;

    private final AtomicInteger connectionIndex = new AtomicInteger();

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
        Assert.state(proxyConnections != null && proxyConnections.length > 0,
                () -> getClass().getSimpleName() + " is not initialized");
        int idx = (connectionIndex.getAndIncrement() & 0x7FFFFFFF) % proxyConnections.length;
        return proxyConnections[idx];
    }

    @Override
    public boolean isAvailable() {
        if (client == null || sharedConnections == null) {
            return false;
        }
        for (T connection : sharedConnections) {
            if (connection != null && connection.isOpen()) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(SessionDataCodec codec) {
        Assert.state(client == null, () -> getClass().getSimpleName() + " is already configured");
        this.client = createClient();

        int poolSize = (poolConfig != null ? poolConfig.getPoolSize() : DEFAULT_POOL_SIZE);
        if (poolSize <= 0) {
            poolSize = DEFAULT_POOL_SIZE;
        }
        poolSize = Math.clamp(poolSize, 2, 32);

        sharedConnections = (T[]) new StatefulConnection<?, ?>[poolSize];
        proxyConnections = (T[]) new StatefulConnection<?, ?>[poolSize];
        for (int i = 0; i < poolSize; i++) {
            sharedConnections[i] = connect(client, codec);
            proxyConnections[i] = wrapSharedConnection(sharedConnections[i]);
        }
    }

    @Override
    public void destroy() {
        if (sharedConnections != null) {
            for (T connection : sharedConnections) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            sharedConnections = null;
            proxyConnections = null;
        }
        if (client != null) {
            try {
                shutdownClient(client);
            } catch (Exception e) {
                // ignore
            }
            client = null;
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private T wrapSharedConnection(@NonNull T connection) {
        Class<?>[] interfaces = getInterfaces(connection.getClass());
        return (T) Proxy.newProxyInstance(
                connection.getClass().getClassLoader(),
                interfaces,
                new SharedConnectionInvocationHandler(connection)
        );
    }

    private static Class<?> @NonNull [] getInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        while (clazz != null) {
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[0]);
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

    private static class SharedConnectionInvocationHandler implements InvocationHandler {

        private final StatefulConnection<?, ?> delegate;

        SharedConnectionInvocationHandler(StatefulConnection<?, ?> delegate) {
            this.delegate = delegate;
        }

        @Override
        @Nullable
        public Object invoke(Object proxy, @NonNull Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("close".equals(methodName) && (args == null || args.length == 0)) {
                // No-op: keep the shared connection open
                return null;
            }
            if ("closeAsync".equals(methodName) && (args == null || args.length == 0)) {
                return CompletableFuture.completedFuture(null);
            }
            if ("isOpen".equals(methodName) && (args == null || args.length == 0)) {
                return delegate.isOpen();
            }
            try {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

    }

}
