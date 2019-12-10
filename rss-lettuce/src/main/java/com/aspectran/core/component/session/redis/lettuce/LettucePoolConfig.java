package com.aspectran.core.component.session.redis.lettuce;

import com.aspectran.core.component.session.SessionData;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Redis connection pool configuration using Lettuce.
 *
 * <p>Created: 2019/12/07</p>
 */
public class LettucePoolConfig extends GenericObjectPoolConfig<StatefulRedisConnection<String, SessionData>> {

    private String uri;

    public LettucePoolConfig() {
        super();
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
