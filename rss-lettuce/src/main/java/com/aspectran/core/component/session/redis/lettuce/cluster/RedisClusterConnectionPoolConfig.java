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
package com.aspectran.core.component.session.redis.lettuce.cluster;

import com.aspectran.core.component.session.SessionData;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterURIUtil;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.URI;
import java.util.List;

/**
 * Redis cluster connection pool configuration based on Lettuce.
 *
 * <p>Created: 2019/12/07</p>
 */
public class RedisClusterConnectionPoolConfig extends GenericObjectPoolConfig<StatefulRedisClusterConnection<String, SessionData>> {

    private RedisURI[] redisURIs;

    private ClusterClientOptions clusterClientOptions;

    public RedisClusterConnectionPoolConfig() {
        super();
    }

    public RedisURI[] getRedisURIs() {
        return redisURIs;
    }

    public void setRedisURIs(RedisURI... redisURIs) {
        if (redisURIs == null || redisURIs.length == 0) {
            throw new IllegalArgumentException("redisURIs must not be null or empty");
        }
        this.redisURIs = redisURIs;
    }

    public void setUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        List<RedisURI> redisURIs = RedisClusterURIUtil.toRedisURIs(URI.create(uri));
        this.redisURIs = redisURIs.toArray(new RedisURI[0]);
    }

    public ClusterClientOptions getClusterClientOptions() {
        return clusterClientOptions;
    }

    public void setClusterClientOptions(ClusterClientOptions clusterClientOptions) {
        this.clusterClientOptions = clusterClientOptions;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("redisURIs", redisURIs);
        tsb.append("clusterClientOptions", clusterClientOptions);
        return tsb.toString();
    }

}
