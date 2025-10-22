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
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Configuration holder for a Lettuce-backed Redis connection pool for a single, standalone node.
 * <p>
 * Extends Apache Commons Pool's {@link org.apache.commons.pool2.impl.GenericObjectPoolConfig}
 * to expose Redis-specific properties:
 * </p>
 * <ul>
 *   <li>{@link #getRedisURI() redisURI} — target Redis endpoint used to create connections</li>
 *   <li>{@link #getClientOptions() clientOptions} — optional {@link io.lettuce.core.ClientOptions}
 *       applied to the underlying Lettuce {@code RedisClient}</li>
 * </ul>
 * The remaining pooling knobs (maxTotal, maxIdle, minIdle, etc.) are inherited from
 * {@code GenericObjectPoolConfig}.
 *
 * <p>Typical usage: populate this config (either with {@link #setRedisURI(RedisURI)} or
 * {@link #setUri(String)} and optional {@link #setClientOptions(ClientOptions)}), then supply it to
 * {@link RedisConnectionPool} which will create/configure the actual pool.</p>
 *
 * <p>Created: 2019/12/07</p>
 */
public class RedisConnectionPoolConfig extends GenericObjectPoolConfig<StatefulRedisConnection<String, SessionData>> {

    private RedisURI redisURI;

    private ClientOptions clientOptions;

    private ClientResources clientResources;

    /**
     * Creates a new config with default pooling parameters.
     */
    public RedisConnectionPoolConfig() {
        super();
    }

    /**
     * Returns the Redis URI used to create new connections.
     * @return the RedisURI to connect to
     */
    public RedisURI getRedisURI() {
        return redisURI;
    }

    /**
     * Sets the Redis target endpoint from a {@link RedisURI} object.
     * This is the primary, type-safe method for programmatic configuration.
     * @param redisURI the Redis URI (must not be {@code null})
     */
    public void setRedisURI(RedisURI redisURI) {
        if (redisURI == null) {
            throw new IllegalArgumentException("redisURI must not be null");
        }
        this.redisURI = redisURI;
    }

    /**
     * Sets the Redis endpoint from a single URI string.
     * This is the recommended method for XML-based configuration.
     * <p>e.g., "redis://host:port/0"</p>
     * @param uri the Redis URI string
     */
    public void setUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        this.redisURI = RedisURI.create(uri);
    }

    /**
     * Returns optional Lettuce client options to tune connection behavior.
     * @return the client options, or {@code null} if none set
     */
    public ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * Sets optional Lettuce client options to apply to the {@code RedisClient} created
     * by the pool.
     * @param clientOptions the client options
     */
    public void setClientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    /**
     * Returns the custom {@link ClientResources} for the Lettuce client.
     * @return the client resources
     */
    public ClientResources getClientResources() {
        return clientResources;
    }

    /**
     * Sets custom {@link ClientResources} for the Lettuce client, allowing for advanced
     * configuration like a {@code SocketAddressResolver}.
     * @param clientResources the client resources
     */
    public void setClientResources(ClientResources clientResources) {
        this.clientResources = clientResources;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("redisURI", redisURI);
        tsb.append("clientOptions", clientOptions);
        tsb.append("clientResources", clientResources);
        return tsb.toString();
    }

}
