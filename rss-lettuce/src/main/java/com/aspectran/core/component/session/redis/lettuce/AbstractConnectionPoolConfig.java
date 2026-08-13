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

import com.aspectran.utils.StringUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

/**
 * Abstract base class for Lettuce-backed Redis connection pool configurations.
 * <p>Extends Apache Commons Pool's {@link GenericObjectPoolConfig} to provide common
 * Redis configuration parameters across standalone, cluster, and primary-replica topologies.</p>
 *
 * <h2>Supported Configuration Properties</h2>
 * <ul>
 *   <li>{@code uri} / {@code redisURI} — The target Redis endpoint(s) to connect to</li>
 *   <li>{@code timeout} — Connection and command timeout (e.g., {@code "5s"}, {@code "5000ms"}, {@code "1m"})</li>
 *   <li>{@code clientOptions} — Optional {@link ClientOptions} applied to the underlying Lettuce client</li>
 *   <li>{@code clientResources} — Optional {@link ClientResources} for custom thread pools or DNS resolvers</li>
 *   <li>Pool sizing knobs — {@code maxTotal}, {@code maxIdle}, {@code minIdle}, etc. inherited from {@link GenericObjectPoolConfig}</li>
 * </ul>
 *
 * @param <T> the type of connection object stored in the pool
 *
 * <p>Created: 2026/08/14</p>
 */
public abstract class AbstractConnectionPoolConfig<T> extends GenericObjectPoolConfig<T> {

    private ClientOptions clientOptions;

    private ClientResources clientResources;

    /**
     * Creates a new config with default pooling parameters inherited from
     * {@link GenericObjectPoolConfig}.
     */
    public AbstractConnectionPoolConfig() {
        super();
    }

    /**
     * Returns optional Lettuce client options to tune connection behavior.
     * @return the client options, or {@code null} if none set
     */
    public ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * Sets optional Lettuce client options to apply to the client created by the pool.
     * @param clientOptions the client options
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

    /**
     * Sets the connection timeout for all configured Redis URIs.
     * @param timeout the duration timeout
     */
    public abstract void setTimeout(Duration timeout);

    /**
     * Sets the connection timeout for all configured Redis URIs as a string (e.g. "5s", "5000ms").
     * @param timeout the timeout string
     */
    public void setTimeout(String timeout) {
        if (StringUtils.hasText(timeout)) {
            setTimeout(parseDuration(timeout));
        }
    }

    /**
     * Parses a duration string (e.g. "5s", "5000ms", "1m") into a {@link Duration}.
     * @param text the duration string to parse
     * @return the parsed {@link Duration}
     */
    protected static Duration parseDuration(@NonNull String text) {
        String trimmed = text.trim().toLowerCase();
        try {
            if (trimmed.endsWith("ms")) {
                long ms = Long.parseLong(trimmed.substring(0, trimmed.length() - 2).trim());
                return Duration.ofMillis(ms);
            } else if (trimmed.endsWith("s")) {
                long s = Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim());
                return Duration.ofSeconds(s);
            } else if (trimmed.endsWith("m")) {
                long m = Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim());
                return Duration.ofMinutes(m);
            } else {
                long s = Long.parseLong(trimmed);
                return Duration.ofSeconds(s);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration string: " + text, e);
        }
    }

}
