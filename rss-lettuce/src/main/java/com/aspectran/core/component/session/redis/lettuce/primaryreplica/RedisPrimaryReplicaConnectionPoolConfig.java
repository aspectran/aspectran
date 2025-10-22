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
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterURIUtil;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration holder for a Lettuce-backed Redis Primary-Replica connection pool.
 * <p>
 * Extends Apache Commons Pool's {@link org.apache.commons.pool2.impl.GenericObjectPoolConfig}
 * to expose Redis Primary-Replica–specific properties:
 * </p>
 * <ul>
 *   <li>{@link #getRedisURIs() redisURIs} — target Primary-Replica node endpoints used to create connections</li>
 *   <li>{@link #getClientOptions() clientOptions} — optional
 *       {@link io.lettuce.core.ClientOptions} applied to the underlying
 *       {@link io.lettuce.core.RedisClient}</li>
 *   <li>{@link #getClientResources() clientResources} — optional
 *       {@link io.lettuce.core.resource.ClientResources} for advanced configuration</li>
 * </ul>
 * The remaining pooling knobs (maxTotal, maxIdle, minIdle, etc.) are inherited from
 * {@code GenericObjectPoolConfig}.
 *
 * <p>Typical usage: populate this config (e.g., with {@link #setUri(String)}),
 * optionally set {@link #setClientOptions(ClientOptions)} or {@link #setClientResources(ClientResources)},
 * then supply it to {@link RedisPrimaryReplicaConnectionPool} which will create/configure the actual pool.</p>
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisPrimaryReplicaConnectionPoolConfig
        extends GenericObjectPoolConfig<StatefulRedisConnection<String, SessionData>> {

    private RedisURI[] redisURIs;

    private ClientOptions clientOptions;

    private ClientResources clientResources;

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

    /**
     * Returns the Lettuce client options.
     * @return the {@link ClientOptions}
     */
    public ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * Sets the Lettuce client options.
     * @param clientOptions the {@link ClientOptions}
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
        tsb.append("redisURIs", redisURIs);
        tsb.append("clientOptions", clientOptions);
        tsb.append("clientResources", clientResources);
        return tsb.toString();
    }

}
