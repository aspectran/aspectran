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
import com.aspectran.core.component.session.redis.lettuce.ConnectionPool;
import com.aspectran.core.component.session.redis.lettuce.DefaultLettuceSessionStore;
import io.lettuce.core.api.StatefulRedisConnection;

/**
 * A specialization of {@link DefaultLettuceSessionStore} for Primary-Replica setups.
 * <p>
 * This class leverages the {@code DefaultLettuceSessionStore} implementation, as the command API
 * for a Primary-Replica setup is identical to that of a standalone setup. All Primary-Replica-specific
 * logic, such as routing reads and writes, is handled by the {@link RedisPrimaryReplicaConnectionPool}
 * and the underlying {@link io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection}.
 * </p>
 *
 * <p>Created: 2019/12/06</p>
 *
 * @since 6.6.0
 */
public class PrimaryReplicaLettuceSessionStore extends DefaultLettuceSessionStore {

    /**
     * Instantiates a new PrimaryReplicaLettuceSessionStore.
     * @param pool the connection pool
     */
    public PrimaryReplicaLettuceSessionStore(ConnectionPool<StatefulRedisConnection<String, SessionData>> pool) {
        super(pool);
    }

}
