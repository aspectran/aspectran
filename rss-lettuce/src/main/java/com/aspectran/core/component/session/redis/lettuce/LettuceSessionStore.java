/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.component.session.UnreadableSessionDataException;
import com.aspectran.core.component.session.UnwritableSessionDataException;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Created: 2019/12/06</p>
 */
public class LettuceSessionStore extends AbstractSessionStore {

    private static final Log log = LogFactory.getLog(LettuceSessionStore.class);

    private final LettucePool pool;

    public LettuceSessionStore(LettucePool pool) {
        this.pool = pool;
    }

    @Override
    public SessionData load(String id) throws Exception {
        try (StatefulRedisConnection<String, SessionData> connection = pool.getConnection()) {
            RedisCommands<String, SessionData> commands = connection.sync();
            return commands.get(id);
        } catch (Exception e) {
            throw new UnreadableSessionDataException(id, e);
        }
    }

    @Override
    public boolean delete(String id) throws Exception {
        try (StatefulRedisConnection<String, SessionData> connection = pool.getConnection()) {
            RedisCommands<String, SessionData> commands = connection.sync();
            Long deleted = commands.del(id);
            return (deleted != null && deleted > 0L);
        }
    }

    @Override
    public boolean exists(String id) throws Exception {
        try (StatefulRedisConnection<String, SessionData> connection = pool.getConnection()) {
            RedisCommands<String, SessionData> commands = connection.sync();
            SessionData data = commands.get(id);
            if (data != null) {
                return (data.getExpiryTime() <= 0L || data.getExpiryTime() > System.currentTimeMillis());
            } else {
                return false;
            }
        }
    }

    @Override
    public void doSave(String id, SessionData data, long lastSaveTime) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(1024)) {
            SessionData.serialize(data, baos, getNonPersistentAttributes());
            try (StatefulRedisConnection<String, SessionData> connection = pool.getConnection()) {
                RedisCommands<String, SessionData> commands = connection.sync();
                if (data.getMaxInactiveInterval() > 0L) {
                    int timeout = (int)(data.getMaxInactiveInterval() / 900) + (getSavePeriodSecs() * 2);
                    commands.setex(id, timeout, data);
                } else {
                    // Never timeout
                    commands.set(id, data);
                }
            } catch (Exception e) {
                throw new UnwritableSessionDataException(id, e);
            }
        }
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
                log.warn("Error checking if session " + candidate + " has expired", e);
            }
        }
        return expired;
    }

    @Override
    protected void doInitialize() throws Exception {
        pool.setNonPersistentAttributes(getNonPersistentAttributes());
        pool.initialize();
    }

    @Override
    protected void doDestroy() throws Exception {
        pool.destroy();
    }

}
