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
import com.aspectran.core.component.session.redis.lettuce.AbstractConnectionPoolConfig;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterURIUtil;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration holder for a Lettuce-backed Redis Cluster connection pool.
 * <p>Extends {@link AbstractConnectionPoolConfig} to manage multi-node cluster URIs,
 * timeouts, cluster-specific client options, and pooling parameters.</p>
 *
 * <h2>Example Configuration (Aspectran XML)</h2>
 * <pre>{@code
 * <bean id="redisClusterConnectionPoolConfig" class="com.aspectran.core.component.session.redis.lettuce.cluster.RedisClusterConnectionPoolConfig">
 *     <property name="uri" value="redis://node1:6379,node2:6379,node3:6379"/>
 *     <property name="timeout" value="5s"/>
 *     <property name="maxTotal" value="50"/>
 *     <property name="maxIdle" value="20"/>
 * </bean>
 * }</pre>
 *
 * <h2>Example Configuration (Java Programmatic)</h2>
 * <pre>{@code
 * RedisClusterConnectionPoolConfig config = new RedisClusterConnectionPoolConfig();
 * config.setNodes("redis://node1:6379", "redis://node2:6379", "redis://node3:6379");
 * config.setTimeout(Duration.ofSeconds(5));
 * config.setMaxTotal(50);
 * }</pre>
 *
 * <p>Created: 2019/12/07</p>
 */
public class RedisClusterConnectionPoolConfig
        extends AbstractConnectionPoolConfig<StatefulRedisClusterConnection<String, SessionData>> {

    private RedisURI[] redisURIs;

    private ClusterClientOptions clusterClientOptions;

    /**
     * Creates a new config with default pooling parameters.
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

    @Override
    public void setTimeout(Duration timeout) {
        if (this.redisURIs != null && timeout != null) {
            for (RedisURI redisURI : this.redisURIs) {
                redisURI.setTimeout(timeout);
            }
        }
    }

    /**
     * Returns optional Lettuce cluster client options to tune connection behavior.
     * @return the cluster client options, or {@code null} if none set
     */
    public ClusterClientOptions getClusterClientOptions() {
        return (clusterClientOptions != null ? clusterClientOptions : (ClusterClientOptions)getClientOptions());
    }

    /**
     * Sets optional Lettuce cluster client options to apply to the {@code RedisClusterClient}
     * created by the pool.
     * @param clusterClientOptions the cluster client options
     */
    public void setClusterClientOptions(ClusterClientOptions clusterClientOptions) {
        this.clusterClientOptions = clusterClientOptions;
        setClientOptions(clusterClientOptions);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("redisURIs", redisURIs);
        tsb.append("clusterClientOptions", getClusterClientOptions());
        tsb.append("clientResources", getClientResources());
        return tsb.toString();
    }

}
