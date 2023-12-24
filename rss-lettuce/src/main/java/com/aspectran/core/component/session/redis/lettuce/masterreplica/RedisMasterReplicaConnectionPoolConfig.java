/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.component.session.redis.lettuce.masterreplica;

import com.aspectran.core.component.session.SessionData;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Arrays;

/**
 * Redis Master-Replica connection pool configuration based on Lettuce.
 *
 * <p>Created: 2019/12/07</p>
 */
public class RedisMasterReplicaConnectionPoolConfig extends GenericObjectPoolConfig<StatefulRedisConnection<String, SessionData>> {

    private RedisURI[] redisURIs;

    private ClientOptions clientOptions;

    public RedisMasterReplicaConnectionPoolConfig() {
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

    public void setNodes(String[] nodes) {
        if (nodes == null || nodes.length == 0) {
            throw new IllegalArgumentException("nodes must not be null or empty");
        }
        this.redisURIs = Arrays.stream(nodes).map(RedisURI::create).toArray(RedisURI[]::new);
    }

    public void setUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        RedisURI redisURI = RedisURI.create(uri);
        this.redisURIs = new RedisURI[] {redisURI};
    }

    public ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("redisURIs", redisURIs);
        tsb.append("clientOptions", clientOptions);
        return tsb.toString();
    }

}
