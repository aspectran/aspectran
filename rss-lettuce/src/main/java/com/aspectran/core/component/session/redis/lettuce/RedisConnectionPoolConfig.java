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
package com.aspectran.core.component.session.redis.lettuce;

import com.aspectran.core.component.session.SessionData;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Redis connection pool configuration based on Lettuce.
 *
 * <p>Created: 2019/12/07</p>
 */
public class RedisConnectionPoolConfig extends GenericObjectPoolConfig<StatefulRedisConnection<String, SessionData>> {

    private RedisURI redisURI;

    private ClientOptions clientOptions;

    public RedisConnectionPoolConfig() {
        super();
    }

    public RedisURI getRedisURI() {
        return redisURI;
    }

    public void setRedisURI(RedisURI redisURI) {
        if (redisURI == null) {
            throw new IllegalArgumentException("redisURI must not be null");
        }
        this.redisURI = redisURI;
    }

    public void setUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        this.redisURI = RedisURI.create(uri);
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
        tsb.append("redisURI", redisURI);
        tsb.append("clientOptions", clientOptions);
        return tsb.toString();
    }

}
