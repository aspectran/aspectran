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

    abstract protected void scan(Consumer<SessionData> func);

    @Override
    public Set<String> doGetExpired(long time) {
        Set<String> expired = new HashSet<>();
        // iterate over the saved sessions and work out which have expired
        scan(sessionData -> {
            long expiry = sessionData.getExpiry();
            if (expiry > 0 && expiry <= time) {
                expired.add(sessionData.getId());
            }
        });
        return expired;
    }

    @Override
    public void doCleanOrphans(long time) {
        // Unnecessary
    }

    @Override
    public Set<String> getAllSessions() {
        long now = System.currentTimeMillis();
        Set<String> all = new HashSet<>();
        scan(sessionData -> {
            long expiry = sessionData.getExpiry();
            if (expiry <= 0 || expiry > now) {
                all.add(sessionData.getId());
            }
        });
        return all;
    }

}
