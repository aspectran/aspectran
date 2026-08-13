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
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

import java.time.Duration;

/**
 * Configuration holder for a Lettuce-backed Redis connection pool targeting a single, standalone node.
 * <p>Extends {@link AbstractConnectionPoolConfig} to manage Redis endpoint URIs, timeouts,
 * client options, and pooling parameters for single-node Redis deployments.</p>
 *
 * <h2>Example Configuration (Aspectran XML)</h2>
 * <pre>{@code
 * <bean id="redisConnectionPoolConfig" class="com.aspectran.core.component.session.redis.lettuce.RedisConnectionPoolConfig">
 *     <property name="uri" value="redis://localhost:6379/0"/>
 *     <property name="timeout" value="5s"/>
 *     <property name="maxTotal" value="20"/>
 *     <property name="maxIdle" value="10"/>
 *     <property name="minIdle" value="5"/>
 * </bean>
 * }</pre>
 *
 * <h2>Example Configuration (Java Programmatic)</h2>
 * <pre>{@code
 * RedisConnectionPoolConfig config = new RedisConnectionPoolConfig();
 * config.setUri("redis://localhost:6379/0");
 * config.setTimeout("5s");
 * config.setMaxTotal(20);
 * }</pre>
 *
 * <p>Created: 2019/12/07</p>
 */
public class RedisConnectionPoolConfig
        extends AbstractConnectionPoolConfig<StatefulRedisConnection<String, SessionData>> {

    private RedisURI redisURI;

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

    @Override
    public void setTimeout(Duration timeout) {
        if (this.redisURI != null && timeout != null) {
            this.redisURI.setTimeout(timeout);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("redisURI", redisURI);
        tsb.append("clientOptions", getClientOptions());
        tsb.append("clientResources", getClientResources());
        return tsb.toString();
    }

}
