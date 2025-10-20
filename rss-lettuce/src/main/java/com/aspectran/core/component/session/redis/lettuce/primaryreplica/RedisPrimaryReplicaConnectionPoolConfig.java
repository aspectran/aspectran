package com.aspectran.core.component.session.redis.lettuce.primaryreplica;

import com.aspectran.core.component.session.SessionData;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Arrays;

/**
 * Configuration holder for a Lettuce-backed Redis Primary-Replica connection pool.
 *
 * <p>Created: 2019/12/08</p>
 */
public class RedisPrimaryReplicaConnectionPoolConfig
        extends GenericObjectPoolConfig<StatefulRedisConnection<String, SessionData>> {

    private RedisURI[] redisURIs;

    private ClientOptions clientOptions;

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
     * Sets the Redis URIs from an array of node strings.
     * @param nodes an array of Redis node URI strings
     */
    public void setNodes(String[] nodes) {
        if (nodes == null || nodes.length == 0) {
            throw new IllegalArgumentException("nodes must not be null or empty");
        }
        this.redisURIs = Arrays.stream(nodes).map(RedisURI::create).toArray(RedisURI[]::new);
    }

    /**
     * Sets a single Redis URI from a string.
     * @param uri the Redis URI string
     */
    public void setUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            throw new IllegalArgumentException("uri must not be null or empty");
        }
        RedisURI redisURI = RedisURI.create(uri);
        this.redisURIs = new RedisURI[] {redisURI};
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

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("redisURIs", redisURIs);
        tsb.append("clientOptions", clientOptions);
        return tsb.toString();
    }

}
