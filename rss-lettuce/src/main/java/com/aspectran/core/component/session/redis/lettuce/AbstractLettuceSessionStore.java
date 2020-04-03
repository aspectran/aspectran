/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

import com.aspectran.core.component.session.AbstractSessionStore;
import com.aspectran.core.component.session.SessionData;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * A Redis-based session store using Lettuce as the client.
 *
 * <p>Created: 2019/12/06</p>
 *
 * @since 6.6.0
 */
public abstract class AbstractLettuceSessionStore<T> extends AbstractSessionStore {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLettuceSessionStore.class);

    private final ConnectionPool<T> pool;

    public AbstractLettuceSessionStore(ConnectionPool<T> pool) {
        this.pool = pool;
    }

    protected ConnectionPool<T> getConnectionPool() {
        return pool;
    }

    @Override
    public Set<String> doGetExpired(Set<String> candidates) {
        long now = System.currentTimeMillis();
        Set<String> expired = new HashSet<>();
        for (String candidate : candidates) {
            try {
                SessionData data = load(candidate);
                // if the session no longer exists
                if (data != null) {
                    if (data.getExpiryTime() > 0 && data.getExpiryTime() <= now) {
                        expired.add(candidate);
                    }
                } else {
                    expired.add(candidate);
                }
            } catch (Exception e) {
                logger.warn("Error checking if session " + candidate + " has expired", e);
            }
        }
        return expired;
    }

    protected boolean checkExpiry(SessionData data) {
        if (data != null) {
            return (data.getExpiryTime() <= 0L || data.getExpiryTime() > System.currentTimeMillis());
        } else {
            return false;
        }
    }

    protected long calculateTimeout(SessionData data) {
        if (data.getMaxInactiveInterval() > 0L) {
            return (long)(data.getMaxInactiveInterval() / 0.9) + (getSavePeriodSecs() * 1000 * 2);
        } else {
            return 0L;
        }
    }

    @Override
    protected void doInitialize() throws Exception {
        SessionDataCodec codec = new SessionDataCodec(getNonPersistentAttributes());
        pool.initialize(codec);
    }

    @Override
    protected void doDestroy() throws Exception {
        pool.destroy();
    }

}
