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
package com.aspectran.core.component.session.redis.lettuce;

import com.aspectran.core.component.session.AbstractSessionStore;
import com.aspectran.core.component.session.SessionData;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A Redis-based session store using Lettuce as the client.
 *
 * <p>Created: 2019/12/06</p>
 *
 * @since 6.6.0
 */
public abstract class AbstractLettuceSessionStore extends AbstractSessionStore {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLettuceSessionStore.class);

    abstract protected void scan(Consumer<SessionData> func);

    @Override
    public Set<String> doGetExpired(Set<String> candidates) {
        long now = System.currentTimeMillis();
        Set<String> expired = new HashSet<>();
        // iterate over the saved sessions and work out which have expired
        scan(sessionData -> {
            long expiry = sessionData.getExpiry();
            if (expiry > 0 && expiry < now) {
                expired.add(sessionData.getId());
            }
        });
        for (String id : candidates) {
            if (!expired.contains(id)) {
                try {
                    SessionData data = load(id);
                    if (data != null) {
                        if (data.getExpiry() > 0 && data.getExpiry() <= now) {
                            expired.add(id);
                        }
                    } else {
                        // if the session no longer exists
                        expired.add(id);
                    }
                } catch (Exception e) {
                    logger.warn("Error checking if session " + id + " has expired", e);
                }
            }
        }
        return expired;
    }

    protected boolean checkExpiry(SessionData data) {
        if (data != null) {
            return (data.getExpiry() <= 0L || data.getExpiry() > System.currentTimeMillis());
        } else {
            return false;
        }
    }

}
