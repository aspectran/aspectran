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

import com.aspectran.core.component.session.AbstractSessionStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates a Redis-based session store using Lettuce for Primary-Replica environments.
 *
 * <p>Created: 2019/12/06</p>
 *
 * @since 6.6.0
 */
public class PrimaryReplicaLettuceSessionStoreFactory extends AbstractSessionStoreFactory {

    private static final Logger logger = LoggerFactory.getLogger(PrimaryReplicaLettuceSessionStoreFactory.class);

    private RedisPrimaryReplicaConnectionPoolConfig poolConfig;

    /**
     * Returns the configuration for the connection pool.
     * @return the pool configuration
     */
    protected RedisPrimaryReplicaConnectionPoolConfig getPoolConfig() {
        return poolConfig;
    }

    /**
     * Sets the configuration for the connection pool.
     * @param poolConfig the pool configuration
     */
    public void setPoolConfig(RedisPrimaryReplicaConnectionPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    /**
     * Creates a new {@link PrimaryReplicaLettuceSessionStore} instance.
     * @return a new {@link PrimaryReplicaLettuceSessionStore}
     */
    @Override
    public PrimaryReplicaLettuceSessionStore createSessionStore() {
        if (logger.isDebugEnabled()) {
            logger.debug("RedisPrimaryReplicaConnectionPoolConfig {}", poolConfig);
        }
        RedisPrimaryReplicaConnectionPool pool = new RedisPrimaryReplicaConnectionPool(poolConfig);
        PrimaryReplicaLettuceSessionStore sessionStore = new PrimaryReplicaLettuceSessionStore(pool);
        if (getNonPersistentAttributes() != null) {
            sessionStore.setNonPersistentAttributes(getNonPersistentAttributes());
        }
        return sessionStore;
    }

}
