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
package com.aspectran.core.component.session.redis.lettuce.cluster;

import com.aspectran.core.component.session.SessionData;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterURIUtil;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration holder for a Lettuce-backed Redis Cluster connection pool.
 * <p>
 * Extends Apache Commons Pool's {@link org.apache.commons.pool2.impl.GenericObjectPoolConfig}
 * to expose Redis Cluster–specific properties:
 * </p>
 * <ul>
 *   <li>{@link #getRedisURIs() redisURIs} — target cluster node endpoints used to create connections</li>
 *   <li>{@link #getClusterClientOptions() clusterClientOptions} — optional
 *       {@link io.lettuce.core.cluster.ClusterClientOptions} applied to the underlying
 *       {@link io.lettuce.core.cluster.RedisClusterClient}</li>
 *   <li>{@link #getClientResources() clientResources} — optional
 *       {@link io.lettuce.core.resource.ClientResources} for advanced configuration like
 *       a {@code SocketAddressResolver}</li>
 * </ul>
 * The remaining pooling knobs (maxTotal, maxIdle, minIdle, etc.) are inherited from
 * {@code GenericObjectPoolConfig}.
 *
 * <p>Typical usage: populate this config (either with {@link #setRedisURIs(RedisURI...)}
 * or {@link #setUri(String)}), optionally set {@link #setClusterClientOptions(ClusterClientOptions)}
 * or {@link #setClientResources(ClientResources)}, then supply it to {@link RedisClusterConnectionPool}
 * which will create/configure the actual pool.</p>
 *
 * <p>Created: 2019/12/07</p>
 */
public class RedisClusterConnectionPoolConfig
        extends GenericObjectPoolConfig<StatefulRedisClusterConnection<String, SessionData>> {

    private RedisURI[] redisURIs;

    private ClusterClientOptions clusterClientOptions;

    private ClientResources clientResources;

    /**
     * Creates a new config with default pooling parameters inherited from
     * {@link GenericObjectPoolConfig}.
     */
    public RedisClusterConnectionPoolConfig() {
        super();
    }

    /**
     * Returns the Redis Cluster node URIs used to create new connections.
     * @return the array of RedisURIs to connect to
     */
    public RedisURI[] getRedisURIs() {
        return redisURIs;
    }

    /**
     * Sets the Redis URIs for the cluster nodes from one or more {@link RedisURI} objects.
     * This is the primary, type-safe method for programmatic configuration.
     * @param redisURIs one or more RedisURIs (must not be {@code null} or empty)
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
     * Sets Redis Cluster URIs from a single URI string that may contain multiple hosts, delimited by commas.
     * This is the recommended method for XML-based configuration.
     * <p>e.g., "redis://host1:port1,host2:port2"</p>
     * @param uri a Redis Cluster URI string (must not be {@code null} or empty)
     */
    public void setUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        List<RedisURI> redisURIs = RedisClusterURIUtil.toRedisURIs(URI.create(uri));
        this.redisURIs = redisURIs.toArray(new RedisURI[0]);
    }

    /**
     * Returns optional Lettuce cluster client options to tune connection behavior.
     * @return the cluster client options, or {@code null} if none set
     */
    public ClusterClientOptions getClusterClientOptions() {
        return clusterClientOptions;
    }

    /**
     * Sets optional Lettuce cluster client options to apply to the {@code RedisClusterClient}
     * created by the pool.
     * @param clusterClientOptions the cluster client options
     */
    public void setClusterClientOptions(ClusterClientOptions clusterClientOptions) {
        this.clusterClientOptions = clusterClientOptions;
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
        tsb.append("clusterClientOptions", clusterClientOptions);
        return tsb.toString();
    }

}
