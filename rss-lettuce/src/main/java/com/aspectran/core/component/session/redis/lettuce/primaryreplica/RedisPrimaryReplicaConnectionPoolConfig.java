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
import com.aspectran.core.component.session.redis.lettuce.AbstractConnectionPoolConfig;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterURIUtil;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration holder for a Lettuce-backed Redis Primary-Replica connection pool.
 * <p>Extends {@link AbstractConnectionPoolConfig} to manage Primary-Replica node URIs,
 * timeouts, client options, and pooling parameters.</p>
 *
 * <h2>Example Configuration (Aspectran XML)</h2>
 * <pre>{@code
 * <bean id="redisPrimaryReplicaConfig" class="com.aspectran.core.component.session.redis.lettuce.primaryreplica.RedisPrimaryReplicaConnectionPoolConfig">
 *     <property name="uri" value="redis://primary:6379,replica1:6379"/>
 *     <property name="timeout" value="5s"/>
 *     <property name="maxTotal" value="30"/>
 * </bean>
 * }</pre>
 *
 * <h2>Example Configuration (Java Programmatic)</h2>
 * <pre>{@code
 * RedisPrimaryReplicaConnectionPoolConfig config = new RedisPrimaryReplicaConnectionPoolConfig();
 * config.setNodes("redis://primary:6379", "redis://replica1:6379");
 * config.setTimeout("5s");
 * config.setMaxTotal(30);
 * }</pre>
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisPrimaryReplicaConnectionPoolConfig
        extends AbstractConnectionPoolConfig<StatefulRedisConnection<String, SessionData>> {

    private RedisURI[] redisURIs;

    public RedisPrimaryReplicaConnectionPoolConfig() {
        super();
    }

    /**
     * Returns the Redis URIs for the Primary-Replica nodes.
     * @return an array of {@link RedisURI}
     */
    public RedisURI[] getRedisURIs() {
        return redisURIs;
    }

    /**
     * Sets the Redis URIs for the Primary-Replica nodes.
     * @param redisURIs an array of {@link RedisURI}
     */
    public void setRedisURIs(RedisURI... redisURIs) {
        if (redisURIs == null || redisURIs.length == 0) {
            throw new IllegalArgumentException("redisURIs must not be null or empty");
        }
        this.redisURIs = redisURIs;
    }

    /**
     * Sets Redis URIs from one or more individual URI strings.
     * <p>e.g., "redis://host1:6379", "redis://host2:6380"</p>
     * @param nodes an array of Redis node URI strings
     */
    public void setNodes(String... nodes) {
        if (nodes == null || nodes.length == 0) {
            throw new IllegalArgumentException("nodes must not be null or empty");
        }
        this.redisURIs = Arrays.stream(nodes)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(RedisURI::create)
                .toArray(RedisURI[]::new);
    }

    /**
     * Sets Redis URIs from a single URI string that may contain multiple hosts, delimited by commas.
     * This is the recommended method for XML-based configuration for consistency with
     * {@link com.aspectran.core.component.session.redis.lettuce.cluster.RedisClusterConnectionPoolConfig}.
     * <p>e.g., "redis://host1:6379,host2:6380"</p>
     * @param uri the Redis URI string
     */
    public void setUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        List<RedisURI> redisURIs = RedisClusterURIUtil.toRedisURIs(URI.create(uri));
        this.redisURIs = redisURIs.toArray(new RedisURI[0]);
    }

    @Override
    public void setTimeout(Duration timeout) {
        if (this.redisURIs != null && timeout != null) {
            for (RedisURI redisURI : this.redisURIs) {
                redisURI.setTimeout(timeout);
            }
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("redisURIs", redisURIs);
        tsb.append("clientOptions", getClientOptions());
        tsb.append("clientResources", getClientResources());
        return tsb.toString();
    }

}
